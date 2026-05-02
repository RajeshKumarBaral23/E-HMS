import React, { useEffect, useState } from 'react'
import api from '../api/api'
import WeeklyAvailability from './WeeklyAvailability'

export default function CreateDoctorModal({ open, onClose, onCreated }) {
  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    specialization: '',
    phone: '',
    bio: '',
    departmentId: '',
    availabilityStartDateTime: '',
    availabilityEndDateTime: '',
    availability: []
  })
  const [departments, setDepartments] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (!open) return
    api.get('/departments')
      .then(resp => setDepartments(resp.data || []))
      .catch(() => setDepartments([]))
  }, [open])

  function handleChange(e) {
    const { name, value } = e.target
    setForm(f => ({ ...f, [name]: value }))
  }

  function handleSubmit(e) {
    e.preventDefault()
    setLoading(true)
    setError(null)
    const payload = { ...form }
    if (!payload.departmentId) delete payload.departmentId
    if (!payload.password) delete payload.password
    // remove legacy fields
    if (!payload.availabilityStartDateTime) delete payload.availabilityStartDateTime
    if (!payload.availabilityEndDateTime) delete payload.availabilityEndDateTime
    if (!payload.availability || payload.availability.length === 0) delete payload.availability

    api.post('/doctors', payload)
      .then(resp => {
        setLoading(false)
        onCreated && onCreated(resp.data)
        onClose && onClose()
      })
      .catch(err => {
        setLoading(false)
        const raw = err?.response?.data
        setError(typeof raw === 'object' ? (raw?.message || raw?.error || JSON.stringify(raw)) : (raw || 'Unable to create doctor'))
      })
  }

  if (!open) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="fixed inset-0 bg-black/40" onClick={onClose} />
      <div className="bg-white rounded-2xl p-6 w-full max-w-2xl z-50 shadow-lg">
        <h3 className="text-xl font-semibold mb-4">Create Doctor</h3>
        {error && <div className="text-sm text-red-600 mb-2">{String(error)}</div>}
        <form onSubmit={handleSubmit} className="space-y-3">
          <div className="grid grid-cols-2 gap-3">
            <input name="name" value={form.name} onChange={handleChange} placeholder="Full name" required className="w-full border rounded p-2" />
            <input name="email" type="email" value={form.email} onChange={handleChange} placeholder="Email" required className="w-full border rounded p-2" />
            <input name="password" type="password" value={form.password} onChange={handleChange} placeholder="Password (optional)" className="w-full border rounded p-2" />
            <select name="departmentId" value={form.departmentId} onChange={handleChange} className="w-full border rounded p-2">
              <option value="">No department</option>
              {departments.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <input name="specialization" value={form.specialization} onChange={handleChange} placeholder="Specialization" className="w-full border rounded p-2" />
            <input name="phone" value={form.phone} onChange={handleChange} placeholder="Phone" className="w-full border rounded p-2" />
          </div>

          <div>
            <textarea name="bio" value={form.bio} onChange={handleChange} placeholder="Short bio" className="w-full border rounded p-2" />
          </div>

          <div>
            <WeeklyAvailability value={form.availability} onChange={v => setForm(f => ({ ...f, availability: v }))} />
          </div>

          <div className="flex justify-end gap-2 mt-3">
            <button type="button" onClick={onClose} className="px-4 py-2 rounded-full border">Cancel</button>
            <button type="submit" disabled={loading} className="px-4 py-2 rounded-full bg-cyan-500 text-white">{loading ? 'Creating...' : 'Create'}</button>
          </div>
        </form>
      </div>
    </div>
  )
}
