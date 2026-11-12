import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { deleteGarment, listGarments, type Garment } from '../api/garments'

// /closet : 내가 등록한 기준 옷 목록
function ClosetPage() {
  const [garments, setGarments] = useState<Garment[] | null>(null)

  useEffect(() => {
    listGarments().then(setGarments)
  }, [])

  const handleDelete = async (garment: Garment) => {
    if (!window.confirm(`'${garment.name}'을(를) 삭제할까요?`)) return
    await deleteGarment(garment.id)
    // 서버에서 지워졌으면 화면 목록에서도 뺀다 (목록을 다시 불러오지 않아도 됨)
    setGarments((prev) => prev?.filter((g) => g.id !== garment.id) ?? null)
  }

  return (
    <main className="page">
      <header className="header">
        <Link className="text-button" to="/">
          ← 홈
        </Link>
        <Link className="text-button strong" to="/closet/new">
          + 옷 등록
        </Link>
      </header>
      <h1>내 옷장</h1>
      <p className="sub">잘 맞는 옷의 실측이 사이즈 비교의 기준이 돼요.</p>

      {garments === null && <p>불러오는 중...</p>}
      {garments?.length === 0 && (
        <section className="card">
          <p>아직 등록한 옷이 없어요.</p>
          <Link className="button primary" to="/closet/new">
            첫 기준 옷 등록하기
          </Link>
        </section>
      )}

      {garments?.map((garment) => (
        <section className="card" key={garment.id}>
          <div className="header">
            <h2>{garment.name}</h2>
            <div className="actions">
              <Link className="text-button" to={`/closet/${garment.id}/edit`}>
                수정
              </Link>
              <button type="button" className="text-button danger" onClick={() => handleDelete(garment)}>
                삭제
              </button>
            </div>
          </div>
          <p className="sub">
            {garment.categoryLabel} · {garment.fitFeelLabel}
          </p>
          <ul className="chips">
            {garment.measurements.map((m) => (
              <li key={m.part}>
                {m.label} <strong>{m.valueCm}</strong>
              </li>
            ))}
          </ul>
        </section>
      ))}
    </main>
  )
}

export default ClosetPage
