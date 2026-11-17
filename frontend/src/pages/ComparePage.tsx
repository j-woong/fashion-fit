import { useEffect, useRef, useState, type FormEvent } from 'react'
import { Link } from 'react-router'
import { analyzeGarment, type GarmentAnalysisResult } from '../api/analysis'
import { ApiError } from '../api/client'
import { getGarmentOptions, listGarments, type Garment, type GarmentOptions } from '../api/garments'
import {
  createSizeCheck,
  extractSizeChart,
  extractSizeChartFromText,
  findSavedSizeChart,
  type ExtractedSizeChart,
  type SavedSizeChart,
  type SizeCheck,
  type SizeInput,
} from '../api/sizeChecks'
import AnalysisResult from '../components/AnalysisResult'
import ImagePicker from '../components/ImagePicker'
import SizeCheckResult from '../components/SizeCheckResult'
import { shrinkImage } from '../lib/shrinkImage'

// 실측표의 한 줄 (사이즈 하나). 입력칸 값은 문자열로 들고 있다가 보낼 때 숫자로 바꾼다
type Row = { label: string; values: Record<string, string> }

const emptyRow = (label = ''): Row => ({ label, values: {} })
const errorMessage = (e: unknown, fallback: string) =>
  e instanceof ApiError ? (e.body.fieldErrors.image ?? e.body.fieldErrors.text ?? e.body.message) : fallback
const isEmptyTable = (rows: Row[]) => rows.every((row) => Object.values(row.values).every((v) => !v.trim()))

