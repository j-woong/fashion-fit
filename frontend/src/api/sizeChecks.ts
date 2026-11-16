import { apiDelete, apiGet, apiPost } from './client'

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
export type SizeResult = { label: string; score: number | null; recommended: boolean; parts: PartDiff[] }
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
