import type { MallLink } from '../api/analysis'

// 검색 링크에 필터를 입힌다.
// 상품 데이터를 우리가 갖고 있지 않아서, "필터가 적용된 쇼핑몰 검색 결과 페이지"로 보내는 방식이다.
//  - 소재·핏: 검색어에 단어를 덧붙인다 (모든 쇼핑몰에서 동작)
//  - 가격대·정렬: 쇼핑몰마다 다른 URL 파라미터를 붙인다 (쇼핑몰이 규칙을 바꾸면 여기만 고치면 된다)

export type PriceRange = { label: string; min?: number; max?: number }

export const PRICE_RANGES: PriceRange[] = [
  { label: '1~3만원', min: 10000, max: 30000 },
  { label: '3~5만원', min: 30000, max: 50000 },
  { label: '5~10만원', min: 50000, max: 100000 },
  { label: '10~20만원', min: 100000, max: 200000 },
  { label: '20만원 이상', min: 200000 },
]

export const FIT_KEYWORDS = ['슬림핏', '레귤러핏', '오버핏']

export type Filters = {
  price: PriceRange | null
  material: string | null
  fit: string | null
  lowPriceFirst: boolean
}

export const NO_FILTERS: Filters = { price: null, material: null, fit: null, lowPriceFirst: false }

type MallRule = {
  queryParam: string
  applyPrice?: (url: URL, range: PriceRange) => void
  applyLowPriceFirst?: (url: URL) => void
}

const setMinMax = (url: URL, range: PriceRange) => {
  if (range.min != null) url.searchParams.set('minPrice', String(range.min))
  if (range.max != null) url.searchParams.set('maxPrice', String(range.max))
}

const MALL_RULES: Record<string, MallRule> = {
  NAVER_SHOPPING: {
    queryParam: 'query',
    applyPrice: setMinMax,
    applyLowPriceFirst: (url) => url.searchParams.set('sort', 'price_asc'),
  },
  // 11-17 실제 검색 화면에서 확인: 가격대(minPrice/maxPrice)와 낮은 가격순(sortCode) 모두 적용됨
  MUSINSA: {
    queryParam: 'keyword',
    applyPrice: setMinMax,
    applyLowPriceFirst: (url) => url.searchParams.set('sortCode', 'LOW_PRICE'),
  },
  COUPANG: {
    queryParam: 'q',
    applyPrice: (url, range) => {
      setMinMax(url, range)
      url.searchParams.set('priceRange', `${range.min ?? 0}~${range.max ?? 99999999}`)
    },
    applyLowPriceFirst: (url) => url.searchParams.set('sorter', 'salePriceAsc'),
  },
  GOOGLE_SHOPPING: {
    queryParam: 'q',
    applyPrice: (url, range) => {
      const parts = ['mr:1', 'price:1']
      if (range.min != null) parts.push(`ppr_min:${range.min}`)
      if (range.max != null) parts.push(`ppr_max:${range.max}`)
      url.searchParams.set('tbs', parts.join(','))
    },
  },
}

// 필터가 반영된 검색어 (화면에 보여주는 용도이기도 하다)
export function filteredQuery(query: string, filters: Filters): string {
  return [query, filters.material, filters.fit].filter(Boolean).join(' ')
}

export function applyFilters(link: MallLink, query: string, filters: Filters): string {
  const rule = MALL_RULES[link.mall]
  if (!rule) return link.url // 모르는 쇼핑몰이면 원래 링크 그대로

  const url = new URL(link.url)
  url.searchParams.set(rule.queryParam, filteredQuery(query, filters))
  if (filters.price) rule.applyPrice?.(url, filters.price)
  if (filters.lowPriceFirst) rule.applyLowPriceFirst?.(url)
  return url.toString()
}