// /compare : 옷 비교하기
//   1단계 옷 스크린샷 → 특징·촉감 분석, 필터 달린 비슷한 옷 검색
//   2단계 실측표 → 내 사이즈와 비교, 핏 판정
//     실측표를 채우는 길 (빠른 순): 전에 넣은 상품 링크 → 글자 붙여넣기 → 스크린샷(LLM) → 직접 입력
// 두 단계는 각각 따로 써도 되고, 1단계를 하면 2단계의 종류·상품명이 자동으로 채워진다.
function ComparePage() {
  const [options, setOptions] = useState<GarmentOptions | null>(null)
  const [garments, setGarments] = useState<Garment[]>([])

  // 1단계: 옷 스크린샷
  const [photo, setPhoto] = useState<File | null>(null)
  const [analysis, setAnalysis] = useState<GarmentAnalysisResult | null>(null)
  const [analyzing, setAnalyzing] = useState(false)
  const [analysisError, setAnalysisError] = useState<string | null>(null)

  // 2단계: 실측표
  const [category, setCategory] = useState('')
  const [productName, setProductName] = useState('')
  const [productUrl, setProductUrl] = useState('')
  const [rows, setRows] = useState<Row[]>([emptyRow()])
  const [reading, setReading] = useState(false)
  const [chartText, setChartText] = useState('')
  // 자동으로 채운 칸("줄번호:부위"). 색으로 표시해서 어디를 확인하면 되는지 보여준다. 손으로 고친 칸은 빠진다.
  const [autoFilled, setAutoFilled] = useState<Set<string>>(new Set())
  // 같은 링크로 저장해 둔 실측표를 찾았는데, 표에 이미 값이 있어서 덮어쓸지 물어봐야 하는 경우
  const [savedOffer, setSavedOffer] = useState<SavedSizeChart | null>(null)
  const [lookedUpUrl, setLookedUpUrl] = useState('')
  const [chartMessage, setChartMessage] = useState<{ tone: 'ok' | 'error'; text: string } | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [comparing, setComparing] = useState(false)
  const [sizeCheck, setSizeCheck] = useState<SizeCheck | null>(null)

  useEffect(() => {
    getGarmentOptions().then(setOptions)
    listGarments().then(setGarments)
  }, [])

  const selected = options?.categories.find((c) => c.code === category)
  const parts = selected?.parts ?? []
  // 재는 부위가 같은 종류끼리 비교할 수 있다 (상의는 상의끼리, 바지는 바지끼리)
  const partsKey = (code: string) =>
    options?.categories.find((c) => c.code === code)?.parts.map((p) => p.code).join(',') ?? ''
  const hasMySize = garments.some((g) => selected && partsKey(g.category) === partsKey(category))

  const handleAnalyze = async () => {
    if (!photo) return
    setAnalyzing(true)
    setAnalysisError(null)
    try {
      const result = await analyzeGarment(await shrinkImage(photo))
      setAnalysis(result)
      // 2단계를 미리 채운다 (이미 입력한 값은 덮어쓰지 않는다)
      if (result.category) setCategory((prev) => prev || result.category!)
      setProductName((prev) => prev || result.itemName)
    } catch (e) {
      setAnalysisError(errorMessage(e, '분석하지 못했어요. 잠시 후 다시 시도해 주세요.'))
    } finally {
      setAnalyzing(false)
    }
  }

  // 읽어 온 실측표를 표에 채우고, 채운 칸을 표시한다
  const fillRows = (sizes: ExtractedSizeChart['sizes'], text: string) => {
    setRows(
      sizes.map((s) => ({
        label: s.label,
        values: Object.fromEntries(Object.entries(s.measurements).map(([part, v]) => [part, String(v)])),
      })),
    )
    setAutoFilled(new Set(sizes.flatMap((s, i) => Object.keys(s.measurements).map((part) => `${i}:${part}`))))
    setChartMessage({ tone: 'ok', text })
  }

  // 상품 링크를 넣으면 전에 저장한 실측표가 있는지 찾아본다 (있으면 실측표를 다시 넣을 필요가 없다)
  const handleUrlBlur = async () => {
    const url = productUrl.trim()
    if (!url || url === lookedUpUrl) return
    setLookedUpUrl(url)
    try {
      const saved = await findSavedSizeChart(url)
      if (!saved) return
      if (isEmptyTable(rows)) applySaved(saved)
      else setSavedOffer(saved)
    } catch {
      // 못 찾아도 직접 넣으면 되니 조용히 넘어간다
    }
  }

  const applySaved = (saved: SavedSizeChart) => {
    setCategory(saved.category)
    setProductName((prev) => prev || saved.productName)
    fillRows(
      saved.sizes,
      `전에 비교한 '${saved.productName}'의 실측표를 불러왔어요 (${saved.savedAt.slice(0, 10)} 저장). 달라졌으면 고쳐서 비교하면 새 값으로 저장돼요.`,
    )
    setSavedOffer(null)
  }

  // 상품 설명에서 복사한 글자에서 읽는다. LLM 을 거치지 않아서 바로 끝난다.
  // 입력이 0.4초 멈추면 자동으로 읽어서 표를 채운다 (버튼을 누르지 않아도 된다).
  // textRequest: 응답이 뒤바뀌어 도착해도 마지막 요청의 결과만 반영하기 위한 번호표.
  const textTimer = useRef<number | undefined>(undefined)
  const textRequest = useRef(0)

  const handleChartText = async (text: string, quiet: boolean) => {
    window.clearTimeout(textTimer.current)
    if (!text.trim()) return
    const requestId = ++textRequest.current
    try {
      const { sizes } = await extractSizeChartFromText(text, category)
      if (requestId !== textRequest.current) return
      fillRows(sizes, `${sizes.length}개 사이즈를 읽었어요. 색칠된 칸의 숫자가 맞는지 확인해 주세요.`)
    } catch (e) {
      if (requestId !== textRequest.current) return
      // 입력하는 도중에는 아직 못 읽는 게 당연하니 조용히 넘어가고, 버튼을 눌렀을 때만 알려준다
      if (!quiet) {
        setChartMessage({ tone: 'error', text: errorMessage(e, '글자를 읽지 못했어요. 직접 입력해 주세요.') })
      }
    }
  }

  const handleChartTextChange = (text: string) => {
    setChartText(text)
    window.clearTimeout(textTimer.current)
    textTimer.current = window.setTimeout(() => handleChartText(text, true), 400)
  }

  // 실측표 스크린샷을 올리면 표에 자동으로 채운다. 틀릴 수 있으니 사용자가 확인하고 고칠 수 있다.
  const handleChartImage = async (file: File) => {
    setReading(true)
    setChartMessage(null)
    try {
      const { sizes } = await extractSizeChart(await shrinkImage(file), category)
      fillRows(sizes, `${sizes.length}개 사이즈를 읽었어요. 색칠된 칸의 숫자가 맞는지 확인해 주세요.`)
    } catch (e) {
      setChartMessage({ tone: 'error', text: errorMessage(e, '실측표를 읽지 못했어요. 직접 입력해 주세요.') })
    } finally {
      setReading(false)
    }
  }

  const updateRow = (index: number, patch: Partial<Row>) =>
    setRows((prev) => prev.map((row, i) => (i === index ? { ...row, ...patch } : row)))

  const handleCompare = async (e: FormEvent) => {
    e.preventDefault()
    setComparing(true)
    setFieldErrors({})
    setFormError(null)

    // 값이 있는 줄만 보낸다 (한 사이즈만 넣어도 된다). 사이즈 이름을 비우면 FREE 로 보낸다.
    const filled = rows.filter((row) => parts.some((p) => row.values[p.code]?.trim()))
    if (filled.length > 0 && filled.length < rows.length) {
      // 빈 줄을 표에서도 지워서, 서버가 알려주는 "n번째 줄" 오류 위치가 화면과 어긋나지 않게 한다
      setRows(filled)
      setAutoFilled(new Set())
    }
    const sizes: SizeInput[] = filled.map((row) => ({
      label: row.label.trim() || 'FREE',
      measurements: Object.fromEntries(
        parts.filter((p) => row.values[p.code]?.trim()).map((p) => [p.code, Number(row.values[p.code])]),
      ),
    }))

    try {
      setSizeCheck(
        await createSizeCheck({
          category,
          productName,
          productUrl: productUrl.trim() || null,
          referenceGarmentId: null, // 서버가 같은 종류의 내 사이즈를 골라 준다
          sizes,
        }),
      )
    } catch (err) {
      if (err instanceof ApiError) {
        setFieldErrors(err.body.fieldErrors)
        setFormError(err.body.message)
      } else {
        setFormError('비교하지 못했어요. 잠시 후 다시 시도해 주세요.')
      }
    } finally {
      setComparing(false)
    }
  }

  if (!options) {
    return <main className="page">불러오는 중...</main>
  }

  const rowErrors = (i: number) =>
    Object.entries(fieldErrors)
      .filter(([key]) => key === `sizes[${i}]` || key.startsWith(`sizes[${i}].`))
      .map(([, message]) => message)

  return (
    <main className="page">
      <header className="header">
        <Link className="text-button" to="/">
          ← 홈
        </Link>
        <Link className="text-button" to="/size-checks">
          비교 기록
        </Link>
      </header>
      <h1>옷 비교하기</h1>
      <p className="sub">옷 사진으로 비슷한 옷을 찾고, 실측표로 내 사이즈와 비교해요.</p>

      {/* ---------- 1단계 ---------- */}
      <section className="card form">
        <h2>
          <span className="step">1</span> 옷 스크린샷
        </h2>
        <ImagePicker
          hint="또는 스크린샷을 찍고 Cmd+V 로 붙여 넣기"
          pasteEnabled={!analysis}
          onPick={(file) => {
            setPhoto(file)
            setAnalysis(null)
            setAnalysisError(null)
          }}
        />
        {analysisError && <p className="error">{analysisError}</p>}
        <button type="button" className="button primary" disabled={!photo || analyzing} onClick={handleAnalyze}>
          {analyzing ? '분석 중... (10초 정도 걸려요)' : '분석하기'}
        </button>
      </section>

      {analysis && <AnalysisResult result={analysis} />}

      {/* ---------- 2단계 ---------- */}
      <form className="card form" onSubmit={handleCompare} noValidate>
        <h2>
          <span className="step">2</span> 실측표로 내 사이즈와 비교
        </h2>

        <label className="field">
          <span>옷 종류</span>
          <select value={category} onChange={(e) => setCategory(e.target.value)}>
            <option value="" disabled>
              골라 주세요
            </option>
            {options.categories.map((c) => (
              <option key={c.code} value={c.code}>
                {c.label}
              </option>
            ))}
          </select>
          {fieldErrors.category && <small className="error">{fieldErrors.category}</small>}
          {selected && !hasMySize && (
            <small className="hint">
              이 종류의 내 사이즈가 아직 없어요. <Link to="/my-sizes">내 사이즈를 입력</Link>하면 추천을 받을 수 있어요.
            </small>
          )}
        </label>

        <label className="field">
          <span>상품 이름</span>
          <input
            type="text"
            placeholder="예: 오버핏 스웨트셔츠"
            maxLength={100}
            value={productName}
            onChange={(e) => setProductName(e.target.value)}
          />
          {fieldErrors.productName && <small className="error">{fieldErrors.productName}</small>}
        </label>

        <label className="field">
          <span>
            상품 링크 <em className="optional">선택</em>
          </span>
          <input
            type="url"
            placeholder="https://"
            maxLength={500}
            value={productUrl}
            onChange={(e) => setProductUrl(e.target.value)}
            onBlur={handleUrlBlur}
          />
          <small className="hint">전에 비교한 상품이면 저장해 둔 실측표를 바로 불러와요.</small>
          {savedOffer && (
            <small className="hint">
              전에 비교한 '{savedOffer.productName}'의 실측표가 있어요.{' '}
              <button type="button" className="text-button strong" onClick={() => applySaved(savedOffer)}>
                불러오기
              </button>
            </small>
          )}
        </label>

        {selected && (
          <fieldset className="field">
            <legend>실측 글자 붙여넣기 (가장 빠름)</legend>
            <textarea
              rows={3}
              maxLength={5000}
              placeholder={'상품 설명의 사이즈 부분을 복사해서 붙여 넣으세요\n예: M - 어깨 52 / 가슴 58 / 소매 62 / 총장 68'}
              value={chartText}
              onChange={(e) => handleChartTextChange(e.target.value)}
            />
            <small className="hint">붙여 넣거나 입력하면 아래 표에 자동으로 채워져요.</small>
            {chartText.trim() && (
              <button type="button" className="text-button strong" onClick={() => handleChartText(chartText, false)}>
                다시 읽기
              </button>
            )}
          </fieldset>
        )}

        {selected && (
          <fieldset className="field">
            <legend>실측표 스크린샷 (글자로 복사할 수 없을 때)</legend>
            <ImagePicker
              hint={analysis ? '상품 페이지의 실측표를 캡처해서 올리거나 Cmd+V' : '상품 페이지의 실측표를 캡처해서 올려 주세요'}
              pasteEnabled={!!analysis}
              onPick={handleChartImage}
            />
            {reading && <p className="muted">실측표를 읽는 중... (10초 정도 걸려요)</p>}
          </fieldset>
        )}

        {selected && (
          <fieldset className="field">
            <legend>실측표 (cm) · 한 사이즈만 넣어도 돼요 (사이즈 이름은 비워도 돼요)</legend>
            {chartMessage && <p className={chartMessage.tone === 'ok' ? 'ok' : 'error'}>{chartMessage.text}</p>}
            <div className="table-scroll">
              <table className="size-table">
                <thead>
                  <tr>
                    <th>사이즈</th>
                    {parts.map((p) => (
                      <th key={p.code} title={p.guide}>
                        {p.label}
                      </th>
                    ))}
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {rows.map((row, i) => (
                    <tr key={i}>
                      <td>
                        <input
                          type="text"
                          placeholder="예: M"
                          maxLength={20}
                          value={row.label}
                          onChange={(e) => updateRow(i, { label: e.target.value })}
                        />
                      </td>
                      {parts.map((p) => (
                        <td key={p.code}>
                          <input
                            type="number"
                            inputMode="decimal"
                            step="0.1"
                            className={autoFilled.has(`${i}:${p.code}`) ? 'filled' : undefined}
                            value={row.values[p.code] ?? ''}
                            onChange={(e) => {
                              updateRow(i, { values: { ...row.values, [p.code]: e.target.value } })
                              setAutoFilled((prev) => {
                                const next = new Set(prev)
                                next.delete(`${i}:${p.code}`)
                                return next
                              })
                            }}
                          />
                        </td>
                      ))}
                      <td>
                        <button
                          type="button"
                          className="text-button danger"
                          aria-label={`${row.label || '이'} 사이즈 줄 삭제`}
                          onClick={() => {
                            setRows((prev) => prev.filter((_, j) => j !== i))
                            setAutoFilled(new Set()) // 줄 번호가 밀려서 표시를 지운다
                          }}
                          disabled={rows.length === 1}
                        >
                          ✕
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {rows.map((row, i) =>
              rowErrors(i).map((message) => (
                <small key={`${i}-${message}`} className="error">
                  {row.label || `${i + 1}번째 줄`}: {message}
                </small>
              )),
            )}
            {fieldErrors.sizes && <small className="error">{fieldErrors.sizes}</small>}
            <button
              type="button"
              className="text-button strong"
              onClick={() => setRows((prev) => [...prev, emptyRow()])}
              disabled={rows.length >= 10}
            >
              + 사이즈 추가
            </button>
          </fieldset>
        )}

        {formError && <p className="error">{formError}</p>}

        <button type="submit" className="button primary" disabled={comparing || !selected}>
          {comparing ? '비교 중...' : '내 사이즈와 비교하기'}
        </button>
      </form>

      {sizeCheck && <SizeCheckResult check={sizeCheck} />}
    </main>
  )
}

export default ComparePage
