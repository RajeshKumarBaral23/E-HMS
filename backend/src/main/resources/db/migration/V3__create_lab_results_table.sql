-- Create lab_results table to store lab test results linked to patients, doctors and appointments
CREATE TABLE IF NOT EXISTS lab_results (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  patient_id BIGINT,
  doctor_id BIGINT,
  appointment_id BIGINT,
  test_type VARCHAR(50),
  result LONGTEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_lab_results_patient FOREIGN KEY (patient_id) REFERENCES users(id),
  CONSTRAINT fk_lab_results_doctor FOREIGN KEY (doctor_id) REFERENCES users(id),
  CONSTRAINT fk_lab_results_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);
