import React, { useEffect, useState } from 'react'
import api from '../api/api'
import useAuthStore from '../store/authStore'

export default function Billing() {
  const [bills, setBills] = useState([])
  const [message, setMessage] = useState(null)
  const user = useAuthStore(state => state.user)

  useEffect(() => {
    api.get('/billing').then(r => setBills(r.data)).catch(() => {})
  }, [])

  const generate = async (appointmentId) => {
    try {
      const res = await api.post(`/billing/generate/${appointmentId}`)
      setBills(prev => [res.data, ...prev])
      setMessage('Bill generated')
      setTimeout(() => setMessage(null), 3000)
    } catch (err) {
      setMessage('Failed to generate')
    }
  }

  const pay = async (id) => {
    try {
      const res = await api.put(`/billing/${id}/pay`)
      setBills(prev => prev.map(b => b.id === id ? res.data : b))
      setMessage('Payment recorded')
      setTimeout(() => setMessage(null), 3000)
    } catch (err) {
      setMessage('Failed to pay')
    }
  }

  return (
    <div className="space-y-6">
      <div className="rounded-3xl bg-white p-6 shadow-sm">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-semibold">Billing</h2>
          <div></div>
        </div>
        {message && <div className="mt-4 text-sm text-emerald-600">{message}</div>}
        <div className="mt-6">
          {user?.role === 'ADMIN' && (
            <div className="mb-4">
              <input placeholder="Appointment ID (after dispensing)" id="apptId" className="rounded-2xl border px-3 py-2 mr-2" />
              <button onClick={() => generate(document.getElementById('apptId').value)} className="rounded-2xl bg-cyan-500 px-4 py-2 text-white">Generate Bill</button>
              <div className="text-xs text-slate-500 mt-1">Only generate after medicines have been dispensed for this appointment.</div>
            </div>
          )}

          <div className="space-y-3">
            {bills.map(b => (
              <div key={b.id} className="rounded-2xl border p-4 flex items-center justify-between">
                <div>
                  <div className="text-sm text-slate-500">Appointment: {b.appointmentId}</div>
                  <div className="mt-1 font-semibold">Total: ${b.totalAmount?.toFixed(2)}</div>
                  <div className="text-sm text-slate-500">Consultation: ${b.consultationFee} • Medicine: ${b.medicineCost}</div>
                </div>
                <div className="flex gap-2 items-center flex-col">
                  <div className={`px-3 py-1 rounded-full text-xs ${(b.paymentStatus ? b.paymentStatus : b.status) === 'PAID' ? 'bg-emerald-100 text-emerald-800' : 'bg-yellow-100 text-yellow-800'}`}>{b.paymentStatus ? b.paymentStatus : b.status}</div>
                  {user?.role === 'ADMIN' && b.status !== 'PAID' && (
                    <button onClick={() => pay(b.id)} className="rounded-full bg-emerald-500 px-3 py-1 text-sm text-white">✓ Mark as Paid</button>
                  )}
                  {user?.role === 'PATIENT' && b.status !== 'PAID' && (
                    <div className="text-xs text-blue-600 font-semibold mt-1">💬 Pay at counter</div>
                  )}
                </div>
              </div>
            ))}
            {bills.length === 0 && <div className="text-slate-500">No bills yet.</div>}
          </div>
        </div>
      </div>
    </div>
  )
}
