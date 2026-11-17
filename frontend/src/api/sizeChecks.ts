import { apiDelete, apiGet, apiPost, apiPostForm } from './client'

// ---- 백엔드 SizeCheckRequest 와 짝 ----
export type SizeInput = { label: string; measurements: Record<string, number> }
export type SizeCheckRequest = {
  category: string
  productName: string
  productUrl: string | null
  referenceGarmentId: number | null // null 이면 서버가 자동으로 고름
  sizes: SizeInput[]
}

// ---- 백엔드 SizeCheckResponse 와 짝 ----
export type PartDiff = {
  part: string
  label: string
  productCm: number
  referenceCm: number
  diffCm: number // 상품 - 기준 옷 (양수면 상품이 큼)
}
export type FitType = 'TIGHT' | 'SLIM' | 'REGULAR' | 'SEMI_OVER' | 'OVER'
export type SizeResult = {
  label: string
  score: number | null
  recommended: boolean
  fitType: FitType | null // 내 사이즈 대비 핏 (품 부위가 없으면 null)
  fitLabel: string | null
  fitDescription: string | null
  parts: PartDiff[]
}
export type Verdict = 'GOOD' | 'SLIGHTLY_OFF' | 'FAR' | 'NO_REFERENCE' | 'NOT_COMPARABLE'
export type SizeCheck = {
  id: number
  category: string
  categoryLabel: string
  productName: string
  productUrl: string | null
  reference: { id: number; name: string; fitFeelLabel: string } | null
  recommendedSize: string | null
  verdict: Verdict
  verdictLabel: string
  sizes: SizeResult[]
  createdAt: string
}

// ---- 목록 (백엔드 SizeCheckSummary 와 짝) ----
export type SizeCheckSummary = {
  id: number
  productName: string
  categoryLabel: string
  recommendedSize: string | null
  referenceGarmentName: string | null
  createdAt: string
}

export const createSizeCheck = (req: SizeCheckRequest) => apiPost<SizeCheck>('/me/size-checks', req)
export const listSizeChecks = () => apiGet<SizeCheckSummary[]>('/me/size-checks')
export const getSizeCheck = (id: number) => apiGet<SizeCheck>(`/me/size-checks/${id}`)
export const deleteSizeCheck = (id: number) => apiDelete<void>(`/me/size-checks/${id}`)

// ---- 실측표 스크린샷 자동 입력 (백엔드 SizeChartExtractionResponse 와 짝) ----
export type ExtractedSizeChart = { sizes: { label: string; measurements: Record<string, number> }[] }

export const extractSizeChart = (image: File, category: string) => {
  const form = new FormData()
  form.append('image', image)
  form.append('category', category)
  return apiPostForm<ExtractedSizeChart>('/me/size-chart-extractions', form)
}

// ---- 실측 글자 붙여넣기 자동 입력: LLM 없이 서버가 바로 읽는다 ----
export const extractSizeChartFromText = (text: string, category: string) =>
  apiPost<ExtractedSizeChart>('/me/size-chart-extractions/text', { text, category })

// ---- 같은 상품 링크로 전에 저장한 실측표 (백엔드 SavedSizeChartResponse 와 짝). 없으면 204 → undefined ----
export type SavedSizeChart = {
  sizeCheckId: number
  category: string
  productName: string
  savedAt: string
  sizes: ExtractedSizeChart['sizes']
}

export const findSavedSizeChart = (url: string) =>
  apiGet<SavedSizeChart | undefined>(`/me/size-checks/saved-chart?url=${encodeURIComponent(url)}`)
