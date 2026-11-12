import { apiDelete, apiGet, apiPost, apiPut } from './client'

// ---- 등록 화면 선택지 (백엔드 GarmentOptionsResponse 와 짝) ----
export type PartOption = { code: string; label: string; guide: string }
export type CategoryOption = { code: string; label: string; parts: PartOption[] }
export type Option = { code: string; label: string }
export type GarmentOptions = { categories: CategoryOption[]; fitFeels: Option[] }

// ---- 기준 옷 (백엔드 GarmentResponse 와 짝) ----
export type Measurement = { part: string; label: string; valueCm: number }
export type Garment = {
  id: number
  category: string
  categoryLabel: string
  name: string
  fitFeel: string
  fitFeelLabel: string
  measurements: Measurement[]
}

// 백엔드 GarmentRequest 와 짝. measurements 예: { TOTAL_LENGTH: 70, CHEST_WIDTH: 58.5 }
export type GarmentRequest = {
  category: string
  name: string
  fitFeel: string
  measurements: Record<string, number>
}

export const getGarmentOptions = () => apiGet<GarmentOptions>('/garment-options')
export const listGarments = () => apiGet<Garment[]>('/me/garments')
export const getGarment = (id: number) => apiGet<Garment>(`/me/garments/${id}`)
export const createGarment = (req: GarmentRequest) => apiPost<Garment>('/me/garments', req)
export const updateGarment = (id: number, req: GarmentRequest) => apiPut<Garment>(`/me/garments/${id}`, req)
export const deleteGarment = (id: number) => apiDelete<void>(`/me/garments/${id}`)
