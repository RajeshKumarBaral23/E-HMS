import React, { useEffect, useMemo, useState } from 'react'
import api from '../api/api'
import useAuthStore from '../store/authStore'
import Table from '../components/Table'

const statusColors = {
  PENDING: 'bg-slate-100 text-slate-700',
  CONFIRMED: 'bg-emerald-100 text-emerald-700',
  CHECKED_IN: 'bg-cyan-100 text-cyan-700',
  IN_PROGRESS: 'bg-yellow-100 text-yellow-700',
  COMPLETED: 'bg-emerald-200 text-emerald-900',
  CANCELLED: 'bg-red-100 text-red-700',
}

export default function Appointments(){
  const [appointments, setAppointments] = useState([])
  const [doctors, setDoctors] = useState([])
  const [filters, setFilters] = useState({ date: '', doctor: '', status: '' })
  const [booking, setBooking] = useState({ doctorId: '', appointmentTime: '', durationMinutes: 30, reason: '' })
  const [rescheduleFor, setRescheduleFor] = useState(null)
  const [message, setMessage] = useState(null)
  const user = useAuthStore(state => state.user)

  useEffect(() => {
    api.get('/appointments').then(r => setAppointments(r.data)).catch(err => console.error('Appointments fetch error:', err))
    api.get('/doctors').then(r => {
      console.log('Doctors fetched:', r.data)
      setDoctors(r.data)
    }).catch(err => console.error('Doctors fetch error:', err))

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

  const pickerDoctors = useMemo(() => {
    const byIdentity = new Map()

    doctors.forEach(doc => {
      const identity = doc.userId || doc.id || doc.email
      const existing = byIdentity.get(identity)

      if (!existing) {
        byIdentity.set(identity, doc)
        return
      }

      const existingScore = (existing.departmentName ? 2 : 0) + (existing.specialization ? 1 : 0)
      const nextScore = (doc.departmentName ? 2 : 0) + (doc.specialization ? 1 : 0)

      if (nextScore > existingScore) {
        byIdentity.set(identity, doc)
      }
    })

    return Array.from(byIdentity.values())
  }, [doctors])

  const doctorsByDepartment = useMemo(() => {
    console.log('Grouping doctors:', pickerDoctors)
    const groups = new Map()
    pickerDoctors.forEach(doc => {
      const departmentName = doc.departmentName || 'General'
      if (!groups.has(departmentName)) {
        groups.set(departmentName, [])
      }
      groups.get(departmentName).push(doc)
    })

    return Array.from(groups.entries())
      .map(([departmentName, members]) => ({
        departmentName,
        members: members.sort((left, right) => {
          const leftName = left.name || left.email || ''
          const rightName = right.name || right.email || ''
          return leftName.localeCompare(rightName)
        }),
      }))
      .sort((left, right) => left.departmentName.localeCompare(right.departmentName))
  }, [pickerDoctors])

  const filteredAppointments = useMemo(() => {
    return appointments.filter(appt => {
      const dateMatch = filters.date ? appt.appointmentTime?.startsWith(filters.date) : true
      const doctorMatch = filters.doctor ? appt.doctor?.id === Number(filters.doctor) : true
      const statusMatch = filters.status ? appt.status === filters.status : true
      return dateMatch && doctorMatch && statusMatch
    })
  }, [appointments, filters])

  const doctorLabel = doc => {
    const name = doc.name || doc.email || 'Unknown doctor'
    return doc.specialization ? `${name} - ${doc.specialization}` : name
  }

  const handleBookAppointment = async (event) => {
    event.preventDefault()

    try {
      await api.post('/appointments', {
        doctorId: Number(booking.doctorId),
        appointmentTime: booking.appointmentTime,
        durationMinutes: Number(booking.durationMinutes),
        reason: booking.reason.trim(),
      })

      // refresh list after booking
      const refreshed = await api.get('/appointments')
      setAppointments(refreshed.data)

      // if we were rescheduling, cancel the original after new booking succeeded
      if (rescheduleFor) {
        try {
          await api.put(`/appointments/${rescheduleFor}/status`, { status: 'CANCELLED' })
        } catch (e) {
          // ignore non-fatal cancellation error
        }
      }

      setBooking({ doctorId: '', appointmentTime: '', durationMinutes: 30, reason: '' })
      setRescheduleFor(null)
      setMessage(rescheduleFor ? 'Appointment rescheduled successfully.' : 'Appointment booked successfully.')
    } catch (err) {
      setMessage(err?.response?.data?.message || 'Unable to book appointment.')
    }

    window.setTimeout(() => setMessage(null), 3200)
  }

  // helper to prefill booking for reschedule
  const toInputDateTime = (iso) => {
    if (!iso) return ''
    const d = new Date(iso)
    const pad = n => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
  }

  const handleReschedule = (row) => {
    setRescheduleFor(row.id)
    setBooking({
      doctorId: row.doctor?.id || row.doctor?.userId || '',
      appointmentTime: toInputDateTime(row.appointmentTime),
      durationMinutes: row.durationMinutes || 30,
      reason: row.reason || ''
    })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const handleAction = async (id, action) => {
    try {
      if (action === 'confirm') {
        const res = await api.put(`/appointments/${id}/status`, { status: 'CONFIRMED' })
        updateLocal(res.data)
        setMessage('Appointment confirmed.')
      } else if (action === 'checkin') {
        const res = await api.put(`/appointments/${id}/status`, { status: 'CHECKED_IN' })
        updateLocal(res.data)
        setMessage('Checked in successfully.')
      } else if (action === 'start') {
        const res = await api.post(`/appointments/${id}/start`)
        updateLocal(res.data)
        setMessage('Consultation started.')
      } else if (action === 'complete') {
        const res = await api.post(`/appointments/${id}/complete`)
        updateLocal(res.data)
        setMessage('Consultation completed.')
      } else if (action === 'cancel') {
        const res = await api.put(`/appointments/${id}/status`, { status: 'CANCELLED' })
        updateLocal(res.data)
        setMessage('Appointment cancelled.')
      }
    } catch (err) {
      setMessage(err?.response?.data?.message || 'Action failed')
    }
    window.setTimeout(() => setMessage(null), 3200)
  }

  const updateLocal = (updatedAppt) => {
    setAppointments(prev => prev.map(a => a.id === updatedAppt.id ? updatedAppt : a))
  }

  const columns = [
    { key: 'id', title: 'ID' },
    { key: 'patient', title: 'Patient', render: row => row.patient?.name || row.patient?.email },
    { key: 'doctor', title: 'Doctor', render: row => row.doctor?.name || row.doctor?.email },
    { key: 'appointmentTime', title: 'Date / Time' },
    { key: 'status', title: 'Status', render: row => <span className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${statusColors[row.status] || 'bg-slate-100 text-slate-700'}`}>{row.status || 'Scheduled'}</span> },
    { key: 'actions', title: 'Actions', render: row => (
      <div className="flex gap-2"> 
        {user?.role === 'ADMIN' && row.status === 'PENDING' && (
          <button onClick={() => handleAction(row.id, 'confirm')} className="rounded-full bg-emerald-500 px-3 py-1 text-xs text-white">Confirm</button>
        )}
        {user?.role === 'PATIENT' && row.status === 'CONFIRMED' && (
          <button onClick={() => handleAction(row.id, 'checkin')} className="rounded-full bg-cyan-500 px-3 py-1 text-xs text-white">Check-in</button>
        )}
        {user?.role === 'PATIENT' && row.status !== 'COMPLETED' && row.status !== 'CANCELLED' && (
          <button onClick={() => handleReschedule(row)} className="rounded-full bg-indigo-500 px-3 py-1 text-xs text-white">Reschedule</button>
        )}
        {(user?.role === 'DOCTOR' || user?.role === 'ADMIN') && row.status === 'CHECKED_IN' && (
          <button onClick={() => handleAction(row.id, 'start')} className="rounded-full bg-yellow-400 px-3 py-1 text-xs text-slate-900">Start</button>
        )}
        {(user?.role === 'DOCTOR' || user?.role === 'ADMIN') && row.status === 'IN_PROGRESS' && (
          <button onClick={() => handleAction(row.id, 'complete')} className="rounded-full bg-emerald-500 px-3 py-1 text-xs text-white">Complete</button>
        )}
        {(user?.role === 'PATIENT' || user?.role === 'ADMIN') && row.status !== 'COMPLETED' && row.status !== 'CANCELLED' && (
          <button onClick={() => handleAction(row.id, 'cancel')} className="rounded-full bg-red-500 px-3 py-1 text-xs text-white">Cancel</button>
        )}
      </div>
    ) },
  ]
 

  return (
    <div className="space-y-6">
      {user?.role === 'PATIENT' && (
        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <h2 className="text-2xl font-semibold">Book Appointment</h2>
              <p className="text-sm text-slate-500">
                {pickerDoctors.length > 0
                  ? `${pickerDoctors.length} doctors available across ${doctorsByDepartment.length} departments.`
                  : 'No doctors are available yet.'}
              </p>
            </div>
          </div>

          <form onSubmit={handleBookAppointment} className="mt-6 grid gap-4 lg:grid-cols-2">
            {rescheduleFor && (
              <div className="lg:col-span-2 rounded-2xl bg-yellow-50 border border-yellow-100 p-3 text-sm text-yellow-800 flex items-center justify-between">
                <div>Rescheduling appointment #{rescheduleFor}</div>
                <button type="button" onClick={() => { setRescheduleFor(null); setBooking({ doctorId: '', appointmentTime: '', durationMinutes: 30, reason: '' }) }} className="text-xs text-slate-600 underline">Cancel reschedule</button>
              </div>
            )}
            <label className="space-y-2">
              <span className="text-sm font-medium text-slate-700">Doctor</span>
              <select
                required
                value={booking.doctorId}
                onChange={event => setBooking(prev => ({ ...prev, doctorId: event.target.value }))}
                className="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none"
              >
                <option value="">Select a doctor</option>
                {doctorsByDepartment.map(group => (
                  <optgroup key={group.departmentName} label={group.departmentName}>
                    {group.members.map(doc => (
                      <option key={doc.id} value={doc.userId}>
                        {doctorLabel(doc)}
                      </option>
                    ))}
                  </optgroup>
                ))}
              </select>
            </label>

            <label className="space-y-2">
              <span className="text-sm font-medium text-slate-700">Appointment time</span>
              <input
                required
                type="datetime-local"
                value={booking.appointmentTime}
                onChange={event => setBooking(prev => ({ ...prev, appointmentTime: event.target.value }))}
                className="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none"
              />
            </label>

            <label className="space-y-2">
              <span className="text-sm font-medium text-slate-700">Duration (minutes)</span>
              <input
                required
                min="15"
                step="15"
                type="number"
                value={booking.durationMinutes}
                onChange={event => setBooking(prev => ({ ...prev, durationMinutes: event.target.value }))}
                className="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none"
              />
            </label>

            <label className="space-y-2 lg:col-span-2">
              <span className="text-sm font-medium text-slate-700">Reason</span>
              <textarea
                required
                rows="4"
                value={booking.reason}
                onChange={event => setBooking(prev => ({ ...prev, reason: event.target.value }))}
                className="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none"
                placeholder="Describe the reason for your visit"
              />
            </label>

            <div className="lg:col-span-2 flex justify-end">
              <button
                type="submit"
                disabled={pickerDoctors.length === 0}
                className="rounded-full bg-cyan-500 px-5 py-3 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:bg-slate-300"
              >
                {rescheduleFor ? 'Reschedule appointment' : 'Book appointment'}
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="rounded-3xl bg-white p-6 shadow-sm">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-2xl font-semibold">Appointments</h2>
            <p className="text-sm text-slate-500">Filter appointment status and manage scheduling.</p>
          </div>
          <div className="grid w-full gap-3 sm:w-auto sm:grid-cols-3">
            <input type="date" value={filters.date} onChange={e => setFilters(prev => ({ ...prev, date: e.target.value }))} className="rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none" />
            <select value={filters.doctor} onChange={e => setFilters(prev => ({ ...prev, doctor: e.target.value }))} className="rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none">
              <option value="">All doctors</option>
              {doctorsByDepartment.map(group => (
                <optgroup key={group.departmentName} label={group.departmentName}>
                  {group.members.map(doc => (
                    <option key={doc.id} value={doc.userId}>
                      {doctorLabel(doc)}
                    </option>
                  ))}
                </optgroup>
              ))}
            </select>
            <select value={filters.status} onChange={e => setFilters(prev => ({ ...prev, status: e.target.value }))} className="rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none">
              <option value="">All statuses</option>
              {Object.keys(statusColors).map(s => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
        </div>
      </div>

      {message && <div className="rounded-3xl bg-emerald-100 p-4 text-sm text-emerald-800">{message}</div>}

      <div className="rounded-3xl bg-white p-6 shadow-sm">
        <Table columns={columns} data={filteredAppointments} />
      </div>
    </div>
  )
}
