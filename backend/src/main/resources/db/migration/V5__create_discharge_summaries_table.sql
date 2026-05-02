-- Create discharge_summaries table for final patient discharge records
CREATE TABLE IF NOT EXISTS discharge_summaries (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  appointment_id BIGINT,
  patient_id BIGINT,
  doctor_id BIGINT,
  summary LONGTEXT,
  instructions LONGTEXT,
  discharge_date DATETIME,
  follow_up_date DATE,
  created_at DATETIME,
  CONSTRAINT fk_discharge_summaries_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id),
  CONSTRAINT fk_discharge_summaries_patient FOREIGN KEY (patient_id) REFERENCES users(id),
  CONSTRAINT fk_discharge_summaries_doctor FOREIGN KEY (doctor_id) REFERENCES users(id)
);
