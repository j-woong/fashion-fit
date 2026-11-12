import { apiGet } from './client'

// 백엔드 HealthResponse(record)와 같은 모양
export type HealthResponse = {
  status: string
  serverTime: string
}

export const getHealth = () => apiGet<HealthResponse>('/health')
