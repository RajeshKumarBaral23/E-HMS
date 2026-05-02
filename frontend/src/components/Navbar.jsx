import React from 'react'
import { Link, useNavigate } from 'react-router-dom'
import useAuthStore from '../store/authStore'

export default function Navbar(){
  const logout = useAuthStore(state => state.logout)
  const user = useAuthStore(state => state.user)
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <header className="bg-white border-b p-4 flex flex-wrap items-center justify-between gap-4">
      <div className="flex items-center gap-4">
        <Link to="/" className="text-lg font-semibold text-slate-900">E-Healthcare</Link>
        <div className="rounded-2xl bg-slate-100 px-3 py-1 text-sm text-slate-600">{user?.role || 'Guest'}</div>
      </div>
      <div className="flex items-center gap-4">
        <span className="text-sm text-slate-700">{user?.name || 'Anonymous'}</span>
        <button onClick={handleLogout} className="rounded-2xl bg-red-500 px-4 py-2 text-sm font-semibold text-white hover:bg-red-600">Logout</button>
      </div>
    </header>
  )
}
