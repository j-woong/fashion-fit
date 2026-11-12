import { ApiError, apiGet, apiPut } from './client'

// 백엔드 BodyProfileRequest / BodyProfileResponse 와 같은 모양 (둘이 같아서 타입 하나로 씀)
export type BodyProfile = {
  heightCm: number | null
  weightKg: number | null
  footSizeMm: number
}

// 아직 입력 안 했으면(404) null
export const getBodyProfile = async (): Promise<BodyProfile | null> => {
  try {
    return await apiGet<BodyProfile>('/me/body-profile')
  } catch (e) {
    if (e instanceof ApiError && e.status === 404) return null
    throw e
  }
}

export const saveBodyProfile = (profile: BodyProfile) =>
  apiPut<BodyProfile>('/me/body-profile', profile)
