import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router'
import { getBodyProfile, type BodyProfile } from '../api/bodyProfile'
import BodyProfilePage from './BodyProfilePage'

// /body-profile : 저장된 값을 불러와서 수정 화면(BodyProfilePage)에 채워 준다
function BodyProfileEditPage() {
  const navigate = useNavigate()
  const [profile, setProfile] = useState<BodyProfile | null | undefined>(undefined)

  useEffect(() => {
    getBodyProfile().then(setProfile)
  }, [])

  if (profile === undefined) {
    return <main className="page">불러오는 중...</main>
  }
  return (
    <BodyProfilePage
      mode="edit"
      initial={profile}
      onSaved={() => navigate('/')}
      onCancel={() => navigate('/')}
    />
  )
}

export default BodyProfileEditPage
