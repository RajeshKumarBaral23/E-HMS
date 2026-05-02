import React, { useEffect, useState } from 'react'
import api from '../api/api'
import useAuthStore from '../store/authStore'
import { uploadFile, getFilesByRelation, downloadFile } from '../api/fileApi'

export default function MedicalHistory() {
  const [records, setRecords] = useState([])
  const [error, setError] = useState(null)
  const [message, setMessage] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ patientId: '', doctorId: '', appointmentId: '', diagnosis: '', treatment: '', notes: '', visitDate: '' })
  const [selectedRecordId, setSelectedRecordId] = useState(null)
  const [files, setFiles] = useState([])
  const [fileInput, setFileInput] = useState(null)
  const [uploading, setUploading] = useState(false)
  const user = useAuthStore(state => state.user)
  const canCreate = user?.role === 'DOCTOR' || user?.role === 'ADMIN'

  useEffect(() => {
    api.get('/medical-records')
      .then(r => setRecords(r.data))
      .catch(() => setError('Unable to load medical history'))
  }, [])

  // Load files for selected record
  useEffect(() => {
    if (selectedRecordId) {
      getFilesByRelation('MEDICAL_RECORD', selectedRecordId)
        .then(r => setFiles(r.data))
        .catch(() => setFiles([]))
    } else {
      setFiles([])
    }
  }, [selectedRecordId])

  const notify = (msg, isErr = false) => {
    if (isErr) setError(msg); else setMessage(msg)
    setTimeout(() => { setError(null); setMessage(null) }, 3500)
  }

  const handleCreate = async (e) => {
    e.preventDefault()
    try {
      const payload = { ...form }
      Object.keys(payload).forEach(k => { if (payload[k] === '') delete payload[k] })
      const res = await api.post('/medical-records', payload)
      setRecords(prev => [res.data, ...prev])
      setShowForm(false)
      setForm({ patientId: '', doctorId: '', appointmentId: '', diagnosis: '', treatment: '', notes: '', visitDate: '' })
      notify('Medical record created')
    } catch (err) {
      notify(err?.response?.data?.message || 'Failed to create record', true)
    }
  }

  return (
    <div className="space-y-6">
      <div className="rounded-3xl bg-white p-6 shadow-sm">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-semibold">Medical History</h2>
          {canCreate && (
            <button onClick={() => setShowForm(s => !s)} className="rounded-full bg-cyan-500 px-4 py-2 text-sm text-white">
              {showForm ? 'Cancel' : '+ Create Record'}
            </button>
          )}
        </div>

        {message && <div className="mt-3 text-sm text-emerald-600">{message}</div>}
        {error && <div className="mt-3 text-sm text-red-600">{error}</div>}

        {showForm && (
          <form onSubmit={handleCreate} className="mt-4 grid grid-cols-2 gap-3">
            <div>
              <label className="text-xs text-slate-500">Patient ID *</label>
              <input required type="number" className="w-full rounded-xl border px-3 py-2 text-sm" value={form.patientId}
                onChange={e => setForm(f => ({ ...f, patientId: e.target.value }))} />
            </div>
            <div>
              <label className="text-xs text-slate-500">Doctor ID *</label>
              <input required type="number" className="w-full rounded-xl border px-3 py-2 text-sm" value={form.doctorId}
                onChange={e => setForm(f => ({ ...f, doctorId: e.target.value }))} />
            </div>
            <div>
              <label className="text-xs text-slate-500">Appointment ID (optional)</label>
              <input type="number" className="w-full rounded-xl border px-3 py-2 text-sm" value={form.appointmentId}
                onChange={e => setForm(f => ({ ...f, appointmentId: e.target.value }))} />
            </div>
            <div>
              <label className="text-xs text-slate-500">Visit Date *</label>
              <input required type="datetime-local" className="w-full rounded-xl border px-3 py-2 text-sm" value={form.visitDate}
                onChange={e => setForm(f => ({ ...f, visitDate: e.target.value }))} />
            </div>
            <div className="col-span-2">
              <label className="text-xs text-slate-500">Diagnosis *</label>
              <input required className="w-full rounded-xl border px-3 py-2 text-sm" value={form.diagnosis}
                onChange={e => setForm(f => ({ ...f, diagnosis: e.target.value }))} />
            </div>
            <div>
              <label className="text-xs text-slate-500">Treatment</label>
              <input className="w-full rounded-xl border px-3 py-2 text-sm" value={form.treatment}
                onChange={e => setForm(f => ({ ...f, treatment: e.target.value }))} />
            </div>
            <div>
              <label className="text-xs text-slate-500">Notes</label>
              <input className="w-full rounded-xl border px-3 py-2 text-sm" value={form.notes}
                onChange={e => setForm(f => ({ ...f, notes: e.target.value }))} />
            </div>
            <div className="col-span-2">
              <button type="submit" className="rounded-full bg-cyan-500 px-4 py-2 text-sm text-white">Save Record</button>
            </div>
          </form>
        )}

        <div className="mt-6">
          <div className="space-y-4">
            {records.map(r => (
              <div key={r.id} className="border rounded-2xl p-4">
                <div className="text-sm text-slate-500">Visit: {r.visitDate}</div>
                <div className="mt-1 font-semibold">Diagnosis: {r.diagnosis}</div>
                <div className="text-sm">Treatment: {r.treatment}</div>
                <div className="text-sm mt-2 text-slate-600">Notes: {r.notes}</div>
                {/* File Attachment UI */}
                <div className="mt-3">
                  <button
                    className="text-xs text-cyan-600 underline"
                    onClick={() => setSelectedRecordId(selectedRecordId === r.id ? null : r.id)}
                  >
                    {selectedRecordId === r.id ? 'Hide Attachments' : 'Show Attachments'}
                  </button>
                  {selectedRecordId === r.id && (
                    <div className="mt-2 border-t pt-2">
                      <div className="flex items-center gap-2">
                        <input
                          type="file"
                          accept=".pdf,image/png,image/jpeg"
                          onChange={e => setFileInput(e.target.files[0])}
                          className="text-xs"
                        />
                        <button
                          className="rounded bg-cyan-500 px-2 py-1 text-xs text-white"
                          disabled={uploading || !fileInput}
                          onClick={async () => {
                            if (!fileInput) return;
                            const formData = new FormData();
                            formData.append('file', fileInput);
                            formData.append('relatedType', 'MEDICAL_RECORD');
                            formData.append('relatedId', r.id);
                            setUploading(true);
                            try {
                              await uploadFile(formData);
                              setFileInput(null);
                              setMessage('File uploaded');
                              // Refresh file list
                              getFilesByRelation('MEDICAL_RECORD', r.id).then(res => setFiles(res.data));
                            } catch (err) {
                              setError(err?.response?.data?.message || 'Upload failed');
                            } finally {
                              setUploading(false);
                            }
                          }}
                        >{uploading ? 'Uploading...' : 'Upload'}</button>
                      </div>
                      <div className="mt-2">
                        <div className="text-xs font-semibold mb-1">Attachments:</div>
                        {files.length === 0 && <div className="text-xs text-slate-400">No files uploaded.</div>}
                        <ul className="space-y-1">
                          {files.map(f => (
                            <li key={f.id} className="flex items-center gap-2">
                              <span className="truncate text-xs">{f.fileName}</span>
                              <button
                                className="text-xs text-cyan-600 underline"
                                onClick={async () => {
                                  try {
                                    const res = await downloadFile(f.id);
                                    const url = window.URL.createObjectURL(new Blob([res.data]));
                                    const link = document.createElement('a');
                                    link.href = url;
                                    link.setAttribute('download', f.fileName);
                                    document.body.appendChild(link);
                                    link.click();
                                    link.remove();
                                    window.URL.revokeObjectURL(url);
                                  } catch {
                                    setError('Download failed');
                                  }
                                }}
                              >Download</button>
                            </li>
                          ))}
                        </ul>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            ))}
            {records.length === 0 && <div className="text-slate-500">No medical records available.</div>}
          </div>
        </div>
      </div>
    </div>
  )
}
