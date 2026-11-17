import { apiPostForm } from './client'

// 백엔드 GarmentAnalysisResponse 와 짝
export type MallLink = { mall: string; label: string; url: string }
export type SearchLinks = { query: string; links: MallLink[] }
export type GarmentAnalysisResult = {
  category: string | null // 사이즈 비교 카테고리. 해당 없으면 null
  categoryLabel: string | null
  itemName: string
  summary: string
  color: string
  fit: string
  materialGuess: string
  textureGuess: string | null // 사진으로 추정한 촉감
  materialKeywords: string[] // 소재 필터 칩으로 쓴다
  details: string[]
  qualityChecks: string[]
  searches: SearchLinks[]
}

export const analyzeGarment = (image: File) => {
  const form = new FormData()
  form.append('image', image)
  return apiPostForm<GarmentAnalysisResult>('/me/garment-analyses', form)
}
