import { useEffect, useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { githubLogin } from '../services/api'
import { useAuth } from '../context/useAuth'

export default function GithubCallback() {
  const [error, setError] = useState('')
  const navigate = useNavigate()
  const location = useLocation()
  const { loginUser } = useAuth()

  useEffect(() => {
    const params = new URLSearchParams(location.search)
    const code = params.get('code')

    if (!code) {
      setError('No authorization code found.')
      return
    }

    githubLogin({ code })
      .then(res => {
        loginUser(res.data)
        navigate('/feed')
      })
      .catch(err => {
        console.error('GitHub login error:', err)
        setError('Failed to log in with GitHub.')
      })
  }, [location, navigate, loginUser])

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="bg-white p-8 rounded-2xl shadow-sm border border-gray-200 text-center">
          <p className="text-red-500 font-bold mb-4">{error}</p>
          <button onClick={() => navigate('/login')} className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm">
            Back to Login
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="flex flex-col items-center">
        <div className="w-10 h-10 border-4 border-blue-600 border-t-transparent rounded-full animate-spin mb-4"></div>
        <p className="text-gray-500 font-medium">Authenticating with GitHub...</p>
      </div>
    </div>
  )
}
