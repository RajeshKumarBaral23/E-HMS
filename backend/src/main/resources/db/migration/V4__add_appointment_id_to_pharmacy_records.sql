-- Add appointment_id to pharmacy_records so direct purchases can join billing to appointments
ALTER TABLE pharmacy_records
  ADD COLUMN appointment_id BIGINT NULL;

-- No foreign key added here to keep migration simple for existing data.
