import React, { useEffect, useState } from 'react'
import { useNavigate, Link, useParams } from 'react-router-dom'
import api from '../api/api'
import useAuthStore from '../store/authStore'

const ROLE_CONFIG = {
  ADMIN: {
    badge: 'Admin Portal',
    title: 'Login as admin',
    description: 'Manage doctors, patients, appointments, and platform analytics.'
  },
  DOCTOR: {
    badge: 'Doctor Portal',
    title: 'Login as doctor',
    description: 'Review appointments, patients, and create digital prescriptions.'
  },
  PATIENT: {
    badge: 'Patient Portal',
    title: 'Login as patient',
    description: 'Book appointments and access your prescriptions securely.'
  }
}

export default function Login(){
  const { role: roleParam } = useParams()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const navigate = useNavigate()
  const setAuth = useAuthStore(state => state.setAuth)
  const user = useAuthStore(state => state.user)

  const selectedRole = ['ADMIN', 'DOCTOR', 'PATIENT'].includes((roleParam || '').toUpperCase())
    ? (roleParam || '').toUpperCase()
    : null
  const content = selectedRole
    ? ROLE_CONFIG[selectedRole]
    : {
        badge: 'Welcome back',
        title: 'Login to your account',
        description: 'Access your personalized healthcare dashboard.'
      }

  useEffect(() => {
    if (user?.role) {
      if (user.role === 'ADMIN') navigate('/admin-dashboard')
      else if (user.role === 'DOCTOR') navigate('/doctor-dashboard')
      else if (user.role === 'PATIENT') navigate('/patient-dashboard')
    }
  }, [user, navigate])

  const submit = async (e) => {
    e.preventDefault()
    setError(null)
    setIsSubmitting(true)
    const normalizedEmail = email.trim().toLowerCase()
    const normalizedPassword = password.trim()

    if (!normalizedEmail || !normalizedPassword) {
      setError('Email and password are required.')
      setIsSubmitting(false)
      return
    }

    try{
      const res = await api.post('/auth/login', { email: normalizedEmail, password: normalizedPassword })
      const { token, email: userEmail, name, role } = res.data

      if (selectedRole && role !== selectedRole) {
        setError(`This account is ${role}. Please use ${selectedRole} credentials.`)
        return
      }

      setAuth(token, { email: userEmail, name, role })
      if (role === 'ADMIN') navigate('/admin-dashboard')
      else if (role === 'DOCTOR') navigate('/doctor-dashboard')
      else navigate('/patient-dashboard')
    }catch(err){
      if (err?.response?.status === 401) {
        setError('Invalid email or password.')
      } else if (!err?.response) {
        setError('Cannot reach server. Make sure backend is running on port 8086.')
      } else {
        setError(err?.response?.data?.message || 'Login failed')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex items-center justify-center px-4 py-10">
      <div className="w-full max-w-md rounded-3xl bg-slate-900 p-10 shadow-2xl ring-1 ring-white/10">
        <div className="mb-8">
          <p className="text-sm uppercase tracking-[0.24em] text-cyan-400">{content.badge}</p>
          <h1 className="mt-4 text-3xl font-semibold">{content.title}</h1>
          <p className="mt-2 text-slate-400">{content.description}</p>
          <div className="mt-4 flex flex-wrap gap-2 text-xs">
            <Link to="/login/admin" className="rounded-full border border-slate-700 px-3 py-1 text-slate-300 hover:border-cyan-500 hover:text-cyan-300">Admin</Link>
            <Link to="/login/doctor" className="rounded-full border border-slate-700 px-3 py-1 text-slate-300 hover:border-cyan-500 hover:text-cyan-300">Doctor</Link>
            <Link to="/login/patient" className="rounded-full border border-slate-700 px-3 py-1 text-slate-300 hover:border-cyan-500 hover:text-cyan-300">Patient</Link>
          </div>
        </div>

        <form onSubmit={submit} className="space-y-5">
          <div>
            <label className="mb-2 block text-sm text-slate-300">Email</label>
            <input value={email} onChange={e => setEmail(e.target.value)} className="w-full rounded-2xl border border-slate-700 bg-slate-950 px-4 py-3 text-slate-100 outline-none focus:border-cyan-500" placeholder="name@company.com" />
          </div>
          <div>
            <label className="mb-2 block text-sm text-slate-300">Password</label>
            <input type="password" value={password} onChange={e => setPassword(e.target.value)} className="w-full rounded-2xl border border-slate-700 bg-slate-950 px-4 py-3 text-slate-100 outline-none focus:border-cyan-500" placeholder="••••••••" />
          </div>
          {error && <div className="rounded-2xl bg-red-500/10 px-4 py-3 text-sm text-red-200">{error}</div>}
          <button type="submit" disabled={isSubmitting} className="w-full rounded-2xl bg-cyan-500 px-5 py-3 text-sm font-semibold text-slate-950 transition hover:bg-cyan-400 disabled:cursor-not-allowed disabled:opacity-50">{isSubmitting ? 'Signing in...' : (selectedRole ? `Login as ${selectedRole.toLowerCase()}` : 'Login')}</button>
        </form>

        <div className="mt-6 text-center text-sm text-slate-500">
          Do not have an account? <Link to="/register" className="text-cyan-400 hover:text-cyan-300">Register now</Link>
        </div>
      </div>
    </div>
  )
}
