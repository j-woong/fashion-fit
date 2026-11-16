import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import { ApiError } from '../api/client'
import { getGarmentOptions, listGarments, type Garment, type GarmentOptions } from '../api/garments'
import { createSizeCheck, type SizeInput } from '../api/sizeChecks'

// 실측표의 한 줄 (사이즈 하나). 입력칸 값은 문자열로 들고 있다가 보낼 때 숫자로 바꾼다
type Row = { label: string; values: Record<string, string> }

const emptyRow = (label = ''): Row => ({ label, values: {} })

// /size-checks/new : 사고 싶은 옷의 실측표를 입력하는 화면
function SizeCheckFormPage() {
  const navigate = useNavigate()
  const [options, setOptions] = useState<GarmentOptions | null>(null)
  const [garments, setGarments] = useState<Garment[]>([])

  const [category, setCategory] = useState('')
  const [productName, setProductName] = useState('')
  const [productUrl, setProductUrl] = useState('')
  const [referenceId, setReferenceId] = useState('') // '' = 자동 선택
  const [rows, setRows] = useState<Row[]>([emptyRow('S'), emptyRow('M'), emptyRow('L')])

  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    getGarmentOptions().then(setOptions)
    listGarments().then(setGarments)
  }, [])

  const selected = options?.categories.find((c) => c.code === category)
  const parts = selected?.parts ?? []
  // 재는 부위가 같은 카테고리끼리만 비교할 수 있다 (반팔·긴팔·아우터끼리, 바지는 바지끼리)
  const partsKey = (code: string) =>
    options?.categories.find((c) => c.code === code)?.parts.map((p) => p.code).join(',') ?? ''
  const comparableGarments = garments.filter((g) => selected && partsKey(g.category) === partsKey(category))

  const updateRow = (index: number, patch: Partial<Row>) =>
    setRows((prev) => prev.map((row, i) => (i === index ? { ...row, ...patch } : row)))

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setFieldErrors({})
    setFormError(null)

    // 이름도 값도 없는 줄은 빼고, 입력한 칸만 숫자로 보낸다
    const sizes: SizeInput[] = rows
      .filter((row) => row.label.trim() || Object.values(row.values).some((v) => v.trim()))
      .map((row) => ({
        label: row.label.trim(),
        measurements: Object.fromEntries(
          parts
            .filter((p) => row.values[p.code]?.trim())
            .map((p) => [p.code, Number(row.values[p.code])]),
        ),
      }))

    try {
      const result = await createSizeCheck({
        category,
        productName,
        productUrl: productUrl.trim() || null,
        referenceGarmentId: referenceId ? Number(referenceId) : null,
        sizes,
      })
      navigate(`/size-checks/${result.id}`)
    } catch (err) {
      if (err instanceof ApiError) {
        setFieldErrors(err.body.fieldErrors)
        setFormError(err.body.message)
      } else {
        setFormError('비교하지 못했어요. 잠시 후 다시 시도해 주세요.')
      }
    } finally {
      setSaving(false)
    }
  }

  if (!options) {
    return <main className="page">불러오는 중...</main>
  }

  // 사이즈 줄(i)에 해당하는 에러들을 모아서 줄 아래에 보여준다
  const rowErrors = (i: number) =>
    Object.entries(fieldErrors)
      .filter(([key]) => key === `sizes[${i}]` || key.startsWith(`sizes[${i}].`))
      .map(([, message]) => message)

  return (
    <main className="page">
      <header className="header">
        <Link className="text-button" to="/size-checks">
          ← 비교 기록
        </Link>
      </header>
      <h1>사이즈 비교</h1>
      <p className="sub">사고 싶은 옷의 실측표를 그대로 옮겨 적어 주세요. 표에 없는 부위는 비워도 돼요.</p>

      <form className="card form" onSubmit={handleSubmit} noValidate>
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
            value={productUrl}
            onChange={(e) => setProductUrl(e.target.value)}
          />
        </label>

        <label className="field">
          <span>카테고리</span>
          <select
            value={category}
            onChange={(e) => {
              setCategory(e.target.value)
              setReferenceId('')
            }}
          >
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
        </label>

        {selected && (
          <label className="field">
            <span>비교할 내 옷</span>
            <select value={referenceId} onChange={(e) => setReferenceId(e.target.value)}>
              <option value="">자동 선택 (같은 종류 중 최근 등록)</option>
              {comparableGarments.map((g) => (
                <option key={g.id} value={g.id}>
                  {g.name} · {g.fitFeelLabel}
                </option>
              ))}
            </select>
            {comparableGarments.length === 0 && (
              <small className="hint">
                이 종류의 기준 옷이 없어요. <Link to="/closet/new">먼저 등록</Link>하면 추천을 받을 수 있어요.
              </small>
            )}
            {fieldErrors.referenceGarmentId && (
              <small className="error">{fieldErrors.referenceGarmentId}</small>
            )}
          </label>
        )}

        {selected && (
          <fieldset className="field">
            <legend>실측표 (cm)</legend>
            {/* 휴대폰에서는 표를 옆으로 밀어서 본다 */}
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
                          placeholder="M"
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
                            value={row.values[p.code] ?? ''}
                            onChange={(e) =>
                              updateRow(i, { values: { ...row.values, [p.code]: e.target.value } })
                            }
                          />
                        </td>
                      ))}
                      <td>
                        <button
                          type="button"
                          className="text-button danger"
                          aria-label={`${row.label || '이'} 사이즈 줄 삭제`}
                          onClick={() => setRows((prev) => prev.filter((_, j) => j !== i))}
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
            {rows.map((_, i) =>
              rowErrors(i).map((message) => (
                <small key={`${i}-${message}`} className="error">
                  {rows[i].label || `${i + 1}번째 줄`}: {message}
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

        <button type="submit" className="button primary" disabled={saving || !selected}>
          {saving ? '비교 중...' : '내 옷과 비교하기'}
        </button>
      </form>
    </main>
  )
}

export default SizeCheckFormPage
