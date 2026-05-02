import React, { useEffect, useState } from 'react'
import api from '../api/api'
import Table from '../components/Table'
import useAuthStore from '../store/authStore'

export default function Prescriptions() {
  const [prescriptions, setPrescriptions] = useState([])
  const [status, setStatus] = useState('Loading...')
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ appointmentId: '', medications: '', notes: '' })
  const [formError, setFormError] = useState(null)
  const [medicines, setMedicines] = useState([])
  const [addingMedicine, setAddingMedicine] = useState(null) // prescriptionId
  const [medForm, setMedForm] = useState({ medicineId: '', dosage: '', durationDays: '', instructions: '' })
  const user = useAuthStore(state => state.user)
  const canCreate = user?.role === 'DOCTOR' || user?.role === 'ADMIN'

  useEffect(() => {
    api.get('/prescriptions')
      .then(response => { setPrescriptions(response.data); setStatus('') })
      .catch(() => setStatus('Unable to load prescriptions'))
    if (canCreate) {
      api.get('/pharmacy/medicines').then(r => setMedicines(r.data)).catch(() => {})
    }
  }, [])

  const handleDownload = item => {
    const contents = `Prescription ID: ${item.id}\nDate: ${item.appointment?.appointmentTime || 'N/A'}\nPatient: ${item.appointment?.patient?.name || item.appointment?.patient?.email}\nDoctor: ${item.appointment?.doctor?.name || item.appointment?.doctor?.email}\n\nMedications:\n${item.medications}\n\nNotes:\n${item.notes || 'None'}`
    const blob = new Blob([contents], { type: 'text/plain;charset=utf-8' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `prescription-${item.id}.txt`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }

  const handleCreate = async (e) => {
    e.preventDefault()
    try {
      const payload = { ...form }
      if (!payload.appointmentId) return setFormError('Appointment ID is required')
      const res = await api.post('/prescriptions', payload)
      setPrescriptions(prev => [res.data, ...prev])
      setShowForm(false)
      setForm({ appointmentId: '', medications: '', notes: '' })
      setFormError(null)
    } catch (err) {
      setFormError(err?.response?.data?.message || 'Failed to create prescription')
    }
  }

  const handleAddMedicine = async (e) => {
    e.preventDefault()
    if (!medForm.medicineId) return
    try {
      await api.post(`/prescriptions/${addingMedicine}/medicines`, {
        medicineId: Number(medForm.medicineId),
        dosage: medForm.dosage || undefined,
        durationDays: medForm.durationDays ? Number(medForm.durationDays) : undefined,
        instructions: medForm.instructions || undefined,
      })
      setAddingMedicine(null)
      setMedForm({ medicineId: '', dosage: '', durationDays: '', instructions: '' })
    } catch (err) {
      alert(err?.response?.data?.message || 'Failed to add medicine')
    }
  }

  return (
    <div className="space-y-6">
      <div className="rounded-3xl bg-white p-6 shadow-sm">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-semibold">Prescriptions</h2>
            <p className="text-sm text-slate-500">View medication records and notes.</p>
          </div>
          {canCreate && (
            <button onClick={() => setShowForm(s => !s)} className="rounded-full bg-cyan-500 px-4 py-2 text-sm text-white">
              {showForm ? 'Cancel' : '+ New Prescription'}
            </button>
          )}
        </div>

        {showForm && (
          <form onSubmit={handleCreate} className="mt-4 space-y-3 max-w-md">
            {formError && <div className="text-sm text-red-600">{formError}</div>}
            <div>
              <label className="text-xs text-slate-500">Appointment ID *</label>
              <input required type="number" className="w-full rounded-xl border px-3 py-2 text-sm" value={form.appointmentId}
                onChange={e => setForm(f => ({ ...f, appointmentId: e.target.value }))} />
            </div>
            <div>
              <label className="text-xs text-slate-500">Medications</label>
              <textarea className="w-full rounded-xl border px-3 py-2 text-sm" rows={3} value={form.medications}
                onChange={e => setForm(f => ({ ...f, medications: e.target.value }))} />
            </div>
            <div>
              <label className="text-xs text-slate-500">Notes</label>
              <textarea className="w-full rounded-xl border px-3 py-2 text-sm" rows={2} value={form.notes}
                onChange={e => setForm(f => ({ ...f, notes: e.target.value }))} />
            </div>
            <button type="submit" className="rounded-full bg-cyan-500 px-4 py-2 text-sm text-white">Save</button>
          </form>
        )}
      </div>

      {status ? (
        <div className="rounded-3xl bg-white p-6 shadow-sm text-slate-500">{status}</div>
      ) : (
        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <Table
            columns={[
              { key: 'id', title: 'ID' },
              { key: 'patient', title: 'Patient', render: row => row.patient?.name || row.patient?.email || row.appointment?.patient?.name || '—' },
              { key: 'doctor', title: 'Doctor', render: row => row.doctor?.name || row.doctor?.email || row.appointment?.doctor?.name || '—' },
              { key: 'medications', title: 'Medications' },
              { key: 'notes', title: 'Notes' },
              {
                key: 'actions', title: 'Actions', render: row => (
                  <div className="flex gap-2">
                    <button onClick={() => handleDownload(row)} className="rounded-full bg-cyan-500 px-3 py-1 text-xs font-semibold text-white">Download</button>
                    {canCreate && (
                      <button onClick={() => setAddingMedicine(row.id)} className="rounded-full bg-emerald-500 px-3 py-1 text-xs font-semibold text-white">+ Medicine</button>
                    )}
                  </div>
                )
              },
            ]}
            data={prescriptions}
          />
        </div>
      )}

      {/* Add medicine modal */}
      {addingMedicine && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-3xl p-6 w-full max-w-md shadow-xl">
            <h3 className="text-lg font-semibold mb-3">Add Medicine to Prescription #{addingMedicine}</h3>
            <form onSubmit={handleAddMedicine} className="space-y-3">
              <div>
                <label className="text-xs text-slate-500">Medicine *</label>
                <select required className="w-full rounded-xl border px-3 py-2 text-sm" value={medForm.medicineId}
                  onChange={e => setMedForm(f => ({ ...f, medicineId: e.target.value }))}>
                  <option value="">— select —</option>
                  {medicines.map(m => <option key={m.id} value={m.id}>{m.name}</option>)}
                </select>
              </div>
              <div>
                <label className="text-xs text-slate-500">Dosage</label>
                <input className="w-full rounded-xl border px-3 py-2 text-sm" placeholder="e.g. 500mg twice daily" value={medForm.dosage}
                  onChange={e => setMedForm(f => ({ ...f, dosage: e.target.value }))} />
              </div>
              <div>
                <label className="text-xs text-slate-500">Duration (days)</label>
                <input type="number" className="w-full rounded-xl border px-3 py-2 text-sm" value={medForm.durationDays}
                  onChange={e => setMedForm(f => ({ ...f, durationDays: e.target.value }))} />
              </div>
              <div>
                <label className="text-xs text-slate-500">Instructions</label>
                <input className="w-full rounded-xl border px-3 py-2 text-sm" value={medForm.instructions}
                  onChange={e => setMedForm(f => ({ ...f, instructions: e.target.value }))} />
              </div>
              <div className="flex gap-3 justify-end">
                <button type="button" onClick={() => setAddingMedicine(null)} className="rounded-full bg-slate-200 px-4 py-2 text-sm">Cancel</button>
                <button type="submit" className="rounded-full bg-cyan-500 px-4 py-2 text-sm text-white">Add</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
