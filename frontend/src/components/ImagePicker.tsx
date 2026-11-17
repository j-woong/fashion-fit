import { useEffect, useRef, useState, type ChangeEvent } from 'react'

const MAX_BYTES = 5 * 1024 * 1024

type Props = {
  hint: string
  // true 인 동안 Cmd+V 로 붙여 넣은 이미지를 이 칸이 받는다 (한 화면에 칸이 둘이라 한쪽만 켠다)
  pasteEnabled: boolean
  onPick: (file: File) => void
}

// 이미지 고르기 + 미리보기 + 붙여넣기. 옷 스크린샷과 실측표 스크린샷이 같이 쓴다.
function ImagePicker({ hint, pasteEnabled, onPick }: Props) {
  const [previewUrl, setPreviewUrl] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const previewUrlRef = useRef<string | null>(null)
  // 최신 onPick 을 붙여넣기 리스너에서 쓰기 위한 ref (리스너를 매번 다시 달지 않으려고)
  const onPickRef = useRef(onPick)
  useEffect(() => {
    onPickRef.current = onPick
  })

  const pick = (picked: File | null | undefined) => {
    if (!picked) return
    if (!picked.type.startsWith('image/')) {
      setError('이미지 파일만 올릴 수 있어요.')
      return
    }
    if (picked.size > MAX_BYTES) {
      setError('5MB 이하 이미지만 올릴 수 있어요.')
      return
    }
    setError(null)
    if (previewUrlRef.current) URL.revokeObjectURL(previewUrlRef.current)
    previewUrlRef.current = URL.createObjectURL(picked)
    setPreviewUrl(previewUrlRef.current)
    onPickRef.current(picked)
  }
  const pickRef = useRef(pick)
  useEffect(() => {
    pickRef.current = pick
  })

  useEffect(() => {
    if (!pasteEnabled) return
    const onPaste = (e: ClipboardEvent) => {
      const image = Array.from(e.clipboardData?.files ?? []).find((f) => f.type.startsWith('image/'))
      if (image) pickRef.current(image)
    }
    window.addEventListener('paste', onPaste)
    return () => window.removeEventListener('paste', onPaste)
  }, [pasteEnabled])

  // 화면을 떠날 때 미리보기 주소 해제
  useEffect(
    () => () => {
      if (previewUrlRef.current) URL.revokeObjectURL(previewUrlRef.current)
    },
    [],
  )

  return (
    <>
      <label className="upload">
        {previewUrl ? (
          <img src={previewUrl} alt="올린 이미지" />
        ) : (
          <span>
            눌러서 사진 고르기
            <br />
            <small className="muted">{hint}</small>
          </span>
        )}
        <input
          type="file"
          accept="image/png,image/jpeg,image/webp"
          onChange={(e: ChangeEvent<HTMLInputElement>) => pick(e.target.files?.[0])}
        />
      </label>
      {error && <p className="error">{error}</p>}
    </>
  )
}

export default ImagePicker
