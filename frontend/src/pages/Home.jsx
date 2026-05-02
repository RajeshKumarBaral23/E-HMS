import React from 'react'
import { Link } from 'react-router-dom'
import { BuildingLibraryIcon, ClockIcon, ShieldCheckIcon, DocumentDuplicateIcon, UserGroupIcon, HandRaisedIcon } from '@heroicons/react/24/outline'

const features = [
  { title: 'Patient Management', description: 'Centralized patient records, history, and care plans.', icon: UserGroupIcon },
  { title: 'Doctor Scheduling', description: 'Streamline doctor availability, shifts, and appointments.', icon: ClockIcon },
  { title: 'Appointment Booking', description: 'Easy booking flow for patients and real-time updates.', icon: BuildingLibraryIcon },
  { title: 'Digital Prescriptions', description: 'Secure prescriptions and medication details in one place.', icon: DocumentDuplicateIcon },
  { title: 'Medical Records', description: 'Access clinical summaries, notes, and records instantly.', icon: ShieldCheckIcon },
  { title: 'Secure Authentication', description: 'JWT-backed login with role-based access and privacy controls.', icon: HandRaisedIcon },
]

export default function Home() {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100">
      <header className="border-b border-slate-800">
        <div className="container flex flex-wrap items-center justify-between py-6">
          <Link to="/" className="text-2xl font-bold tracking-tight">E-Healthcare</Link>
          <div className="flex items-center gap-4">
            <Link to="/#features" className="text-sm hover:text-white">Features</Link>
            <Link to="/login" className="text-sm hover:text-white">Login</Link>
            <Link to="/register" className="rounded-lg bg-cyan-500 px-4 py-2 text-sm font-semibold text-slate-950 hover:bg-cyan-400">Register</Link>
          </div>
        </div>
      </header>

      <main className="container py-20">
        <section className="grid gap-10 lg:grid-cols-[1.2fr_0.8fr] items-center">
          <div className="space-y-8">
            <div className="inline-flex rounded-full bg-cyan-500/10 px-4 py-1 text-xs uppercase tracking-[0.24em] text-cyan-300">Healthcare SaaS</div>
            <h1 className="text-5xl font-semibold leading-tight text-white">Smart Healthcare Management System</h1>
            <p className="max-w-2xl text-lg text-slate-300">A centralized platform to manage patients, doctors, and real-time appointments with secure digital medical records — designed to improve healthcare efficiency and patient experience.</p>
            <div className="flex flex-wrap gap-4">
              <Link to="/login" className="inline-flex items-center justify-center rounded-xl bg-cyan-500 px-6 py-3 text-sm font-semibold text-slate-950 transition hover:bg-cyan-400">Get Started</Link>
              <Link to="/login" className="inline-flex items-center justify-center rounded-xl border border-slate-700 bg-slate-900 px-6 py-3 text-sm text-slate-200 transition hover:border-slate-500">Book Appointment</Link>
            </div>
            
            <div className="mt-8 pt-8 border-t border-slate-800">
              <p className="mb-4 text-sm text-slate-400">Quick access by role:</p>
              <div className="flex flex-wrap gap-3">
                <Link to="/login/admin" className="inline-flex items-center justify-center rounded-lg bg-slate-800 px-4 py-2 text-sm font-medium text-slate-100 transition hover:bg-slate-700">Admin Login</Link>
                <Link to="/login/doctor" className="inline-flex items-center justify-center rounded-lg bg-slate-800 px-4 py-2 text-sm font-medium text-slate-100 transition hover:bg-slate-700">Doctor Login</Link>
                <Link to="/login/patient" className="inline-flex items-center justify-center rounded-lg bg-slate-800 px-4 py-2 text-sm font-medium text-slate-100 transition hover:bg-slate-700">Patient Login</Link>
                <Link to="/register" className="inline-flex items-center justify-center rounded-lg bg-cyan-600 px-4 py-2 text-sm font-medium text-slate-950 transition hover:bg-cyan-500">Patient Register</Link>
              </div>
            </div>
          </div>
          <div className="rounded-[2rem] bg-slate-900 p-10 shadow-xl ring-1 ring-white/10">
            <div className="space-y-6">
              <div className="rounded-3xl border border-white/5 bg-gradient-to-br from-cyan-500/10 to-sky-500/5 p-6 text-slate-100">
                <div className="text-sm uppercase tracking-[0.26em] text-cyan-200">SaaS snapshot</div>
                <div className="mt-4 text-2xl font-semibold">Healthcare workflows in one secure hub.</div>
                <p className="mt-3 text-sm text-slate-300">Role-based dashboards for administrators, doctors, and patients with modern telehealth workflows.</p>
              </div>
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="rounded-3xl bg-slate-950 p-5 ring-1 ring-white/10">
                  <div className="text-xs uppercase text-slate-400">Trusted by</div>
                  <div className="mt-3 text-2xl font-semibold">350+</div>
                  <div className="text-sm text-slate-500">Healthcare teams</div>
                </div>
                <div className="rounded-3xl bg-slate-950 p-5 ring-1 ring-white/10">
                  <div className="text-xs uppercase text-slate-400">Global bookings</div>
                  <div className="mt-3 text-2xl font-semibold">12K+</div>
                  <div className="text-sm text-slate-500">Secure appointments</div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section id="features" className="mt-24">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <p className="text-sm uppercase tracking-[0.32em] text-cyan-400">Features</p>
              <h2 className="mt-3 text-3xl font-semibold text-white">Everything your clinic needs to run smoothly.</h2>
            </div>
            <p className="max-w-xl text-slate-400">Optimize care delivery with patient tracking, secure digital records, and automated scheduling for doctors and staff.</p>
          </div>
          <div className="mt-10 grid gap-6 md:grid-cols-2 xl:grid-cols-3">
            {features.map(feature => (
              <article key={feature.title} className="rounded-3xl border border-white/5 bg-slate-900 p-6 shadow-md transition hover:-translate-y-1 hover:border-cyan-500/30">
                <div className="inline-flex rounded-2xl bg-cyan-500/10 p-3 text-cyan-300">
                  <feature.icon className="h-6 w-6" />
                </div>
                <h3 className="mt-5 text-xl font-semibold text-white">{feature.title}</h3>
                <p className="mt-3 text-sm leading-6 text-slate-400">{feature.description}</p>
              </article>
            ))}
          </div>
        </section>

        <section className="mt-24 grid gap-10 xl:grid-cols-[0.75fr_1fr]">
          <div className="rounded-3xl bg-slate-900 p-8 shadow-xl ring-1 ring-white/10">
            <h2 className="text-3xl font-semibold text-white">Built for each role</h2>
            <div className="mt-8 space-y-4">
              <div className="rounded-3xl bg-slate-950 p-5">
                <h3 className="text-lg font-semibold text-white">Patient</h3>
                <p className="mt-2 text-slate-400">Book appointments, review medical history, and access prescriptions in one secure place.</p>
              </div>
              <div className="rounded-3xl bg-slate-950 p-5">
                <h3 className="text-lg font-semibold text-white">Doctor</h3>
                <p className="mt-2 text-slate-400">Manage appointments, review patients, and create prescriptions with clinical confidence.</p>
              </div>
              <div className="rounded-3xl bg-slate-950 p-5">
                <h3 className="text-lg font-semibold text-white">Admin</h3>
                <p className="mt-2 text-slate-400">Oversee clinics, users, workflow metrics, and secure access policies from a single dashboard.</p>
              </div>
            </div>
          </div>
          <div className="grid gap-6">
            <div className="rounded-3xl bg-slate-900 p-8 shadow-xl ring-1 ring-white/10">
              <h3 className="text-lg font-semibold text-white">Trusted metrics</h3>
              <div className="mt-8 grid gap-4 sm:grid-cols-2">
                <div className="rounded-3xl bg-slate-950 p-6">
                  <div className="text-sm uppercase text-slate-400">Patients count</div>
                  <div className="mt-4 text-3xl font-semibold text-white">1,824</div>
                </div>
                <div className="rounded-3xl bg-slate-950 p-6">
                  <div className="text-sm uppercase text-slate-400">Doctors count</div>
                  <div className="mt-4 text-3xl font-semibold text-white">214</div>
                </div>
                <div className="rounded-3xl bg-slate-950 p-6">
                  <div className="text-sm uppercase text-slate-400">Appointments</div>
                  <div className="mt-4 text-3xl font-semibold text-white">8,130</div>
                </div>
                <div className="rounded-3xl bg-slate-950 p-6">
                  <div className="text-sm uppercase text-slate-400">Security</div>
                  <div className="mt-4 text-3xl font-semibold text-white">JWT & RBAC</div>
                </div>
              </div>
            </div>
            <div className="rounded-3xl bg-slate-900 p-8 shadow-xl ring-1 ring-white/10">
              <h3 className="text-lg font-semibold text-white">Why it works</h3>
              <ul className="mt-6 space-y-4 text-slate-400">
                <li>✓ Role-aware access for admin, doctor, and patient workflows.</li>
                <li>✓ Secure token-based login with centralized data management.</li>
                <li>✓ Modern responsive design built with React and Tailwind.</li>
              </ul>
            </div>
          </div>
        </section>
      </main>
    </div>
  )
}
