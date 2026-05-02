import React, { useEffect, useMemo, useState } from 'react'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts'
import api from '../api/api'
import StatCard from '../components/StatCard'
import Table from '../components/Table'

export default function DoctorDashboard() {
  const [appointments, setAppointments] = useState([])
  const [prescriptions, setPrescriptions] = useState([])
  const [availabilitySlots, setAvailabilitySlots] = useState([])
  const [showModal, setShowModal] = useState(false)
  const [showAvailabilityModal, setShowAvailabilityModal] = useState(false)
  const [showEditAvailabilityModal, setShowEditAvailabilityModal] = useState(false)
  const [selectedAppt, setSelectedAppt] = useState(null)
  const [medications, setMedications] = useState('')
  const [notes, setNotes] = useState('')
  const [message, setMessage] = useState(null)
  const [isAvailable, setIsAvailable] = useState(true)
  const [availabilityForm, setAvailabilityForm] = useState({
    startDate: '',
    startTime: '08:00',
    endDate: '',
    endTime: '18:00'
  })
  const [editingSlot, setEditingSlot] = useState(null)

  useEffect(() => {
    api.get('/appointments/today').then(r => setAppointments(r.data)).catch(() => {})
    api.get('/prescriptions').then(r => setPrescriptions(r.data)).catch(() => {})
    fetchAvailabilitySlots()

    const token = localStorage.getItem('token')
    if (!token) return
    const streamUrl = `${api.defaults.baseURL}/appointments/stream?token=${token}`
    const es = new EventSource(streamUrl)
    es.addEventListener('appointment', e => {
      try {
        const appt = JSON.parse(e.data)
        setAppointments(prev => prev.map(a => a.id === appt.id ? appt : a))
      } catch (err) {}
    })
    es.onerror = () => es.close()
    return () => es.close()
  }, [])

  const fetchAvailabilitySlots = async () => {
    try {
      const res = await api.get('/availability')
      setAvailabilitySlots(res.data || [])
    } catch (err) {
      console.error('Error fetching availability:', err)
    }
  }

  const todaysAppointments = useMemo(() => appointments.filter(appt => appt.appointmentTime?.startsWith(new Date().toISOString().slice(0, 10))), [appointments])
  const upcomingCount = todaysAppointments.length
  const patients = useMemo(() => {
    const map = new Map()
    appointments.forEach(appt => {
      if (appt.patient?.id && !map.has(appt.patient.id)) {
        map.set(appt.patient.id, appt.patient)
      }
    })
    return Array.from(map.values())
  }, [appointments])
  const patientCount = patients.length
  const prescriptionCount = prescriptions.length

  const [actionMsg, setActionMsg] = useState(null)

  // Week-wise bar chart data for approved appointments
  const weekBarData = useMemo(() => {
    // Only CONFIRMED appointments
    const confirmed = appointments.filter(a => a.status === 'CONFIRMED' && a.appointmentTime)
    // Map: weekday (0=Sun) -> count
    const map = [0,0,0,0,0,0,0]
    confirmed.forEach(a => {
      const d = new Date(a.appointmentTime)
      if (!isNaN(d)) map[d.getDay()]++
    })
    const days = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat']
    return days.map((name, i) => ({ day: name, count: map[i] }))
  }, [appointments])
  const approveAppointment = async (appt) => {
    try {
      await api.put(`/appointments/${appt.id}/status`, { status: 'CONFIRMED' })
      setAppointments(prev => prev.map(a => a.id === appt.id ? { ...a, status: 'CONFIRMED' } : a))
      setActionMsg('Appointment approved')
      setTimeout(() => setActionMsg(null), 2000)
    } catch {
      setActionMsg('Failed to approve')
      setTimeout(() => setActionMsg(null), 2000)
    }
  }
  const appointmentColumns = [
    { key: 'id', title: 'ID' },
    { key: 'queueNumber', title: 'Serial', render: row => row.queueNumber || '-' },
    { key: 'patient', title: 'Patient', render: row => row.patient?.name || row.patient?.email },
    { key: 'appointmentTime', title: 'Time' },
    { key: 'status', title: 'Status', render: row => <span className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${{
      PENDING: 'bg-slate-100 text-slate-700',
      CONFIRMED: 'bg-emerald-100 text-emerald-700',
      CHECKED_IN: 'bg-cyan-100 text-cyan-700',
      IN_PROGRESS: 'bg-yellow-100 text-yellow-700',
      COMPLETED: 'bg-emerald-200 text-emerald-900',
      CANCELLED: 'bg-red-100 text-red-700',
    }[row.status] || 'bg-slate-100 text-slate-700'}`}>{row.status || 'PENDING'}</span> },
    { key: 'actions', title: 'Actions', render: row => (
      <div className="flex gap-2">
        {row.status === 'PENDING' && (
          <button onClick={() => approveAppointment(row)} className="rounded bg-emerald-500 px-3 py-1 text-xs text-white">Approve</button>
        )}
        {row.status === 'CHECKED_IN' && (
          <button onClick={() => handleStartConsultation(row)} className="rounded-full bg-yellow-400 px-3 py-1 text-xs text-slate-900">Start</button>
        )}
        {row.status === 'IN_PROGRESS' && (
          <button onClick={() => { setSelectedAppt(row); setShowModal(true); }} className="rounded-full bg-emerald-500 px-3 py-1 text-xs text-white">Add Prescription</button>
        )}
        {row.status === 'IN_PROGRESS' && (
          <button onClick={() => handleCompleteConsultation(row)} className="rounded-full bg-emerald-700 px-3 py-1 text-xs text-white">Complete</button>
        )}
      </div>
    ) }
  ]

  const handleCompleteConsultation = async (appt) => {
      try {
        const res = await api.post(`/appointments/${appt.id}/complete`)
        setAppointments(prev => prev.map(a => a.id === res.data.id ? res.data : a))
        setMessage('Consultation completed')
        setTimeout(() => setMessage(null), 3000)
      } catch (err) {
        setMessage('Unable to complete consultation')
      }
    }

  const handleNextPatient = async () => {
    try {
      const res = await api.get('/appointments/doctor/next')
      if (res.data) {
        setSelectedAppt(res.data)
        setMessage(`Next patient: ${res.data.patient?.name || res.data.patient?.email}`)
      } else {
        setMessage('No next patient')
      }
      setTimeout(() => setMessage(null), 3000)
    } catch (err) {
      setMessage('Unable to fetch next patient')
    }
  }

  const handleStartConsultation = async (appt) => {
    try {
      const res = await api.post(`/appointments/${appt.id}/start`)
      setAppointments(prev => prev.map(a => a.id === res.data.id ? res.data : a))
      setSelectedAppt(res.data)
      setMessage('Consultation started')
      setTimeout(() => setMessage(null), 3000)
    } catch (err) {
      setMessage('Unable to start consultation')
    }
  }

  const handleCreatePrescription = async () => {
    if (!selectedAppt || !medications) return
    try {
      await api.post('/prescriptions', { appointmentId: selectedAppt.id, medications, notes })
      setMessage('Prescription saved successfully.')
      setSelectedAppt(null)
      setMedications('')
      setNotes('')
      setShowModal(false)
      api.get('/prescriptions').then(r => setPrescriptions(r.data))
    } catch (err) {
      setMessage('Unable to save prescription.')
    }
  }

  const handleSetAvailability = async () => {
    if (!availabilityForm.startDate || !availabilityForm.endDate) {
      setMessage('Please select both start and end dates')
      return
    }
    try {
      const startDateTime = `${availabilityForm.startDate}T${availabilityForm.startTime}:00`
      const endDateTime = `${availabilityForm.endDate}T${availabilityForm.endTime}:00`
      
      await api.post('/availability', {
        startDateTime,
        endDateTime
      })
      setMessage('Availability set successfully!')
      fetchAvailabilitySlots()
      setAvailabilityForm({ startDate: '', startTime: '08:00', endDate: '', endTime: '18:00' })
      setShowAvailabilityModal(false)
      setTimeout(() => setMessage(null), 3000)
    } catch (err) {
      setMessage('Unable to set availability: ' + (err?.response?.data?.message || 'Unknown error'))
    }
  }

  const handleEditAvailability = async () => {
    if (!editingSlot || !availabilityForm.startDate || !availabilityForm.endDate) {
      setMessage('Please fill all fields')
      return
    }
    try {
      const startDateTime = `${availabilityForm.startDate}T${availabilityForm.startTime}:00`
      const endDateTime = `${availabilityForm.endDate}T${availabilityForm.endTime}:00`
      
      await api.put(`/availability/${editingSlot.id}`, {
        startDateTime,
        endDateTime,
        active: true
      })
      setMessage('Availability updated successfully!')
      fetchAvailabilitySlots()
      setAvailabilityForm({ startDate: '', startTime: '08:00', endDate: '', endTime: '18:00' })
      setEditingSlot(null)
      setShowEditAvailabilityModal(false)
      setTimeout(() => setMessage(null), 3000)
    } catch (err) {
      setMessage('Unable to update availability: ' + (err?.response?.data?.message || 'Unknown error'))
    }
  }

  const handleMarkUnavailable = async (slotId) => {
    try {
      await api.put(`/availability/${slotId}`, { active: false })
      setMessage('Marked as unavailable')
      fetchAvailabilitySlots()
      setTimeout(() => setMessage(null), 2000)
    } catch (err) {
      setMessage('Unable to update availability')
    }
  }

  const openEditModal = (slot) => {
    const startDT = new Date(slot.startDateTime)
    const endDT = new Date(slot.endDateTime)
    const pad = n => String(n).padStart(2, '0')
    
    setEditingSlot(slot)
    setAvailabilityForm({
      startDate: startDT.toISOString().split('T')[0],
      startTime: `${pad(startDT.getHours())}:${pad(startDT.getMinutes())}`,
      endDate: endDT.toISOString().split('T')[0],
      endTime: `${pad(endDT.getHours())}:${pad(endDT.getMinutes())}`
    })
    setShowEditAvailabilityModal(true)
  }

  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-3">
        <StatCard title="Today’s Appointments" value={upcomingCount}>Active appointments for your schedule</StatCard>
        <StatCard title="Patient List" value={patientCount}>Total unique patients</StatCard>
        <StatCard title="Prescriptions" value={prescriptionCount}>Created records</StatCard>
      </div>

      {/* Week-wise bar chart for approved appointments */}
      <div className="rounded-3xl bg-white p-6 shadow-sm">
        <h2 className="text-lg font-semibold mb-2">Approved Appointments (This Week)</h2>
        <div className="h-56">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={weekBarData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
              <XAxis dataKey="day" stroke="#94A3B8" />
              <YAxis stroke="#94A3B8" allowDecimals={false} />
              <Tooltip />
              <Bar dataKey="count" fill="#2563EB" radius={[6,6,0,0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {actionMsg && <div className="mb-2 text-sm text-emerald-600">{actionMsg}</div>}
      <section className="grid gap-6 xl:grid-cols-[1.25fr_0.75fr]">
        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-lg font-semibold">Today’s appointments</h2>
              <p className="text-sm text-slate-500">Keep your schedule on track.</p>
            </div>
            <div className="flex gap-3">
              <button onClick={handleNextPatient} className="rounded-full bg-indigo-500 px-4 py-2 text-sm font-semibold text-white">Next Patient</button>
              <button onClick={() => setShowModal(true)} className="rounded-full bg-cyan-500 px-4 py-2 text-sm font-semibold text-slate-950">Add prescription</button>
            </div>
          </div>
          <div className="mt-6 space-y-4">
            {todaysAppointments.length === 0 ? (
              <div className="rounded-3xl border border-dashed border-slate-300 p-6 text-slate-500">No appointments scheduled for today.</div>
            ) : (
              <Table columns={appointmentColumns} data={todaysAppointments} />
            )}
          </div>
        </div>

        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-lg font-semibold">Availability</h2>
              <p className="text-sm text-slate-500">Set your availability and keep patients informed about open slots.</p>
            </div>
            <button onClick={() => { setShowAvailabilityModal(true); setAvailabilityForm({ startDate: '', startTime: '08:00', endDate: '', endTime: '18:00' }); }} className="rounded-full bg-emerald-500 px-4 py-2 text-sm font-semibold text-white">+ Set Availability</button>
          </div>
          
          <div className="mt-6 space-y-3">
            {availabilitySlots.length === 0 ? (
              <div className="rounded-2xl border border-dashed border-slate-300 p-4 text-center text-sm text-slate-500">
                No availability set yet. Click "Set Availability" to add your working hours.
              </div>
            ) : (
              availabilitySlots.map(slot => (
                <div key={slot.id} className={`rounded-2xl p-4 flex items-center justify-between ${slot.active ? 'bg-emerald-50 border border-emerald-200' : 'bg-red-50 border border-red-200'}`}>
                  <div>
                    <div className={`text-sm font-medium ${slot.active ? 'text-emerald-700' : 'text-red-700'}`}>
                      {slot.active ? '✓ Available' : '✗ Not Available'}
                    </div>
                    <div className="text-sm text-slate-600 mt-1">
                      {new Date(slot.startDateTime).toLocaleString()} → {new Date(slot.endDateTime).toLocaleString()}
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <button onClick={() => openEditModal(slot)} className="rounded-lg bg-blue-500 px-3 py-2 text-xs text-white font-medium">Edit</button>
                    {slot.active && (
                      <button onClick={() => handleMarkUnavailable(slot.id)} className="rounded-lg bg-red-500 px-3 py-2 text-xs text-white font-medium">Not Available</button>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>

          {/* Quick preset options */}
          <div className="mt-6 grid gap-2 sm:grid-cols-2">
            <div className="rounded-2xl bg-slate-50 p-3">
              <div className="text-xs text-slate-600">Preset: Mon - Fri</div>
              <div className="mt-1 font-semibold text-slate-900">08:00 - 18:00</div>
            </div>
            <div className="rounded-2xl bg-slate-50 p-3">
              <div className="text-xs text-slate-600">Preset: Weekends</div>
              <div className="mt-1 font-semibold text-slate-900">10:00 - 14:00</div>
            </div>
          </div>
        </div>
      </section>

      <section className="rounded-3xl bg-white p-6 shadow-sm">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-semibold">Recent patients</h2>
            <p className="text-sm text-slate-500">Your latest patient interactions.</p>
          </div>
        </div>
        <div className="mt-6 grid gap-4 sm:grid-cols-2">
          {patients.slice(0, 4).map(patient => (
            <div key={patient.id} className="rounded-3xl border border-slate-200 p-4">
              <div className="text-sm text-slate-500">{patient.name || 'Patient'}</div>
              <div className="mt-2 text-lg font-semibold">{patient.email}</div>
              <div className="mt-3 text-sm text-slate-500">User ID: {patient.id}</div>
            </div>
          ))}
        </div>
      </section>

      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/70 p-4">
          <div className="w-full max-w-2xl rounded-3xl bg-white p-6 shadow-2xl">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-semibold">Create Prescription</h2>
              <button onClick={() => setShowModal(false)} className="text-slate-500">Close</button>
            </div>
            {message && <div className="mt-4 rounded-xl bg-emerald-100 p-4 text-sm text-emerald-700">{message}</div>}
            <div className="mt-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700">Select appointment</label>
                <select value={selectedAppt?.id || ''} onChange={e => setSelectedAppt(appointments.find(item => item.id === Number(e.target.value)))} className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none">
                  <option value="">Choose appointment</option>
                  {appointments.map(appt => (
                    <option key={appt.id} value={appt.id}>{appt.patient?.name || appt.patient?.email} — {appt.appointmentTime?.slice(0, 16).replace('T', ' ')}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700">Medications</label>
                <textarea value={medications} onChange={e => setMedications(e.target.value)} className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none" rows={4} />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700">Notes</label>
                <textarea value={notes} onChange={e => setNotes(e.target.value)} className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none" rows={4} />
              </div>
            </div>
            <div className="mt-6 flex justify-end gap-3">
              <button onClick={() => setShowModal(false)} className="rounded-2xl border border-slate-300 px-5 py-3 text-sm text-slate-600">Cancel</button>
              <button onClick={handleCreatePrescription} className="rounded-2xl bg-cyan-500 px-5 py-3 text-sm font-semibold text-slate-950">Save Prescription</button>
            </div>
          </div>
        </div>
      )}

      {/* Set Availability Modal */}
      {showAvailabilityModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/70 p-4">
          <div className="w-full max-w-2xl rounded-3xl bg-white p-6 shadow-2xl">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-semibold">Set Your Availability</h2>
              <button onClick={() => setShowAvailabilityModal(false)} className="text-slate-500">✕</button>
            </div>
            {message && <div className="mt-4 rounded-xl bg-emerald-100 p-4 text-sm text-emerald-700">{message}</div>}
            <div className="mt-6 space-y-4">
              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label className="block text-sm font-medium text-slate-700">Start Date</label>
                  <input type="date" value={availabilityForm.startDate} onChange={e => setAvailabilityForm({ ...availabilityForm, startDate: e.target.value })} className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700">Start Time</label>
                  <input type="time" value={availabilityForm.startTime} onChange={e => setAvailabilityForm({ ...availabilityForm, startTime: e.target.value })} className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none" />
                </div>
              </div>
              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label className="block text-sm font-medium text-slate-700">End Date</label>
                  <input type="date" value={availabilityForm.endDate} onChange={e => setAvailabilityForm({ ...availabilityForm, endDate: e.target.value })} className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700">End Time</label>
                  <input type="time" value={availabilityForm.endTime} onChange={e => setAvailabilityForm({ ...availabilityForm, endTime: e.target.value })} className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none" />
                </div>
              </div>
            </div>
            <div className="mt-6 flex justify-end gap-3">
              <button onClick={() => setShowAvailabilityModal(false)} className="rounded-2xl border border-slate-300 px-5 py-3 text-sm text-slate-600">Cancel</button>
              <button onClick={handleSetAvailability} className="rounded-2xl bg-emerald-500 px-5 py-3 text-sm font-semibold text-white">Set Availability</button>
            </div>
          </div>
        </div>
      )}

      {/* Edit Availability Modal */}
      {showEditAvailabilityModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/70 p-4">
          <div className="w-full max-w-2xl rounded-3xl bg-white p-6 shadow-2xl">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-semibold">Edit Availability</h2>
              <button onClick={() => setShowEditAvailabilityModal(false)} className="text-slate-500">✕</button>
            </div>
            {message && <div className="mt-4 rounded-xl bg-emerald-100 p-4 text-sm text-emerald-700">{message}</div>}
            <div className="mt-6 space-y-4">
              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label className="block text-sm font-medium text-slate-700">Start Date</label>
                  <input type="date" value={availabilityForm.startDate} onChange={e => setAvailabilityForm({ ...availabilityForm, startDate: e.target.value })} className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700">Start Time</label>
                  <input type="time" value={availabilityForm.startTime} onChange={e => setAvailabilityForm({ ...availabilityForm, startTime: e.target.value })} className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none" />
                </div>
              </div>
              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label className="block text-sm font-medium text-slate-700">End Date</label>
                  <input type="date" value={availabilityForm.endDate} onChange={e => setAvailabilityForm({ ...availabilityForm, endDate: e.target.value })} className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700">End Time</label>
                  <input type="time" value={availabilityForm.endTime} onChange={e => setAvailabilityForm({ ...availabilityForm, endTime: e.target.value })} className="mt-2 w-full rounded-2xl border border-slate-300 px-4 py-3 outline-none" />
                </div>
              </div>
            </div>
            <div className="mt-6 flex justify-end gap-3">
              <button onClick={() => setShowEditAvailabilityModal(false)} className="rounded-2xl border border-slate-300 px-5 py-3 text-sm text-slate-600">Cancel</button>
              <button onClick={handleEditAvailability} className="rounded-2xl bg-blue-500 px-5 py-3 text-sm font-semibold text-white">Update Availability</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
