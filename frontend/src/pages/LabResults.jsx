import React, { useEffect, useState } from 'react'
import api from '../api/api'
import useAuthStore from '../store/authStore'
import { uploadFile, getFilesByRelation, downloadFile } from '../api/fileApi'

export default function LabResults() {
  const user = useAuthStore(state => state.user)
  const [results, setResults] = useState([])
  const [patientId, setPatientId] = useState('')
  const [message, setMessage] = useState(null)
  const [form, setForm] = useState({ patientId: '', doctorId: '', appointmentId: '', testType: 'BLOOD', result: '' })
  const [selectedLabId, setSelectedLabId] = useState(null)
  const [files, setFiles] = useState([])
  const [fileInput, setFileInput] = useState(null)
  const [uploading, setUploading] = useState(false)

  const fetchResults = async (filterId) => {
    try {
      const query = filterId ? `?patientId=${filterId}` : ''
      const res = await api.get(`/lab-results${query}`)
      setResults(res.data)
    } catch (err) {
      setResults([])
    }
  }

  useEffect(() => {
    fetchResults(user?.role === 'PATIENT' ? null : patientId)
  }, [user, patientId])

  // Load files for selected lab result
  useEffect(() => {
    if (selectedLabId) {
      getFilesByRelation('LAB', selectedLabId)
        .then(r => setFiles(r.data))
        .catch(() => setFiles([]))
    } else {
      setFiles([])
    }
  }, [selectedLabId])

  const createResult = async () => {
    try {
      const payload = {
        patientId: Number(form.patientId),
        doctorId: form.doctorId ? Number(form.doctorId) : undefined,
        appointmentId: form.appointmentId ? Number(form.appointmentId) : undefined,
        testType: form.testType,
        result: form.result
      }
      const res = await api.post('/lab-results', payload)
      setResults(prev => [res.data, ...prev])
      setForm({ patientId: '', doctorId: '', appointmentId: '', testType: 'BLOOD', result: '' })
      setMessage('Lab result created')
      setTimeout(() => setMessage(null), 3000)
    } catch (err) {
      setMessage(err?.response?.data?.message || 'Failed to create lab result')
    }
  }

  return (
    <div className="space-y-6">
      <div className="rounded-3xl bg-white p-6 shadow-sm">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-semibold">Lab Results</h2>
          <div></div>
        </div>
        {message && <div className="mt-4 text-sm text-emerald-600">{message}</div>}
        {user?.role !== 'PATIENT' && (
          <div className="mt-6 grid gap-4 md:grid-cols-2">
            <div>
              <label className="block text-sm text-slate-500">Patient ID</label>
              <input className="w-full rounded-2xl border px-4 py-2" value={form.patientId} onChange={e => setForm({...form, patientId: e.target.value})} />
              <label className="block text-sm text-slate-500 mt-2">Doctor ID</label>
              <input className="w-full rounded-2xl border px-4 py-2" value={form.doctorId} onChange={e => setForm({...form, doctorId: e.target.value})} />
              <label className="block text-sm text-slate-500 mt-2">Appointment ID</label>
              <input className="w-full rounded-2xl border px-4 py-2" value={form.appointmentId} onChange={e => setForm({...form, appointmentId: e.target.value})} />
              <label className="block text-sm text-slate-500 mt-2">Test Type</label>
              <select className="w-full rounded-2xl border px-4 py-2" value={form.testType} onChange={e => setForm({...form, testType: e.target.value})}>
                <option value="BLOOD">BLOOD</option>
                <option value="XRAY">XRAY</option>
                <option value="ULTRASOUND">ULTRASOUND</option>
                <option value="OTHER">OTHER</option>
              </select>
              <label className="block text-sm text-slate-500 mt-2">Result</label>
              <textarea className="w-full rounded-2xl border px-4 py-2" value={form.result} onChange={e => setForm({...form, result: e.target.value})} />
              <div className="mt-4">
                <button onClick={createResult} className="rounded-2xl bg-cyan-500 px-4 py-2 text-white">Create Result</button>
              </div>
            </div>
            <div>
              <label className="block text-sm text-slate-500">Filter by Patient ID</label>
              <input className="w-full rounded-2xl border px-4 py-2" value={patientId} onChange={e => setPatientId(e.target.value)} placeholder="Leave empty for all" />
              <div className="text-xs text-slate-500 mt-2">Doctors/admin can filter records by patient.</div>
            </div>
          </div>
        )}

        <div className="mt-6 space-y-3">
          {results.map(result => (
            <div key={result.id} className="rounded-2xl border p-4">
              <div className="font-semibold">Test: {result.testType}</div>
              <div className="text-sm text-slate-500">Patient: {result.patientId} • Doctor: {result.doctorId} • Appointment: {result.appointmentId}</div>
              <div className="mt-2 text-slate-700">{result.result}</div>
              <div className="mt-2 text-xs text-slate-400">Created at: {result.createdAt}</div>
              {/* File Attachment UI */}
              <div className="mt-2">
                <button
                  className="text-xs text-cyan-600 underline"
                  onClick={() => setSelectedLabId(selectedLabId === result.id ? null : result.id)}
                >
                  {selectedLabId === result.id ? 'Hide Attachments' : 'Show Attachments'}
                </button>
                {selectedLabId === result.id && (
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
                          formData.append('relatedType', 'LAB');
                          formData.append('relatedId', result.id);
                          setUploading(true);
                          try {
                            await uploadFile(formData);
                            setFileInput(null);
                            setMessage('File uploaded');
                            // Refresh file list
                            getFilesByRelation('LAB', result.id).then(res => setFiles(res.data));
                          } catch (err) {
                            setMessage(err?.response?.data?.message || 'Upload failed');
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
                                  setMessage('Download failed');
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
          {results.length === 0 && <div className="text-slate-500">No lab results available.</div>}
        </div>
      </div>
    </div>
  )
}
