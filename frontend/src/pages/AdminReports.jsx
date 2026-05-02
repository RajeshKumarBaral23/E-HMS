import React, { useEffect, useState } from 'react'
import api from '../api/api'

export default function AdminReports() {
  const [patients, setPatients] = useState(null)
  const [doctors, setDoctors] = useState(null)
  const [appointments, setAppointments] = useState({})
  const [revenue, setRevenue] = useState(null)

  useEffect(() => {
    api.get('/admin/analytics/total-patients').then(r => setPatients(r.data.totalPatients)).catch(() => {})
    api.get('/admin/analytics/total-doctors').then(r => setDoctors(r.data.totalDoctors)).catch(() => {})
    api.get('/admin/analytics/appointments-per-day?days=7').then(r => setAppointments(r.data)).catch(() => {})
    api.get('/admin/analytics/revenue-summary?days=30').then(r => setRevenue(r.data.revenue)).catch(() => {})
  }, [])

  const exportCsv = async () => {
    try {
      const res = await api.get('/admin/reports/export', { responseType: 'blob' })
      const url = URL.createObjectURL(new Blob([res.data], { type: 'text/csv' }))
      const a = document.createElement('a')
      a.href = url
      a.download = 'appointments-export.csv'
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
    } catch {
      alert('Export failed')
    }
  }

  return (
    <div className="space-y-6">
      <div className="rounded-3xl bg-white p-6 shadow-sm">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-semibold">Admin Reports</h2>
          <button onClick={exportCsv} className="rounded-full bg-cyan-500 px-4 py-2 text-sm text-white">Export CSV</button>
        </div>
        <div className="mt-4 grid grid-cols-3 gap-4">
          <div className="p-4 bg-slate-50 rounded-2xl">Total patients: <div className="font-bold">{patients ?? '–'}</div></div>
          <div className="p-4 bg-slate-50 rounded-2xl">Total doctors: <div className="font-bold">{doctors ?? '–'}</div></div>
          <div className="p-4 bg-slate-50 rounded-2xl">Revenue (30d): <div className="font-bold">${revenue?.toFixed(2) ?? '–'}</div></div>
        </div>

        <div className="mt-6">
          <h3 className="text-lg font-medium">Appointments (last 7 days)</h3>
          <div className="mt-3 space-y-2">
            {Object.keys(appointments).map(d => (
              <div key={d} className="flex justify-between text-sm">
                <div>{d}</div>
                <div>{appointments[d]}</div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
