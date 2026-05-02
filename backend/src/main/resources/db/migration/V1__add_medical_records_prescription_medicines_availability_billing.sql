-- Migration to add medical_records, prescription_medicines, availability_slots and payment_status
CREATE TABLE IF NOT EXISTS medical_records (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  patient_id BIGINT,
  doctor_id BIGINT,
  diagnosis VARCHAR(1024),
  treatment VARCHAR(1024),
  notes TEXT,
  visit_date DATETIME,
  created_at DATETIME,
  INDEX (patient_id),
  INDEX (doctor_id)
);

CREATE TABLE IF NOT EXISTS availability_slots (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  doctor_id BIGINT,
  start_date_time DATETIME,
  end_date_time DATETIME,
  active BOOLEAN DEFAULT TRUE,
  created_at DATETIME,
  INDEX (doctor_id)
);

CREATE TABLE IF NOT EXISTS prescription_medicines (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  prescription_id BIGINT,
  medicine_id BIGINT,
  dosage VARCHAR(255),
  duration_days INT,
  instructions TEXT,
  INDEX (prescription_id),
  INDEX (medicine_id)
);

-- Add payment_status to billings if not present
ALTER TABLE billings ADD COLUMN IF NOT EXISTS payment_status VARCHAR(32);
