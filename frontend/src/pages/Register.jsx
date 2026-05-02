import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/api'

export default function Register() {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [sex, setSex] = useState('')
  const [age, setAge] = useState('')
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(null)
  const navigate = useNavigate()

  const submit = async event => {
    event.preventDefault()
    setError(null)
    setSuccess(null)

    try {
      const body = { name, email, password }
      if (sex) body.sex = sex
      if (age) body.age = Number(age)
      await api.post('/auth/register', body)
      setSuccess('Account created successfully. You can now login.')
      setName('')
      setEmail('')
      setPassword('')
      window.setTimeout(() => navigate('/login/patient'), 1200)
    } catch (err) {
      setError(err?.response?.data?.message || 'Registration failed')
    }
  }

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex items-center justify-center px-4 py-10">
      <div className="w-full max-w-lg rounded-3xl bg-slate-900 p-10 shadow-2xl ring-1 ring-white/10">
        <div className="mb-8">
          <p className="text-sm uppercase tracking-[0.3em] text-cyan-400">Create your account</p>
          <h1 className="mt-4 text-3xl font-semibold">Register for E-Healthcare</h1>
          <p className="mt-2 text-slate-400">Secure access for patients, doctors, and admins.</p>
        </div>

        <form onSubmit={submit} className="space-y-5">
          <div>
            <label className="mb-2 block text-sm text-slate-300">Full name</label>
            <input value={name} onChange={e => setName(e.target.value)} className="w-full rounded-2xl border border-slate-700 bg-slate-950 px-4 py-3 text-slate-100 outline-none focus:border-cyan-500" placeholder="Jane Doe" />
          </div>
          <div>
            <label className="mb-2 block text-sm text-slate-300">Email</label>
            <input value={email} onChange={e => setEmail(e.target.value)} className="w-full rounded-2xl border border-slate-700 bg-slate-950 px-4 py-3 text-slate-100 outline-none focus:border-cyan-500" placeholder="name@company.com" />
          </div>
          <div>
            <label className="mb-2 block text-sm text-slate-300">Password</label>
            <input type="password" value={password} onChange={e => setPassword(e.target.value)} className="w-full rounded-2xl border border-slate-700 bg-slate-950 px-4 py-3 text-slate-100 outline-none focus:border-cyan-500" placeholder="••••••••" />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="mb-2 block text-sm text-slate-300">Sex (optional)</label>
              <select value={sex} onChange={e => setSex(e.target.value)} className="w-full rounded-2xl border border-slate-700 bg-slate-950 px-4 py-3 text-slate-100 outline-none focus:border-cyan-500">
                <option value="">Prefer not to say</option>
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
            <div>
              <label className="mb-2 block text-sm text-slate-300">Age (optional)</label>
              <input type="number" min="0" value={age} onChange={e => setAge(e.target.value)} className="w-full rounded-2xl border border-slate-700 bg-slate-950 px-4 py-3 text-slate-100 outline-none focus:border-cyan-500" placeholder="e.g. 34" />
            </div>
          </div>
          {error && <div className="rounded-xl bg-red-500/10 px-4 py-3 text-sm text-red-200">{error}</div>}
          {success && <div className="rounded-xl bg-emerald-500/10 px-4 py-3 text-sm text-emerald-200">{success}</div>}
          <button type="submit" className="w-full rounded-2xl bg-cyan-500 px-5 py-3 text-sm font-semibold text-slate-950 transition hover:bg-cyan-400">Create Account</button>
        </form>
      </div>
    </div>
  )
}
