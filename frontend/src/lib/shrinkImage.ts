// 올리기 전에 큰 스크린샷을 줄인다.
// 레티나 화면 캡처는 가로 3000px 가까이 되는데, 이미지가 클수록 LLM 이 읽는 양(토큰)이 늘어 느려진다.
// 짧은 변 기준으로 줄이는 이유: 세로로 긴 상세페이지 캡처를 긴 변 기준으로 줄이면 글자가 뭉개져서 못 읽는다.
const MAX_SHORT_EDGE = 1280
const MAX_LONG_EDGE = 4000

export async function shrinkImage(file: File): Promise<File> {
  try {
    const bitmap = await createImageBitmap(file)
    const short = Math.min(bitmap.width, bitmap.height)
    const long = Math.max(bitmap.width, bitmap.height)
    const scale = Math.min(MAX_SHORT_EDGE / short, MAX_LONG_EDGE / long)
    if (scale >= 1) {
      bitmap.close()
      return file
    }

    const canvas = document.createElement('canvas')
    canvas.width = Math.round(bitmap.width * scale)
    canvas.height = Math.round(bitmap.height * scale)
    const ctx = canvas.getContext('2d')
    if (!ctx) return file
    // JPEG 는 투명을 못 담아서 검게 나온다. 흰 바탕을 먼저 깐다
    ctx.fillStyle = '#fff'
    ctx.fillRect(0, 0, canvas.width, canvas.height)
    ctx.drawImage(bitmap, 0, 0, canvas.width, canvas.height)
    bitmap.close()

    const blob = await new Promise<Blob | null>((resolve) => canvas.toBlob(resolve, 'image/jpeg', 0.9))
    return blob ? new File([blob], file.name.replace(/\.\w+$/, '') + '.jpg', { type: 'image/jpeg' }) : file
  } catch {
    // 줄이기에 실패해도 원본으로 계속 진행한다 (속도만 느릴 뿐 기능은 같다)
    return file
  }
}
