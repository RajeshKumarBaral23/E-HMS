import React from 'react'

export default function StatCard({ title, value, children }){
  return (
    <div className="bg-white shadow rounded p-4 flex-1">
      <div className="text-sm text-gray-500">{title}</div>
      <div className="text-2xl font-bold">{value}</div>
      <div className="mt-2 text-xs text-gray-400">{children}</div>
    </div>
  )
}
