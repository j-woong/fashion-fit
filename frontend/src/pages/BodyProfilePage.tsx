import { useState, type FormEvent } from 'react'
import { saveBodyProfile, type BodyProfile } from '../api/bodyProfile'
import { ApiError } from '../api/client'

type Props = {
  // 처음 가입한 사람(온보딩)인지, 홈에서 "수정"을 누른 사람인지
  mode: 'onboarding' | 'edit'
  initial: BodyProfile | null
  onSaved: (saved: BodyProfile) => void
  onCancel?: () => void
}

// input 은 항상 문자열을 다루므로, 화면 상태는 문자열로 두고 보낼 때 숫자로 바꾼다
const toText = (n: number | null | undefined) => (n == null ? '' : String(n))
const toNumberOrNull = (s: string) => (s.trim() === '' ? null : Number(s))

function BodyProfilePage({ mode, initial, onSaved, onCancel }: Props) {
  const [heightCm, setHeightCm] = useState(toText(initial?.heightCm))
  const [weightKg, setWeightKg] = useState(toText(initial?.weightKg))
  const [footSizeMm, setFootSizeMm] = useState(toText(initial?.footSizeMm))
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault() // form 기본 동작(페이지 새로고침) 막기
    setSaving(true)
    setFieldErrors({})
    setFormError(null)
    try {
      const saved = await saveBodyProfile({
        heightCm: toNumberOrNull(heightCm),
        weightKg: toNumberOrNull(weightKg),
        // 비어 있으면 null 로 보내서 서버의 @NotNull 메시지를 그대로 보여준다
        footSizeMm: toNumberOrNull(footSizeMm) as number,
      })
      onSaved(saved)
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

  return (
    <main className="page">
      <h1>{mode === 'onboarding' ? '신체 정보 입력' : '신체 정보 수정'}</h1>
      <p className="sub">
        {mode === 'onboarding'
          ? '사이즈 추천에 쓰여요. 발 사이즈만 필수예요.'
          : '바뀐 값만 고치고 저장하세요.'}
      </p>

      <form className="card form" onSubmit={handleSubmit} noValidate>
        <label className="field">
          <span>
            발 사이즈 (mm) <em className="required">필수</em>
          </span>
          <input
            type="number"
            inputMode="numeric"
            placeholder="예: 265"
            value={footSizeMm}
            onChange={(e) => setFootSizeMm(e.target.value)}
          />
          {fieldErrors.footSizeMm && <small className="error">{fieldErrors.footSizeMm}</small>}
        </label>

        <label className="field">
          <span>
            키 (cm) <em className="optional">권장</em>
          </span>
          <input
            type="number"
            inputMode="numeric"
            placeholder="예: 175"
            value={heightCm}
            onChange={(e) => setHeightCm(e.target.value)}
          />
          {fieldErrors.heightCm && <small className="error">{fieldErrors.heightCm}</small>}
        </label>

        <label className="field">
          <span>
            몸무게 (kg) <em className="optional">선택</em>
          </span>
          <input
            type="number"
            inputMode="numeric"
            placeholder="입력하지 않아도 돼요"
            value={weightKg}
            onChange={(e) => setWeightKg(e.target.value)}
          />
          {fieldErrors.weightKg && <small className="error">{fieldErrors.weightKg}</small>}
        </label>

        {formError && <p className="error">{formError}</p>}

        <button type="submit" className="button primary" disabled={saving}>
          {saving ? '저장 중...' : '저장하기'}
        </button>
        {onCancel && (
          <button type="button" className="text-button" onClick={onCancel}>
            취소
          </button>
        )}
      </form>
    </main>
  )
}

export default BodyProfilePage
