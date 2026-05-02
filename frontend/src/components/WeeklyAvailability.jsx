import React from 'react'

const DAYS = [
  { key: 'MON', label: 'Mon' },
  { key: 'TUE', label: 'Tue' },
  { key: 'WED', label: 'Wed' },
  { key: 'THU', label: 'Thu' },
  { key: 'FRI', label: 'Fri' },
  { key: 'SAT', label: 'Sat' },
  { key: 'SUN', label: 'Sun' },
]

export default function WeeklyAvailability({ value = [], onChange }) {
  const rules = Array.isArray(value) ? value : []

  function pushRule() {
    const next = [...rules, { days: [], start: '09:00', end: '17:00' }]
    onChange && onChange(next)
  }

  function updateRule(idx, patch) {
    const next = rules.map((r, i) => i === idx ? { ...r, ...patch } : r)
    onChange && onChange(next)
  }

  function removeRule(idx) {
    const next = rules.filter((_, i) => i !== idx)
    onChange && onChange(next)
  }

  function toggleDay(idx, day) {
    const r = rules[idx]
    if (!r) return
    const has = (r.days || []).includes(day)
    const days = has ? r.days.filter(d => d !== day) : [...(r.days||[]), day]
    updateRule(idx, { days })
  }

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <h4 className="text-sm font-medium">Weekly availability</h4>
        <button type="button" onClick={pushRule} className="text-sm text-cyan-600">+ Add rule</button>
      </div>

      {rules.length === 0 && <div className="text-sm text-slate-500">No recurring availability added.</div>}

      {rules.map((r, idx) => (
        <div key={idx} className="border rounded p-3">
          <div className="flex items-center gap-3 mb-2">
            <div className="flex gap-1">
              {DAYS.map(d => {
                const active = (r.days || []).includes(d.key)
                return (
                  <button key={d.key} type="button" onClick={() => toggleDay(idx, d.key)} className={`px-2 py-1 rounded ${active ? 'bg-cyan-500 text-white' : 'bg-slate-100 text-slate-700'}`}>
                    {d.label}
                  </button>
                )
              })}
            </div>
            <div className="ml-auto flex items-center gap-2">
              <input type="time" value={r.start} onChange={e => updateRule(idx, { start: e.target.value })} className="border rounded p-1" />
              <span className="text-sm">to</span>
              <input type="time" value={r.end} onChange={e => updateRule(idx, { end: e.target.value })} className="border rounded p-1" />
              <button type="button" onClick={() => removeRule(idx)} className="ml-2 text-sm text-red-600">Remove</button>
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}
