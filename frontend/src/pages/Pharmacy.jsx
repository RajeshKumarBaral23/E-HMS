import React, { useEffect, useState } from 'react'
import api from '../api/api'
import useAuthStore from '../store/authStore'

export default function Pharmacy() {
  const user = useAuthStore(state => state.user)
  const [medicines, setMedicines] = useState([])
  const [form, setForm] = useState({ name: '', quantity: 0, price: 0, description: '' })
  const [message, setMessage] = useState(null)
  // inline edit/purchase state keyed by medicine id
  const [editing, setEditing] = useState({}) // { [id]: newQty }
  const [purchasing, setPurchasing] = useState({}) // { [id]: { qty, appointmentId } }

  useEffect(() => {
    api.get('/pharmacy/medicines').then(r => setMedicines(r.data)).catch(() => {})
  }, [])

  const notify = (msg) => { setMessage(msg); setTimeout(() => setMessage(null), 3000) }

  const addMedicine = async () => {
    try {
      const res = await api.post('/pharmacy/medicines', form)
      setMedicines(prev => [res.data, ...prev])
      setForm({ name: '', quantity: 0, price: 0, description: '' })
      notify('Medicine added')
    } catch (err) {
      notify('Failed to add')
    }
  }

  const updateStock = async (id) => {
    const m = medicines.find(x => x.id === id)
    const newQty = editing[id] !== undefined ? editing[id] : m.quantity
    try {
      const res = await api.put(`/pharmacy/medicines/${id}`, { ...m, quantity: Number(newQty) })
      setMedicines(prev => prev.map(x => x.id === id ? res.data : x))
      setEditing(e => { const n = { ...e }; delete n[id]; return n })
      notify('Stock updated')
    } catch (err) {
      notify('Failed to update stock')
    }
  }

  const purchase = async (id) => {
    const state = purchasing[id] || {}
    const qty = state.qty || 1
    const appointmentId = state.appointmentId || ''
    try {
      const query = appointmentId ? `?medicineId=${id}&quantity=${qty}&appointmentId=${appointmentId}` : `?medicineId=${id}&quantity=${qty}`
      await api.post(`/pharmacy/purchase${query}`)
      const res = await api.get('/pharmacy/medicines')
      setMedicines(res.data)
      setPurchasing(p => { const n = { ...p }; delete n[id]; return n })
      notify('Purchase completed')
    } catch (err) {
      notify(err?.response?.data?.message || 'Purchase failed')
    }
  }

  return (
    <div className="space-y-6">
      <div className="rounded-3xl bg-white p-6 shadow-sm">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-semibold">Pharmacy</h2>
        </div>
        {message && <div className="mt-4 text-sm text-emerald-600">{message}</div>}
        <div className="mt-6 grid gap-4 md:grid-cols-2">
          {user?.role === 'ADMIN' && (
            <div>
              <label className="block text-sm text-slate-500">Name</label>
              <input className="w-full rounded-2xl border px-4 py-2" value={form.name} onChange={e => setForm({...form, name: e.target.value})} />
              <label className="block text-sm text-slate-500 mt-2">Quantity</label>
              <input type="number" className="w-full rounded-2xl border px-4 py-2" value={form.quantity} onChange={e => setForm({...form, quantity: Number(e.target.value)})} />
              <label className="block text-sm text-slate-500 mt-2">Price</label>
              <input type="number" className="w-full rounded-2xl border px-4 py-2" value={form.price} onChange={e => setForm({...form, price: Number(e.target.value)})} />
              <label className="block text-sm text-slate-500 mt-2">Description</label>
              <textarea className="w-full rounded-2xl border px-4 py-2" value={form.description} onChange={e => setForm({...form, description: e.target.value})} />
              <div className="mt-4">
                <button onClick={addMedicine} className="rounded-2xl bg-cyan-500 px-4 py-2 text-white">Add Medicine</button>
              </div>
            </div>
          )}

          <div>
            <h3 className="text-lg font-semibold">Medicines</h3>
            <div className="mt-4 space-y-3">
              {medicines.map(m => (
                <div key={m.id} className="rounded-2xl border p-3">
                  <div className="flex items-center justify-between">
                    <div>
                      <div className="font-semibold">{m.name} — ${m.price}</div>
                      <div className="text-sm text-slate-500">Qty: {m.quantity}</div>
                      <div className="text-sm text-slate-500">{m.description}</div>
                    </div>
                    <div className="flex gap-2">
                      {user?.role === 'ADMIN' && (
                        <button
                          onClick={() => setEditing(e => ({ ...e, [m.id]: e[m.id] !== undefined ? undefined : m.quantity }))}
                          className="rounded-full bg-slate-200 px-3 py-1 text-sm"
                        >
                          Update
                        </button>
                      )}
                      {user?.role === 'PATIENT' && (
                        <button
                          onClick={() => setPurchasing(p => ({ ...p, [m.id]: p[m.id] ? undefined : { qty: 1, appointmentId: '' } }))}
                          className="rounded-full bg-emerald-500 px-3 py-1 text-sm text-white"
                        >
                          Buy
                        </button>
                      )}
                    </div>
                  </div>

                  {/* ADMIN inline update stock form */}
                  {user?.role === 'ADMIN' && editing[m.id] !== undefined && (
                    <div className="mt-2 flex items-center gap-2">
                      <label className="text-xs text-slate-500">New qty:</label>
                      <input
                        type="number"
                        className="w-24 rounded border px-2 py-1 text-sm"
                        value={editing[m.id]}
                        onChange={e => setEditing(ed => ({ ...ed, [m.id]: Number(e.target.value) }))}
                      />
                      <button onClick={() => updateStock(m.id)} className="rounded-full bg-cyan-500 px-3 py-1 text-xs text-white">Save</button>
                      <button onClick={() => setEditing(ed => { const n = { ...ed }; delete n[m.id]; return n })} className="rounded-full bg-slate-200 px-3 py-1 text-xs">Cancel</button>
                    </div>
                  )}

                  {/* PATIENT inline purchase form */}
                  {user?.role === 'PATIENT' && purchasing[m.id] && (
                    <div className="mt-2 space-y-1">
                      <div className="flex items-center gap-2">
                        <label className="text-xs text-slate-500 w-28">Quantity:</label>
                        <input
                          type="number"
                          className="w-20 rounded border px-2 py-1 text-sm"
                          value={purchasing[m.id].qty}
                          min={1}
                          onChange={e => setPurchasing(p => ({ ...p, [m.id]: { ...p[m.id], qty: Number(e.target.value) } }))}
                        />
                      </div>
                      <div className="flex items-center gap-2">
                        <label className="text-xs text-slate-500 w-28">Appointment ID:</label>
                        <input
                          type="number"
                          className="w-28 rounded border px-2 py-1 text-sm"
                          value={purchasing[m.id].appointmentId}
                          placeholder="optional"
                          onChange={e => setPurchasing(p => ({ ...p, [m.id]: { ...p[m.id], appointmentId: e.target.value } }))}
                        />
                      </div>
                      <div className="flex gap-2 mt-1">
                        <button onClick={() => purchase(m.id)} className="rounded-full bg-emerald-500 px-3 py-1 text-xs text-white">Confirm Purchase</button>
                        <button onClick={() => setPurchasing(p => { const n = { ...p }; delete n[m.id]; return n })} className="rounded-full bg-slate-200 px-3 py-1 text-xs">Cancel</button>
                      </div>
                    </div>
                  )}
                </div>
              ))}
              {medicines.length === 0 && <div className="text-slate-500">No medicines available.</div>}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
