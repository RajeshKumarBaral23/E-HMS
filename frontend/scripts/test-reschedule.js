(async () => {
  const base = 'http://localhost:8086'
  const email = 'patient@ehealth.com'
  const password = 'patient123'

  const wait = ms => new Promise(r => setTimeout(r, ms))

  async function tryFetch(url, options = {}, retries = 20, delay = 2000) {
    for (let i = 0; i < retries; i++) {
      try {
        const res = await fetch(url, options)
        return res
      } catch (err) {
        if (i === retries - 1) throw err
        await wait(delay)
      }
    }
  }

  try {
    console.log('Logging in...')
    const loginRes = await tryFetch(`${base}/api/auth/login`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ email, password }) }, 40, 1500)
    if (!loginRes.ok) {
      console.error('Login failed:', await loginRes.text())
      process.exit(1)
    }
    const loginJson = await loginRes.json()
    const token = loginJson.token
    console.log('Got token')

    // fetch doctors
    const docsRes = await tryFetch(`${base}/api/doctors`, { headers: { Authorization: 'Bearer ' + token } })
    const docs = await docsRes.json()
    if (!Array.isArray(docs) || docs.length === 0) {
      console.error('No doctors available')
      process.exit(1)
    }
    const doctorUserId = docs[0].userId || docs[0].id || docs[0].user?.id
    console.log('Using doctor userId:', doctorUserId)

    const dt1 = new Date(); dt1.setDate(dt1.getDate()+2); dt1.setHours(10,0,0,0)
    const dt2 = new Date(); dt2.setDate(dt2.getDate()+3); dt2.setHours(11,0,0,0)

    const pad = n => String(n).padStart(2, '0')
    const toLocalDateTime = d => `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:00`

    console.log('Creating initial appointment...')
    const create1 = await tryFetch(`${base}/api/appointments`, { method: 'POST', headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token }, body: JSON.stringify({ doctorId: doctorUserId, appointmentTime: toLocalDateTime(dt1), durationMinutes: 30, reason: 'Test initial booking' }) })
    const appt1Text = await create1.text()
    let appt1
    try { appt1 = JSON.parse(appt1Text) } catch(e) { appt1 = appt1Text }
    console.log('Created appointment 1 raw:', appt1Text)
    console.log('Created appointment 1 parsed:', appt1)

    console.log('Creating rescheduled appointment...')
    const create2 = await tryFetch(`${base}/api/appointments`, { method: 'POST', headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token }, body: JSON.stringify({ doctorId: doctorUserId, appointmentTime: toLocalDateTime(dt2), durationMinutes: 30, reason: 'Test rescheduled booking' }) })
    const appt2Text = await create2.text()
    let appt2
    try { appt2 = JSON.parse(appt2Text) } catch(e) { appt2 = appt2Text }
    console.log('Created appointment 2 raw:', appt2Text)
    console.log('Created appointment 2 parsed:', appt2)

    console.log('Cancelling original appointment...')
    const cancel = await tryFetch(`${base}/api/appointments/${appt1.id}/status`, { method: 'PUT', headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token }, body: JSON.stringify({ status: 'CANCELLED' }) })
    const cancelText = await cancel.text()
    let cancelJson
    try { cancelJson = JSON.parse(cancelText) } catch(e) { cancelJson = cancelText }
    console.log('Cancelled appointment raw:', cancelText)
    console.log('Cancelled appointment parsed:', cancelJson)

    const listText = await (await tryFetch(`${base}/api/appointments`, { headers: { Authorization: 'Bearer ' + token } })).text()
    let list
    try { list = JSON.parse(listText) } catch(e) { list = listText }
    console.log('Appointments for patient raw:', listText)
    console.log('Appointments for patient parsed:', list)
    if (Array.isArray(list)) list.forEach(a => console.log(a.id, a.status, a.appointmentTime))
    process.exit(0)
  } catch (err) {
    console.error('Test failed:', err)
    process.exit(2)
  }
})()
