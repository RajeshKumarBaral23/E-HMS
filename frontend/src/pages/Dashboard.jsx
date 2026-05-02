import React, { useEffect, useState } from 'react'
import StatCard from '../components/StatCard'
import api from '../api/api'
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts'
import Table from '../components/Table'

export default function Dashboard(){
  const [appointments, setAppointments] = useState([])
  const [stats, setStats] = useState({ patients:0, doctors:0, appts:0, prescriptions:0 })

  useEffect(()=>{
    // sample calls (backend endpoints may vary)
    api.get('/appointments').then(r=> setAppointments(r.data)).catch(()=>{})
    // dummy stats for now
    setStats({ patients: 42, doctors: 8, appts: 12, prescriptions: 4 })
  },[])

  const lineData = [
    { name: 'Mon', appts: 3 },
    { name: 'Tue', appts: 5 },
    { name: 'Wed', appts: 2 },
    { name: 'Thu', appts: 6 },
    { name: 'Fri', appts: 4 },
  ]

  const pieData = [{ name: 'Completed', value: 8 }, { name: 'Scheduled', value: 4 }]
  const COLORS = ['#10B981', '#3B82F6']

  const columns = [
    { key: 'id', title: 'ID' },
    { key: 'patient', title: 'Patient', render: r => r.patient?.name || r.patient?.email },
    { key: 'doctor', title: 'Doctor', render: r => r.doctor?.name || r.doctor?.email },
    { key: 'appointmentTime', title: 'When' },
  ]

  return (
    <div className="container space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <StatCard title="Patients" value={stats.patients} />
        <StatCard title="Doctors" value={stats.doctors} />
        <StatCard title="Appointments" value={stats.appts} />
        <StatCard title="Prescriptions" value={stats.prescriptions} />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="md:col-span-2 bg-white p-4 rounded shadow">
          <h3 className="font-semibold mb-2">Appointments (week)</h3>
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={lineData}>
              <XAxis dataKey="name" />
              <YAxis />
              <Tooltip />
              <Line type="monotone" dataKey="appts" stroke="#3B82F6" />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-white p-4 rounded shadow">
          <h3 className="font-semibold mb-2">Status</h3>
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie data={pieData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={60}>
                {pieData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div>
        <h3 className="font-semibold mb-2">Recent Appointments</h3>
        <Table columns={columns} data={appointments.slice(0,6)} />
      </div>
    </div>
  )
}
