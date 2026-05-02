import React, { useEffect, useState } from 'react'
import api from '../api/api'
import useAuthStore from '../store/authStore'

export default function DischargeSummaries() {
  const user = useAuthStore(state => state.user)
  const [summaries, setSummaries] = useState([])
  const [appointmentIdFilter, setAppointmentIdFilter] = useState('')
  const [message, setMessage] = useState(null)
  const [form, setForm] = useState({ appointmentId: '', summary: '', instructions: '', followUpDate: '' })

  const fetchSummaries = async (appointmentId) => {
    try {
      const query = appointmentId ? `?appointmentId=${appointmentId}` : ''
      const res = await api.get(`/discharge-summaries${query}`)
      setSummaries(res.data)
    } catch (err) {
      setSummaries([])
    }
  }

  useEffect(() => {
    fetchSummaries(appointmentIdFilter)
  }, [appointmentIdFilter])

  const createSummary = async () => {
    try {
      const payload = {
        appointmentId: Number(form.appointmentId),
        summary: form.summary,
        instructions: form.instructions,
        followUpDate: form.followUpDate || undefined
      }
      const res = await api.post('/discharge-summaries', payload)
      setSummaries(prev => [res.data, ...prev])
      setForm({ appointmentId: '', summary: '', instructions: '', followUpDate: '' })
      setMessage('Discharge summary saved')
      setTimeout(() => setMessage(null), 3000)
    } catch (err) {
      setMessage(err?.response?.data?.message || 'Failed to save summary')
    }
  }

  return (
    <div className="space-y-6">
      <div className="rounded-3xl bg-white p-6 shadow-sm">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-semibold">Discharge Summaries</h2>
          <div></div>
        </div>
        {message && <div className="mt-4 text-sm text-emerald-600">{message}</div>}
        {user?.role !== 'PATIENT' && (
          <div className="mt-6 grid gap-4 md:grid-cols-2">
            <div>
              <label className="block text-sm text-slate-500">Appointment ID</label>
              <input className="w-full rounded-2xl border px-4 py-2" value={form.appointmentId} onChange={e => setForm({...form, appointmentId: e.target.value})} />
              <label className="block text-sm text-slate-500 mt-2">Summary</label>
              <textarea className="w-full rounded-2xl border px-4 py-2" value={form.summary} onChange={e => setForm({...form, summary: e.target.value})} />
              <label className="block text-sm text-slate-500 mt-2">Instructions</label>
              <textarea className="w-full rounded-2xl border px-4 py-2" value={form.instructions} onChange={e => setForm({...form, instructions: e.target.value})} />
              <label className="block text-sm text-slate-500 mt-2">Follow-up Date</label>
              <input type="date" className="w-full rounded-2xl border px-4 py-2" value={form.followUpDate} onChange={e => setForm({...form, followUpDate: e.target.value})} />
              <div className="mt-4">
                <button onClick={createSummary} className="rounded-2xl bg-cyan-500 px-4 py-2 text-white">Save Discharge Summary</button>
              </div>
            </div>
            <div>
              <label className="block text-sm text-slate-500">Filter by Appointment ID</label>
              <input className="w-full rounded-2xl border px-4 py-2" value={appointmentIdFilter} onChange={e => setAppointmentIdFilter(e.target.value)} placeholder="Leave empty for all" />
              <div className="text-xs text-slate-500 mt-2">Anyone can browse summaries by appointment.</div>
            </div>
          </div>
        )}

        <div className="mt-6 space-y-3">
          {summaries.map(summary => (
            <div key={summary.id} className="rounded-2xl border p-4">
              <div className="font-semibold">Appointment: {summary.appointmentId}</div>
              <div className="text-sm text-slate-500">Patient: {summary.patientId} • Doctor: {summary.doctorId}</div>
              <div className="mt-2 text-slate-700">{summary.summary}</div>
              {summary.instructions && <div className="mt-2 text-slate-600">Instructions: {summary.instructions}</div>}
              {summary.followUpDate && <div className="mt-2 text-sm text-slate-500">Follow-up: {summary.followUpDate}</div>}
              <div className="mt-2 text-xs text-slate-400">Created at: {summary.createdAt}</div>
            </div>
          ))}
          {summaries.length === 0 && <div className="text-slate-500">No discharge summaries available.</div>}
        </div>
      </div>
    </div>
  )
}
