-- remove_doctors_and_patients.sql
-- Purpose: Remove all DOCTOR and PATIENT users and associated data, but keep ADMIN users and departments.
-- WARNING: Destructive. Run only after taking a backup.
-- Usage (mysql client):
-- mysql -u root -p ehealth < remove_doctors_and_patients.sql

SET @confirm := 0; -- set to 1 to execute deletions (safety switch)

-- Show counts (dry run)
SELECT 'USERS_TO_DELETE' AS description, COUNT(*) AS count
FROM users
WHERE role IN ('DOCTOR','PATIENT');

SELECT 'APPOINTMENTS_TO_DELETE' AS description, COUNT(*) AS count
FROM appointments
WHERE doctor_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT'))
   OR patient_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT'));

SELECT 'PRESCRIPTIONS_TO_DELETE' AS description, COUNT(*) AS count
FROM prescriptions
WHERE doctor_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT'))
   OR patient_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT'));

SELECT 'LAB_RESULTS_TO_DELETE' AS description, COUNT(*) AS count
FROM lab_results
WHERE doctor_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT'))
   OR patient_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT'));

SELECT 'MEDICAL_RECORDS_TO_DELETE' AS description, COUNT(*) AS count
FROM medical_records
WHERE doctor_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT'))
   OR patient_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT'));

SELECT 'DISCHARGE_SUMMARIES_TO_DELETE' AS description, COUNT(*) AS count
FROM discharge_summaries
WHERE doctor_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT'))
   OR patient_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT'));

SELECT 'AVAILABILITY_SLOTS_TO_DELETE' AS description, COUNT(*) AS count
FROM availability_slots
WHERE doctor_id IN (SELECT id FROM users WHERE role = 'DOCTOR');

SELECT 'PRESCRIPTION_MEDICINES_TO_DELETE (via prescriptions)' AS description, COUNT(*) AS count
FROM prescription_medicines pm
JOIN prescriptions p ON pm.prescription_id = p.id
WHERE p.doctor_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT'))
   OR p.patient_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT'));

SELECT 'BILLINGS_TO_DELETE' AS description, COUNT(*) AS count
FROM billings
WHERE patient_id IN (SELECT id FROM users WHERE role = 'PATIENT');

SELECT 'PHARMACY_RECORDS_TO_DELETE' AS description, COUNT(*) AS count
FROM pharmacy_records
WHERE patient_id IN (SELECT id FROM users WHERE role = 'PATIENT');

-- If you are satisfied with the dry-run counts, set @confirm := 1 and re-run this script to perform deletions.
-- The following deletion block runs only when @confirm = 1

-- Begin destructive section
-- Use transactions when supported
START TRANSACTION;

-- delete child rows that reference prescriptions
DELETE pm FROM prescription_medicines pm
JOIN prescriptions p ON pm.prescription_id = p.id
WHERE (p.doctor_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT'))
   OR p.patient_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT')))
AND @confirm = 1;

-- prescriptions
DELETE FROM prescriptions
WHERE (doctor_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT'))
   OR patient_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT')))
AND @confirm = 1;

-- lab results
DELETE FROM lab_results
WHERE (doctor_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT'))
   OR patient_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT')))
AND @confirm = 1;

-- medical_records
DELETE FROM medical_records
WHERE (doctor_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT'))
   OR patient_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT')))
AND @confirm = 1;

-- discharge_summaries
DELETE FROM discharge_summaries
WHERE (doctor_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT'))
   OR patient_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT')))
AND @confirm = 1;

-- availability_slots (doctors only)
DELETE FROM availability_slots
WHERE doctor_id IN (SELECT id FROM users WHERE role = 'DOCTOR')
AND @confirm = 1;

-- appointments
DELETE FROM appointments
WHERE doctor_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT'))
   OR patient_id IN (SELECT id FROM users WHERE role IN ('DOCTOR','PATIENT'))
AND @confirm = 1;

-- pharmacy_records
DELETE FROM pharmacy_records
WHERE patient_id IN (SELECT id FROM users WHERE role = 'PATIENT')
AND @confirm = 1;

-- billings
DELETE FROM billings
WHERE patient_id IN (SELECT id FROM users WHERE role = 'PATIENT')
AND @confirm = 1;

-- doctors and patients profile rows
DELETE FROM doctors d
WHERE d.user_id IN (SELECT id FROM users WHERE role = 'DOCTOR')
AND @confirm = 1;

DELETE FROM patients p
WHERE p.user_id IN (SELECT id FROM users WHERE role = 'PATIENT')
AND @confirm = 1;

-- finally delete user accounts with roles DOCTOR or PATIENT
DELETE FROM users
WHERE role IN ('DOCTOR','PATIENT')
AND @confirm = 1;

COMMIT;

-- End of script
