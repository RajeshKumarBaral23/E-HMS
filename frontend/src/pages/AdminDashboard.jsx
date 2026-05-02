import React, { useEffect, useMemo, useState } from 'react'
import api from '../api/api'
import StatCard from '../components/StatCard'
import Table from '../components/Table'
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, BarChart, Bar, Legend } from 'recharts'

const COLORS = ['#22C55E', '#2563EB', '#F59E0B', '#E11D48']

export default function AdminDashboard() {
  const [patients, setPatients] = useState([])
  const [doctors, setDoctors] = useState([])
  const [appointments, setAppointments] = useState([])
  const [prescriptions, setPrescriptions] = useState([])

  useEffect(() => {
    api.get('/patients').then(r => {
      console.log('Patients fetched:', r.data)
      setPatients(r.data)
    }).catch(err => console.error('Patients error:', err))
    
    api.get('/doctors').then(r => {
      console.log('Doctors fetched:', r.data)
      setDoctors(r.data)
    }).catch(err => console.error('Doctors error:', err))
    
    api.get('/appointments').then(r => {
      console.log('Appointments fetched:', r.data)
      setAppointments(r.data)
    }).catch(err => console.error('Appointments error:', err))
    
    api.get('/prescriptions').then(r => {
      console.log('Prescriptions fetched:', r.data)
      setPrescriptions(r.data)
    }).catch(err => console.error('Prescriptions error:', err))
    api.get('/admin/analytics/appointments-per-day?days=7').then(r => {
      const data = Object.entries(r.data).map(([day, cnt]) => ({ name: day.slice(5), appts: cnt }))
      // set lineData via state (replace computed lineData)
      setLineData(data)
    }).catch(() => {})
  }, [])

  const [lineData, setLineData] = useState([])
  // fallback compute from appointments when analytics not available
  useEffect(() => {
    if (lineData.length === 0 && appointments.length) {
      const map = {}
      appointments.forEach(appt => {
        const date = appt.appointmentTime?.split('T')[0] || 'Unknown'
        map[date] = (map[date] || 0) + 1
      })
      setLineData(Object.keys(map).sort().map(date => ({ name: date.slice(5), appts: map[date] })))
    }
  }, [appointments])

  const departmentData = useMemo(() => {
    const map = {}
    doctors.forEach(doc => {
      const key = doc.specialization || 'General'
      map[key] = (map[key] || 0) + 1
    })
    return Object.entries(map).map(([name, value]) => ({ name, value }))
  }, [doctors])

  // Weekly department-wise bar chart data (inside component)
  const weeklyDeptBarData = useMemo(() => {
    const deptMap = {}
    appointments.forEach(appt => {
      if (!appt.appointmentTime || !appt.doctor) return
      const d = new Date(appt.appointmentTime)
      if (isNaN(d)) return
      const dept = appt.doctor.specialization || 'General'
      if (!deptMap[dept]) deptMap[dept] = [0, 0, 0, 0, 0, 0, 0]
      deptMap[dept][d.getDay()]++
    })
    const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']
    return days.map((day, i) => {
      const row = { day }
      Object.keys(deptMap).forEach(dept => { row[dept] = deptMap[dept][i] })
      return row
    })
  }, [appointments])

  const recentActivities = appointments.slice(-6).reverse()

  const [actionMsg, setActionMsg] = useState(null)
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
  function formatDate(dateStr) {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    if (isNaN(d)) return dateStr;
    return `${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
  const columns = [
    { key: 'id', title: 'ID' },
    { key: 'queueNumber', title: 'Serial', render: row => row.queueNumber || '-' },
    { key: 'patient', title: 'Patient', render: row => row.patient?.name || row.patient?.email },
    { key: 'doctor', title: 'Doctor', render: row => row.doctor?.name || row.doctor?.email },
    { key: 'appointmentTime', title: 'Date / Time', render: row => `${formatDate(row.appointmentTime)}${row.appointmentTime ? ' / ' + row.appointmentTime.split('T')[1]?.slice(0,5) : ''}` },
    { key: 'status', title: 'Status', render: row => row.status || 'Scheduled' },
    { key: 'actions', title: 'Actions', render: row => (
      row.status === 'PENDING' ? (
        <>
          <button onClick={() => approveAppointment(row)} className="rounded bg-emerald-500 px-3 py-1 text-xs text-white mr-2">Approve</button>
          <button className="rounded bg-red-500 px-3 py-1 text-xs text-white">Cancel</button>
        </>
      ) : null
    ) },
  ]


  const deptKeys = Object.keys(weeklyDeptBarData[0] || {}).filter(k => k !== 'day')

  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-4">
        <StatCard title="Total Patients" value={patients.length}>
          {patients.length > 0 ? `${patients.length} active profiles` : 'Loading...'}
        </StatCard>
        <StatCard title="Total Doctors" value={doctors.length}>
          {doctors.length > 0 ? `${doctors.length} specialists` : 'Loading...'}
        </StatCard>
        <StatCard title="Total Appointments" value={appointments.length}>
          {appointments.length > 0 ? 'Live booking metrics' : 'Loading...'}
        </StatCard>
        <StatCard title="Revenue" value={`$${(appointments.length * 120).toLocaleString()}`}> 
          Projected from current booking volume
        </StatCard>
      </div>

      {/* Weekly department-wise bar chart */}
      <div className="rounded-3xl bg-white p-6 shadow-sm">
        <h2 className="text-lg font-semibold mb-2">Weekly Appointments by Department</h2>
        <div className="h-72">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={weeklyDeptBarData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
              <XAxis dataKey="day" stroke="#94A3B8" />
              <YAxis stroke="#94A3B8" allowDecimals={false} />
              <Tooltip />
              <Legend />
              {deptKeys.map((dept, idx) => (
                <Bar key={dept} dataKey={dept} fill={COLORS[idx % COLORS.length]} radius={[6, 6, 0, 0]} />
              ))}
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="grid gap-6 xl:grid-cols-[1.25fr_0.75fr]">
        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold">Appointments trend</h2>
            <span className="text-sm text-slate-500">Last 7 days</span>
          </div>
          <div className="mt-6 h-72">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={lineData.length ? lineData : [{ name: 'No data', appts: 0 }] }>
                <XAxis dataKey="name" stroke="#94A3B8" />
                <YAxis stroke="#94A3B8" />
                <Tooltip />
                <Line type="monotone" dataKey="appts" stroke="#2563EB" strokeWidth={3} dot={{ r: 4 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <h2 className="text-lg font-semibold">Department distribution</h2>
          <div className="mt-6 h-72">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={departmentData.length ? departmentData : [{ name: 'General', value: 1 }]} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label>
                  {(departmentData.length ? departmentData : [{ name: 'General', value: 1 }]).map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      <section className="rounded-3xl bg-white p-6 shadow-sm">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-lg font-semibold">Recent activities</h2>
            <p className="text-sm text-slate-500">Latest appointments and clinical activity.</p>
          </div>
          <div className="rounded-full bg-slate-100 px-4 py-2 text-sm text-slate-600">Updated just now</div>
        </div>
        {actionMsg && <div className="mb-2 text-sm text-emerald-600">{actionMsg}</div>}
        <div className="mt-4">
          <Table columns={columns} data={recentActivities} />
        </div>
      </section>
    </div>
  )
}
