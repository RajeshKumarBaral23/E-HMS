import React, { useEffect, useState } from 'react'
import api from '../api/api'
import useAuthStore from '../store/authStore'
import Table from '../components/Table'

export default function Patients(){
  const [patients, setPatients] = useState([])
  const [status, setStatus] = useState('Loading...')
  const user = useAuthStore(state => state.user)

  useEffect(() => {
    if (!user) return

    if (user.role === 'ADMIN') {
      api.get('/patients')
        .then(response => { setPatients(response.data); setStatus('') })
        .catch(() => setStatus('Unable to load patients'))
    } else if (user.role === 'PATIENT') {
      api.get('/patients/me')
        .then(response => { setPatients([response.data]); setStatus('') })
        .catch(() => setStatus('Unable to load profile'))
    } else {
      setStatus('Only administrators can view all patients.')
    }
  }, [user])

  return (
    <div className="space-y-6">
      <div className="rounded-3xl bg-white p-6 shadow-sm">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-semibold">Patients</h2>
            <p className="text-sm text-slate-500">Manage patient profiles and records.</p>
          </div>
        </div>
      </div>

      {status ? (
        <div className="rounded-3xl bg-white p-6 shadow-sm text-slate-500">{status}</div>
      ) : (
        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <Table
            columns={[
              { key: 'id', title: 'ID' },
              { key: 'name', title: 'Name', render: row => row.name },
              { key: 'email', title: 'Email', render: row => row.email },
              { key: 'phone', title: 'Phone' },
              { key: 'address', title: 'Address' },
              { key: 'dob', title: 'DOB' },
            ]}
            data={patients}
          />
        </div>
      )}
    </div>
  )
}
