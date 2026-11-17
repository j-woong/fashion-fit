import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router'
import { ApiError } from '../api/client'
import {
  createGarment,
  getGarmentOptions,
  listGarments,
  updateGarment,
  type Garment,
  type GarmentOptions,
} from '../api/garments'

// /my-sizes : 내 사이즈 입력
// 옷 종류별로 "나에게 잘 맞는 실측"을 하나씩 저장한다. 이 값이 사이즈 비교의 기준이 된다.
// (백엔드에서는 종류별 기준 옷 1벌로 저장된다: reference_garment + garment_measurement)
function MySizesPage() {
  const [options, setOptions] = useState<GarmentOptions | null>(null)
  // 종류별로 저장돼 있는 내 사이즈 (없으면 키가 없음)
  const [saved, setSaved] = useState<Record<string, Garment>>({})
  const [category, setCategory] = useState('')
  // 종류별 입력값. 탭을 오가도 입력하던 값이 남도록 종류별로 따로 들고 있다
  const [values, setValues] = useState<Record<string, Record<string, string>>>({})
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [message, setMessage] = useState<{ tone: 'ok' | 'error'; text: string } | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    Promise.all([getGarmentOptions(), listGarments()]).then(([opts, garments]) => {
      // 목록은 최근 등록순이라, 종류마다 처음 나오는 것이 가장 최근 값이다
      const byCategory: Record<string, Garment> = {}
      for (const g of garments) {
        if (!byCategory[g.category]) byCategory[g.category] = g
      }
      setOptions(opts)
      setSaved(byCategory)
      setValues(
        Object.fromEntries(
          Object.entries(byCategory).map(([code, g]) => [
            code,
            Object.fromEntries(g.measurements.map((m) => [m.part, String(m.valueCm)])),
          ]),
        ),
      )
      setCategory(opts.categories[0]?.code ?? '')
    })
  }, [])

  if (!options) {
    return <main className="page">불러오는 중...</main>
  }

  const selected = options.categories.find((c) => c.code === category)
  const current = values[category] ?? {}

  const handleSave = async (e: FormEvent) => {
    e.preventDefault()
    if (!selected) return
    setSaving(true)
    setFieldErrors({})
    setMessage(null)

    const measurements: Record<string, number> = {}
    for (const part of selected.parts) {
      const text = current[part.code]?.trim()
      if (text) measurements[part.code] = Number(text)
    }
    const request = { category, name: `내 사이즈 · ${selected.label}`.slice(0, 50), fitFeel: 'REGULAR', measurements }

    try {
      const existing = saved[category]
      const result = existing ? await updateGarment(existing.id, request) : await createGarment(request)
      setSaved((prev) => ({ ...prev, [category]: result }))
      setMessage({ tone: 'ok', text: `${selected.label} 사이즈를 저장했어요.` })
    } catch (err) {
      if (err instanceof ApiError) {
        setFieldErrors(err.body.fieldErrors)
        setMessage({ tone: 'error', text: err.body.message })
      } else {
        setMessage({ tone: 'error', text: '저장하지 못했어요. 잠시 후 다시 시도해 주세요.' })
      }
    } finally {
      setSaving(false)
    }
  }

  return (
    <main className="page">
      <header className="header">
        <Link className="text-button" to="/">
          ← 홈
        </Link>
      </header>
      <h1>내 사이즈 입력</h1>
      <p className="sub">
        나에게 잘 맞는 옷을 바닥에 평평하게 펴고 재서 넣어 주세요. 한 번 넣어 두면 모든 사이즈 비교의 기준이 돼요.
      </p>

      <div className="tabs" role="tablist">
        {options.categories.map((c) => (
          <button
            key={c.code}
            type="button"
            role="tab"
            aria-selected={c.code === category}
            className={`tab ${c.code === category ? 'on' : ''}`}
            onClick={() => {
              setCategory(c.code)
              setFieldErrors({})
              setMessage(null)
            }}
          >
            {c.label} {saved[c.code] ? '✓' : ''}
          </button>
        ))}
      </div>

      {selected && (
        <form className="card form" onSubmit={handleSave} noValidate>
          <h2>{selected.label}</h2>
          {selected.parts.map((part) => (
            <label key={part.code} className="field">
              <span>{part.label} (cm)</span>
              <input
                type="number"
                inputMode="decimal"
                step="0.1"
                placeholder="cm"
                value={current[part.code] ?? ''}
                onChange={(e) =>
                  setValues((prev) => ({ ...prev, [category]: { ...current, [part.code]: e.target.value } }))
                }
              />
              <small className="hint">{part.guide}</small>
              {fieldErrors[`measurements.${part.code}`] && (
                <small className="error">{fieldErrors[`measurements.${part.code}`]}</small>
              )}
            </label>
          ))}

          {message && <p className={message.tone === 'ok' ? 'ok' : 'error'}>{message.text}</p>}

          <button type="submit" className="button primary" disabled={saving}>
            {saving ? '저장 중...' : saved[category] ? '수정하기' : '저장하기'}
          </button>
        </form>
      )}
    </main>
  )
}

export default MySizesPage
