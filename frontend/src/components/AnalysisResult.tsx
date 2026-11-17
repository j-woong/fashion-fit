import { useState } from 'react'
import type { GarmentAnalysisResult } from '../api/analysis'
import {
  applyFilters,
  filteredQuery,
  FIT_KEYWORDS,
  NO_FILTERS,
  PRICE_RANGES,
  type Filters,
} from '../lib/shoppingFilters'

type ChipProps = { on: boolean; onClick: () => void; children: string }

function Chip({ on, onClick, children }: ChipProps) {
  return (
    <button type="button" className={`chip-button ${on ? 'on' : ''}`} aria-pressed={on} onClick={onClick}>
      {children}
    </button>
  )
}

// 스크린샷 분석 결과: 옷 특징 + 필터 달린 쇼핑몰 검색 링크 + 살 때 확인할 점
function AnalysisResult({ result }: { result: GarmentAnalysisResult }) {
  const [filters, setFilters] = useState<Filters>(NO_FILTERS)

  // 같은 칩을 다시 누르면 해제
  const toggle = <K extends keyof Filters>(key: K, value: Filters[K]) =>
    setFilters((prev) => ({ ...prev, [key]: prev[key] === value ? NO_FILTERS[key] : value }))

  return (
    <>
      <section className="card">
        <h2>{result.itemName}</h2>
        <p className="sub">{result.categoryLabel ?? '사이즈 비교 대상이 아닌 종류예요'}</p>
        <p>{result.summary}</p>
        <dl className="facts">
          {result.color && (
            <div>
              <dt>색</dt>
              <dd>{result.color}</dd>
            </div>
          )}
          {result.fit && (
            <div>
              <dt>사진 속 핏</dt>
              <dd>{result.fit}</dd>
            </div>
          )}
          {result.materialGuess && (
            <div>
              <dt>소재 (추정)</dt>
              <dd>{result.materialGuess}</dd>
            </div>
          )}
          {result.textureGuess && (
            <div>
              <dt>촉감 (추정)</dt>
              <dd>{result.textureGuess}</dd>
            </div>
          )}
        </dl>
        {result.details.length > 0 && (
          <ul className="chips">
            {result.details.map((d) => (
              <li key={d}>{d}</li>
            ))}
          </ul>
        )}
        <p className="muted">소재와 촉감은 사진만 보고 추정한 값이에요. 정확한 혼용률은 상품 페이지에서 확인하세요.</p>
      </section>

      <section className="card">
        <h2>비슷한 옷 찾기</h2>

        <div className="filter-row">
          <span className="filter-name">가격대</span>
          {PRICE_RANGES.map((range) => (
            <Chip key={range.label} on={filters.price === range} onClick={() => toggle('price', range)}>
              {range.label}
            </Chip>
          ))}
        </div>
        {result.materialKeywords.length > 0 && (
          <div className="filter-row">
            <span className="filter-name">소재</span>
            {result.materialKeywords.map((m) => (
              <Chip key={m} on={filters.material === m} onClick={() => toggle('material', m)}>
                {m}
              </Chip>
            ))}
          </div>
        )}
        <div className="filter-row">
          <span className="filter-name">핏</span>
          {FIT_KEYWORDS.map((f) => (
            <Chip key={f} on={filters.fit === f} onClick={() => toggle('fit', f)}>
              {f}
            </Chip>
          ))}
        </div>
        <div className="filter-row">
          <span className="filter-name">정렬</span>
          <Chip on={filters.lowPriceFirst} onClick={() => toggle('lowPriceFirst', true)}>
            낮은 가격순
          </Chip>
        </div>
        <p className="muted">
          필터를 고르면 아래 링크에 바로 반영돼요. 쇼핑몰에 따라 가격대·정렬이 적용되지 않을 수 있어요.
        </p>

        {result.searches.map((s) => (
          <div key={s.query} className="search-group">
            <p>
              <strong>{filteredQuery(s.query, filters)}</strong>
            </p>
            <div className="mall-links">
              {s.links.map((l) => (
                <a key={l.mall} href={applyFilters(l, s.query, filters)} target="_blank" rel="noreferrer">
                  {l.label}
                </a>
              ))}
            </div>
          </div>
        ))}
      </section>

      {result.qualityChecks.length > 0 && (
        <section className="card">
          <h2>살 때 확인할 점</h2>
          <ul className="checklist">
            {result.qualityChecks.map((q) => (
              <li key={q}>{q}</li>
            ))}
          </ul>
        </section>
      )}
    </>
  )
}

export default AnalysisResult
