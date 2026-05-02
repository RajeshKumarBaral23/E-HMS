import React from 'react'
import { NavLink } from 'react-router-dom'
import { HomeIcon, UserIcon, CalendarDaysIcon, DocumentTextIcon, BuildingOffice2Icon, BuildingStorefrontIcon, CreditCardIcon, ChartBarIcon, ClipboardDocumentListIcon } from '@heroicons/react/24/outline'
import useAuthStore from '../store/authStore'

export default function Sidebar() {
  const user = useAuthStore(state => state.user)
  const role = user?.role

  const adminLinks = [
    { to: '/admin-dashboard', label: 'Dashboard', icon: <HomeIcon className="w-5 h-5" /> },
    { to: '/patients', label: 'Patients', icon: <UserIcon className="w-5 h-5" /> },
    { to: '/doctors', label: 'Doctors', icon: <BuildingOffice2Icon className="w-5 h-5" /> },
    { to: '/departments', label: 'Departments', icon: <BuildingOffice2Icon className="w-5 h-5" /> },
    { to: '/appointments', label: 'Appointments', icon: <CalendarDaysIcon className="w-5 h-5" /> },
    { to: '/prescriptions', label: 'Prescriptions', icon: <DocumentTextIcon className="w-5 h-5" /> },
    { to: '/billing', label: 'Billing', icon: <CreditCardIcon className="w-5 h-5" /> },
    { to: '/pharmacy', label: 'Pharmacy', icon: <BuildingStorefrontIcon className="w-5 h-5" /> },
    { to: '/lab-results', label: 'Lab Results', icon: <DocumentTextIcon className="w-5 h-5" /> },
    { to: '/discharge-summaries', label: 'Discharge', icon: <DocumentTextIcon className="w-5 h-5" /> },
    { to: '/medical-history', label: 'Medical Records', icon: <ClipboardDocumentListIcon className="w-5 h-5" /> },
    { to: '/admin-reports', label: 'Reports', icon: <ChartBarIcon className="w-5 h-5" /> },
  ]

  const doctorLinks = [
    { to: '/doctor-dashboard', label: 'Dashboard', icon: <HomeIcon className="w-5 h-5" /> },
    { to: '/appointments', label: 'Appointments', icon: <CalendarDaysIcon className="w-5 h-5" /> },
    { to: '/patients', label: 'Patients', icon: <UserIcon className="w-5 h-5" /> },
    { to: '/prescriptions', label: 'Prescriptions', icon: <DocumentTextIcon className="w-5 h-5" /> },
    { to: '/billing', label: 'Billing', icon: <CreditCardIcon className="w-5 h-5" /> },
    { to: '/lab-results', label: 'Lab Results', icon: <DocumentTextIcon className="w-5 h-5" /> },
    { to: '/discharge-summaries', label: 'Discharge', icon: <DocumentTextIcon className="w-5 h-5" /> },
    { to: '/medical-history', label: 'Medical Records', icon: <ClipboardDocumentListIcon className="w-5 h-5" /> },
  ]

  const patientLinks = [
    { to: '/patient-dashboard', label: 'Dashboard', icon: <HomeIcon className="w-5 h-5" /> },
    { to: '/appointments', label: 'Appointments', icon: <CalendarDaysIcon className="w-5 h-5" /> },
    { to: '/prescriptions', label: 'Prescriptions', icon: <DocumentTextIcon className="w-5 h-5" /> },
    { to: '/billing', label: 'Billing', icon: <CreditCardIcon className="w-5 h-5" /> },
    { to: '/pharmacy', label: 'Pharmacy', icon: <BuildingStorefrontIcon className="w-5 h-5" /> },
    { to: '/lab-results', label: 'Lab Results', icon: <DocumentTextIcon className="w-5 h-5" /> },
    { to: '/discharge-summaries', label: 'Discharge', icon: <DocumentTextIcon className="w-5 h-5" /> },
    { to: '/medical-history', label: 'Medical History', icon: <ClipboardDocumentListIcon className="w-5 h-5" /> },
  ]

  const links = role === 'ADMIN' ? adminLinks : role === 'DOCTOR' ? doctorLinks : patientLinks

  return (
    <aside className="w-72 bg-slate-950 text-slate-100 border-r border-slate-800 hidden md:block">
      <div className="p-6">
        <div className="text-2xl font-bold">E-Healthcare</div>
        <div className="mt-3 rounded-3xl bg-slate-900 p-4 text-sm">
          <div className="text-slate-400">{user?.name || 'Guest'}</div>
          <div className="mt-1 text-slate-200">{role ? role.toLowerCase() : 'visitor'} dashboard</div>
        </div>
      </div>
      <nav className="px-4 pb-6">
        {links.map(link => (
          <NavLink key={link.to} to={link.to} className={({isActive}) => `flex items-center gap-3 rounded-2xl px-4 py-3 text-sm transition ${isActive ? 'bg-cyan-500/10 text-cyan-300 font-semibold' : 'text-slate-300 hover:bg-slate-900 hover:text-white'}`}>
            {link.icon}
            <span>{link.label}</span>
          </NavLink>
        ))}
      </nav>
    </aside>
  )
}
