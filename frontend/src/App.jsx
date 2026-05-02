import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import Home from './pages/Home'
import Login from './pages/Login'
import Register from './pages/Register'
import AdminDashboard from './pages/AdminDashboard'
import DoctorDashboard from './pages/DoctorDashboard'
import PatientDashboard from './pages/PatientDashboard'
import Patients from './pages/Patients'
import Doctors from './pages/Doctors'
import Departments from './pages/Departments'
import Appointments from './pages/Appointments'
import Prescriptions from './pages/Prescriptions'
import Pharmacy from './pages/Pharmacy'
import Billing from './pages/Billing'
import LabResults from './pages/LabResults'
import DischargeSummaries from './pages/DischargeSummaries'
import MedicalHistory from './pages/MedicalHistory'
import AdminReports from './pages/AdminReports'
import DashboardLayout from './layouts/DashboardLayout'
import useAuthStore from './store/authStore'

function RequireAuth({ children }) {
  const token = useAuthStore(state => state.token)
  return token ? children : <Navigate to="/login" replace />
}

function RequireRole({ role, children }) {
  const user = useAuthStore(state => state.user)
  if (!user) return <Navigate to="/login" replace />
  return user.role === role ? children : <Navigate to="/" replace />
}

function RequireAnyRole({ roles, children }) {
  const user = useAuthStore(state => state.user)
  if (!user) return <Navigate to="/login" replace />
  return roles.includes(user.role) ? children : <Navigate to="/" replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/login/:role" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route element={<RequireAuth><DashboardLayout /></RequireAuth>}>
        <Route path="/admin-dashboard" element={<RequireRole role="ADMIN"><AdminDashboard /></RequireRole>} />
        <Route path="/doctor-dashboard" element={<RequireRole role="DOCTOR"><DoctorDashboard /></RequireRole>} />
        <Route path="/patient-dashboard" element={<RequireRole role="PATIENT"><PatientDashboard /></RequireRole>} />
        <Route path="/patients" element={<Patients />} />
        <Route path="/doctors" element={<Doctors />} />
        <Route path="/departments" element={<Departments />} />
        <Route path="/appointments" element={<Appointments />} />
        <Route path="/prescriptions" element={<Prescriptions />} />
        <Route path="/pharmacy" element={<RequireAnyRole roles={['ADMIN', 'PATIENT']}><Pharmacy /></RequireAnyRole>} />
        <Route path="/lab-results" element={<RequireAnyRole roles={['ADMIN', 'DOCTOR', 'PATIENT']}><LabResults /></RequireAnyRole>} />
        <Route path="/discharge-summaries" element={<RequireAnyRole roles={['ADMIN', 'DOCTOR', 'PATIENT']}><DischargeSummaries /></RequireAnyRole>} />
        <Route path="/billing" element={<Billing />} />
        <Route path="/medical-history" element={<MedicalHistory />} />
        <Route path="/admin-reports" element={<RequireRole role="ADMIN"><AdminReports /></RequireRole>} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
