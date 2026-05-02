-- Migration: V7__create_bed_and_admission_tables.sql
-- Purpose: Add bed management and admission workflow tables

-- Create Bed table
CREATE TABLE IF NOT EXISTS beds (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bed_number VARCHAR(50) NOT NULL UNIQUE,
    ward VARCHAR(100) NOT NULL,
    occupied BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Admission table
CREATE TABLE IF NOT EXISTS admissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,
    bed_id BIGINT NOT NULL,
    admission_date DATE NOT NULL,
    discharge_date DATE,
    room_charge_per_day DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (bed_id) REFERENCES beds(id),
    UNIQUE KEY unique_bed_admission (bed_id, admission_date)
);

-- Alter Billing table to support aggregated billing
ALTER TABLE billings ADD COLUMN IF NOT EXISTS patient_id BIGINT;
ALTER TABLE billings ADD COLUMN IF NOT EXISTS admission_id BIGINT;
ALTER TABLE billings ADD COLUMN IF NOT EXISTS lab_charges DOUBLE DEFAULT 0.0;
ALTER TABLE billings ADD COLUMN IF NOT EXISTS room_charges DOUBLE DEFAULT 0.0;
ALTER TABLE billings ADD COLUMN IF NOT EXISTS paid_at DATETIME;

-- Add foreign key constraints
ALTER TABLE billings ADD CONSTRAINT fk_billing_patient
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE;

ALTER TABLE billings ADD CONSTRAINT fk_billing_admission
    FOREIGN KEY (admission_id) REFERENCES admissions(id) ON DELETE SET NULL;

-- Create indexes for performance
CREATE INDEX idx_admissions_patient_id ON admissions(patient_id);
CREATE INDEX idx_admissions_bed_id ON admissions(bed_id);
CREATE INDEX idx_billings_patient_id ON billings(patient_id);
CREATE INDEX idx_billings_admission_id ON billings(admission_id);
CREATE INDEX idx_billings_status ON billings(status);

