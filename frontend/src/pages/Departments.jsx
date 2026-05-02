import React, { useEffect, useState } from 'react'
import api from '../api/api'
import useAuthStore from '../store/authStore'

export default function Departments() {
  const [departments, setDepartments] = useState([])
  const [form, setForm] = useState({ name: '', description: '' })
  const [message, setMessage] = useState(null)
  const [error, setError] = useState(null)
  const user = useAuthStore(state => state.user)
  const isAdmin = user?.role === 'ADMIN'

  useEffect(() => { fetchDepartments() }, [])

  const fetchDepartments = () => {
    api.get('/departments').then(r => setDepartments(r.data)).catch(() => setError('Unable to load departments'))
  }

  const notify = (msg, isError = false) => {
    if (isError) setError(msg); else setMessage(msg)
    setTimeout(() => { setMessage(null); setError(null) }, 3500)
  }

  const handleCreate = async (e) => {
    e.preventDefault()
    if (!form.name.trim()) return
    try {
      const res = await api.post('/departments', form)
      setDepartments(prev => [res.data, ...prev])
      setForm({ name: '', description: '' })
      notify('Department created')
    } catch (err) {
      notify(err?.response?.data?.message || 'Failed to create department', true)
    }
  }

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Delete department "${name}"? This cannot be undone.`)) return
    try {
      await api.delete(`/departments/${id}`)
      setDepartments(prev => prev.filter(d => d.id !== id))
      notify('Department deleted')
    } catch (err) {
      notify(err?.response?.data?.message || err?.response?.data || 'Cannot delete department', true)
    }
  }

  return (
    <div className="space-y-6">
      <div className="rounded-3xl bg-white p-6 shadow-sm">
        <h2 className="text-2xl font-semibold">Departments</h2>
        <p className="text-sm text-slate-500">Manage hospital departments.</p>
      </div>

      {isAdmin && (
        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <h3 className="text-lg font-semibold mb-3">Create Department</h3>
          {message && <div className="text-sm text-emerald-600 mb-2">{message}</div>}
          {error && <div className="text-sm text-red-600 mb-2">{error}</div>}
          <form onSubmit={handleCreate} className="flex flex-wrap gap-3 items-end">
            <div className="flex flex-col">
              <label className="text-xs text-slate-500 mb-1">Name *</label>
              <input
                value={form.name}
                onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                placeholder="e.g., Cardiology"
                required
                className="rounded-xl border px-3 py-2 text-sm w-56"
              />
            </div>
            <div className="flex flex-col">
              <label className="text-xs text-slate-500 mb-1">Description</label>
              <input
                value={form.description}
                onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
                placeholder="Optional description"
                className="rounded-xl border px-3 py-2 text-sm w-64"
              />
            </div>
            <button type="submit" className="px-4 py-2 rounded-full bg-cyan-500 text-white text-sm">
              Create
            </button>
          </form>
        </div>
      )}

      <div className="rounded-3xl bg-white p-6 shadow-sm">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b text-left text-slate-500">
              <th className="pb-2 pr-4">ID</th>
              <th className="pb-2 pr-4">Name</th>
              <th className="pb-2 pr-4">Description</th>
              {isAdmin && <th className="pb-2">Actions</th>}
            </tr>
          </thead>
          <tbody>
            {departments.map(d => (
              <tr key={d.id} className="border-b last:border-0 hover:bg-slate-50">
                <td className="py-2 pr-4">{d.id}</td>
                <td className="py-2 pr-4 font-medium">{d.name}</td>
                <td className="py-2 pr-4 text-slate-500">{d.description || '—'}</td>
                {isAdmin && (
                  <td className="py-2">
                    <button
                      onClick={() => handleDelete(d.id, d.name)}
                      className="rounded-full bg-red-100 text-red-700 px-3 py-1 text-xs hover:bg-red-200"
                    >
                      Delete
                    </button>
                  </td>
                )}
              </tr>
            ))}
            {departments.length === 0 && (
              <tr><td colSpan={4} className="py-4 text-slate-500">No departments found.</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
