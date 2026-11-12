import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { ApiError } from '../api/client'
import {
  createGarment,
  getGarment,
  getGarmentOptions,
  updateGarment,
  type GarmentOptions,
  type GarmentRequest,
} from '../api/garments'

// /closet/new           → 등록
// /closet/:garmentId/edit → 수정 (같은 화면을 재사용)
function GarmentFormPage() {
  const { garmentId } = useParams()
  const editingId = garmentId ? Number(garmentId) : null
  const navigate = useNavigate()

  const [options, setOptions] = useState<GarmentOptions | null>(null)
  const [category, setCategory] = useState('')
  const [name, setName] = useState('')
  const [fitFeel, setFitFeel] = useState('')
  // 입력칸 값은 문자열로 들고 있다가 보낼 때 숫자로 바꾼다. 키는 부위 코드 (CHEST_WIDTH 등)
  const [values, setValues] = useState<Record<string, string>>({})
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  // 선택지(카테고리별 부위, 핏 느낌)를 불러오고, 수정이면 기존 값도 채운다
  useEffect(() => {
    getGarmentOptions().then(setOptions)
    if (editingId !== null) {
      getGarment(editingId).then((g) => {
        setCategory(g.category)
        setName(g.name)
        setFitFeel(g.fitFeel)
        setValues(Object.fromEntries(g.measurements.map((m) => [m.part, String(m.valueCm)])))
      })
    }
  }, [editingId])

  // 지금 고른 카테고리에서 재야 하는 부위들
  const parts = options?.categories.find((c) => c.code === category)?.parts ?? []

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setFieldErrors({})
    setFormError(null)

    // 지금 카테고리의 부위만, 입력한 칸만 보낸다. (빈 칸은 서버가 "값을 입력해 주세요"로 알려준다)
    const measurements: Record<string, number> = {}
    for (const part of parts) {
      const text = values[part.code]?.trim()
      if (text) measurements[part.code] = Number(text)
    }
    const request: GarmentRequest = { category, name, fitFeel, measurements }

    try {
      if (editingId === null) {
        await createGarment(request)
      } else {
        await updateGarment(editingId, request)
      }
      navigate('/closet')
    } catch (err) {
      if (err instanceof ApiError) {
        setFieldErrors(err.body.fieldErrors)
        setFormError(err.body.message)
      } else {
        setFormError('저장하지 못했어요. 잠시 후 다시 시도해 주세요.')
      }
    } finally {
      setSaving(false)
    }
  }

  if (!options) {
    return <main className="page">불러오는 중...</main>
  }

  return (
    <main className="page">
      <header className="header">
        <Link className="text-button" to="/closet">
          ← 내 옷장
        </Link>
      </header>
      <h1>{editingId === null ? '기준 옷 등록' : '기준 옷 수정'}</h1>
      <p className="sub">잘 맞는 옷을 바닥에 평평하게 펴고 cm 단위로 재 주세요.</p>

      <form className="card form" onSubmit={handleSubmit} noValidate>
        <label className="field">
          <span>카테고리</span>
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
        </label>

        <label className="field">
          <span>옷 이름</span>
          <input
            type="text"
            placeholder="예: 무지 흰 반팔 (L)"
            maxLength={50}
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
          {fieldErrors.name && <small className="error">{fieldErrors.name}</small>}
        </label>

        <fieldset className="field">
          <legend>이 옷을 어떻게 입나요?</legend>
          <div className="radio-group">
            {options.fitFeels.map((f) => (
              <label key={f.code} className={`radio ${fitFeel === f.code ? 'checked' : ''}`}>
                <input
                  type="radio"
                  name="fitFeel"
                  value={f.code}
                  checked={fitFeel === f.code}
                  onChange={() => setFitFeel(f.code)}
                />
                {f.label}
              </label>
            ))}
          </div>
          {fieldErrors.fitFeel && <small className="error">{fieldErrors.fitFeel}</small>}
        </fieldset>

        {/* 카테고리를 고르면 그 카테고리의 부위만큼 입력칸이 생긴다 */}
        {parts.length > 0 && (
          <fieldset className="field">
            <legend>실측 (cm)</legend>
            {parts.map((part) => (
              <label key={part.code} className="field">
                <span>{part.label}</span>
                <input
                  type="number"
                  inputMode="decimal"
                  step="0.1"
                  placeholder="cm"
                  value={values[part.code] ?? ''}
                  onChange={(e) => setValues((prev) => ({ ...prev, [part.code]: e.target.value }))}
                />
                <small className="hint">{part.guide}</small>
                {fieldErrors[`measurements.${part.code}`] && (
                  <small className="error">{fieldErrors[`measurements.${part.code}`]}</small>
                )}
              </label>
            ))}
          </fieldset>
        )}

        {formError && <p className="error">{formError}</p>}

        <button type="submit" className="button primary" disabled={saving}>
          {saving ? '저장 중...' : '저장하기'}
        </button>
      </form>
    </main>
  )
}

export default GarmentFormPage
