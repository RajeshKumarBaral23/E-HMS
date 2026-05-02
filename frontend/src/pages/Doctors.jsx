import React, { useEffect, useState } from 'react'
import api from '../api/api'
import Table from '../components/Table'
import useAuthStore from '../store/authStore'
import CreateDoctorModal from '../components/CreateDoctorModal'

export default function Doctors(){
  const [doctors, setDoctors] = useState([])
  const [status, setStatus] = useState('Loading...')
  const [showCreateModal, setShowCreateModal] = useState(false)
  const user = useAuthStore(state => state.user)
  const role = user?.role

  useEffect(() => {
    fetchDoctors()
  }, [])

  const fetchDoctors = () => {
    api.get('/doctors')
      .then(response => { setDoctors(response.data); setStatus('') })
      .catch(() => setStatus('Unable to load doctors'))
  }

  const handleCreated = (newDoctor) => {
    setDoctors(prev => [newDoctor, ...prev])
    setStatus('')
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this doctor?')) return;
    try {
      await api.delete(`/doctors/${id}`)
      setDoctors(prev => prev.filter(d => d.id !== id))
    } catch {
      setStatus('Failed to delete doctor')
    }
  }

  return (
    <div className="space-y-6">
      <div className="rounded-3xl bg-white p-6 shadow-sm">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-semibold">Doctors</h2>
            <p className="text-sm text-slate-500">Browse doctor schedules and specialties.</p>
          </div>
          {role === 'ADMIN' && (
            <div>
              <button onClick={() => setShowCreateModal(true)} className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-cyan-500 text-white text-sm hover:bg-cyan-600">
                Create Doctor
              </button>
            </div>
          )}
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
              { key: 'specialization', title: 'Specialization' },
              { key: 'phone', title: 'Phone' },
              ...(role === 'ADMIN' ? [{
                key: 'actions',
                title: 'Actions',
                render: row => (
                  <button
                    className="text-xs text-red-600 underline"
                    onClick={() => handleDelete(row.id)}
                  >Delete</button>
                )
              }] : [])
            ]}
            data={doctors}
          />
        </div>
      )}

      <CreateDoctorModal open={showCreateModal} onClose={() => setShowCreateModal(false)} onCreated={(d) => { handleCreated(d); setShowCreateModal(false); }} />
    </div>
  )
}
