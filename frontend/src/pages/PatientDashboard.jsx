import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/api'
import StatCard from '../components/StatCard'
import Table from '../components/Table'

export default function PatientDashboard() {
  const [profile, setProfile] = useState(null)
  const [appointments, setAppointments] = useState([])
  const [prescriptions, setPrescriptions] = useState([])
  const [notifications, setNotifications] = useState([])

  useEffect(() => {
    api.get('/patients/me').then(r => setProfile(r.data)).catch(() => {})
    api.get('/appointments').then(r => setAppointments(r.data)).catch(() => {})
    api.get('/prescriptions').then(r => setPrescriptions(r.data)).catch(() => {})

    const token = localStorage.getItem('token')
    if (!token) return
    const streamUrl = `${api.defaults.baseURL}/appointments/stream?token=${token}`
    const es = new EventSource(streamUrl)
    es.addEventListener('appointment', e => {
      try {
        const appt = JSON.parse(e.data)
        setAppointments(prev => prev.map(a => a.id === appt.id ? appt : a))
        setNotifications(prev => [{ id: appt.id, text: `Appointment ${appt.id} updated: ${appt.status}`, time: new Date().toISOString() }, ...prev])
      } catch (err) {}
    })
    es.onerror = () => es.close()
    return () => es.close()
  }, [])

  const upcoming = appointments.filter(a => new Date(a.appointmentTime) >= new Date())
  const history = appointments.filter(a => new Date(a.appointmentTime) < new Date()).slice(-5).reverse()
  const reminders = appointments.filter(a => a.followUpDate).filter(a => new Date(a.followUpDate) >= new Date())

  // Find latest bill for the patient (if any)
  const [bills, setBills] = useState([])
  useEffect(() => { api.get('/billing').then(r => setBills(r.data)).catch(() => {}) }, [])
  const latestBill = bills.length > 0 ? bills[0] : null

  // Workflow timeline for the latest appointment
  const latestAppt = appointments.length > 0 ? appointments[0] : null
  const timeline = latestAppt ? [
    { label: 'Booked', active: true },
    { label: 'Confirmed', active: ['CONFIRMED','CHECKED_IN','IN_PROGRESS','COMPLETED'].includes(latestAppt.status) },
    { label: 'Checked-in', active: ['CHECKED_IN','IN_PROGRESS','COMPLETED'].includes(latestAppt.status) },
    { label: 'In Progress', active: ['IN_PROGRESS','COMPLETED'].includes(latestAppt.status) },
    { label: 'Completed', active: latestAppt.status === 'COMPLETED' },
  ] : []

  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-3">
        <StatCard title="Book an appointment" value="Fast" >
          <Link to="/appointments" className="text-cyan-500 hover:text-cyan-400">Schedule now</Link>
        </StatCard>
        <StatCard title="Upcoming visits" value={upcoming.length}>{upcoming.length} appointments pending</StatCard>
        <StatCard title="Prescriptions" value={prescriptions.length}>{prescriptions.length} records</StatCard>
      </div>

      <div className="grid gap-6 xl:grid-cols-[0.75fr_1.25fr]">
        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold">Your profile</h2>
            <div className="text-sm text-slate-500">Secure patient record</div>
          </div>
          {profile ? (
            <div className="mt-6 space-y-4">
              <div className="rounded-3xl bg-slate-50 p-5">
                <div className="text-sm text-slate-500">Name</div>
                <div className="mt-2 text-lg font-semibold text-slate-900">{profile.name}</div>
              </div>
              <div className="rounded-3xl bg-slate-50 p-5">
                <div className="text-sm text-slate-500">Email</div>
                <div className="mt-2 text-lg font-semibold text-slate-900">{profile.email}</div>
              </div>
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="rounded-3xl bg-slate-50 p-5">
                  <div className="text-sm text-slate-500">Phone</div>
                  <div className="mt-2 text-lg font-semibold text-slate-900">{profile.phone || 'Not set'}</div>
                </div>
                <div className="rounded-3xl bg-slate-50 p-5">
                  <div className="text-sm text-slate-500">Address</div>
                  <div className="mt-2 text-lg font-semibold text-slate-900">{profile.address || 'Not set'}</div>
                </div>
              </div>
            </div>
          ) : (
            <div className="mt-6 text-slate-500">Loading profile...</div>
          )}
        </div>

        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold">Notifications</h2>
            <span className="rounded-full bg-cyan-100 px-3 py-1 text-xs text-cyan-700">New</span>
          </div>
          <div className="mt-6 space-y-4 text-slate-600">
            {notifications.length === 0 && (
              <div className="rounded-3xl border border-dashed border-slate-300 p-6 text-slate-500">No notifications.</div>
            )}
            {notifications.map(n => (
              <div key={n.id} className="rounded-3xl border border-slate-200 p-4">{n.text} <div className="text-xs text-slate-400">{n.time}</div></div>
            ))}
          </div>
        </div>
      </div>

      <div className="grid gap-6 xl:grid-cols-2">
        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <h2 className="text-lg font-semibold mb-2">Appointment Status Timeline</h2>
          {latestAppt ? (
            <div className="flex gap-2 items-center">
              {timeline.map((step, idx) => (
                <React.Fragment key={step.label}>
                  <span className={`px-3 py-1 rounded-full text-xs font-semibold ${step.active ? 'bg-emerald-500 text-white' : 'bg-slate-200 text-slate-500'}`}>{step.label}</span>
                  {idx < timeline.length-1 && <span className="text-slate-400">→</span>}
                </React.Fragment>
              ))}
            </div>
          ) : <div className="text-slate-500">No recent appointment.</div>}
        </div>
        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold">Appointment history</h2>
            <Link to="/appointments" className="text-cyan-500 hover:text-cyan-400">View all</Link>
          </div>
          <div className="mt-6">
            {history.length === 0 ? (
              <div className="rounded-3xl border border-dashed border-slate-300 p-6 text-slate-500">No appointment history yet.</div>
            ) : (
              <Table columns={[
                { key: 'appointmentTime', title: 'Date' },
                { key: 'doctor', title: 'Doctor', render: row => row.doctor?.name || row.doctor?.email },
                { key: 'status', title: 'Status', render: row => row.status || 'Scheduled' }
              ]} data={history} />
            )}
          </div>
        </div>

        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold">Recent prescriptions</h2>
            <Link to="/prescriptions" className="text-cyan-500 hover:text-cyan-400">See all</Link>
          </div>
          <div className="mt-6 space-y-4">
            {prescriptions.slice(0, 4).map(item => (
              <div key={item.id} className="rounded-3xl border border-slate-200 p-4">
                <div className="text-sm text-slate-500">{item.appointment?.appointmentTime?.slice(0, 10)}</div>
                <div className="mt-2 font-semibold text-slate-900">{item.medications}</div>
                <div className="mt-2 text-sm text-slate-500">{item.notes || 'No additional notes'}</div>
              </div>
            ))}
            {prescriptions.length === 0 && <div className="rounded-3xl border border-dashed border-slate-300 p-6 text-slate-500">No prescriptions yet.</div>}
          </div>
        </div>
        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <h2 className="text-lg font-semibold mb-2">Latest Bill</h2>
          {latestBill ? (
            <div>
              <div className="text-sm text-slate-500">Appointment: {latestBill.appointmentId}</div>
              <div className="mt-1 font-semibold">Total: ${latestBill.totalAmount?.toFixed(2)}</div>
              <div className="text-sm text-slate-500">Consultation: ${latestBill.consultationFee} • Medicine: ${latestBill.medicineCost}</div>
              <div className={`px-3 py-1 rounded-full text-xs mt-2 inline-block ${latestBill.status === 'PAID' ? 'bg-emerald-100 text-emerald-800' : 'bg-yellow-100 text-yellow-800'}`}>{latestBill.status}</div>
            </div>
          ) : <div className="text-slate-500">No bill yet.</div>}
        </div>
      </div>
      <div className="rounded-3xl bg-white p-6 shadow-sm">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold">Follow-up reminders</h2>
          <span className="text-sm text-slate-500">Upcoming</span>
        </div>
        <div className="mt-4 space-y-3">
          {reminders.length === 0 && <div className="rounded-3xl border border-dashed border-slate-300 p-6 text-slate-500">No follow-up reminders.</div>}
          {reminders.map(r => (
            <div key={r.id} className="rounded-3xl border border-slate-200 p-4">
              <div className="text-sm text-slate-500">Follow-up on {r.followUpDate}</div>
              <div className="mt-2 font-semibold text-slate-900">After appointment {r.id} with {r.doctor?.name}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
