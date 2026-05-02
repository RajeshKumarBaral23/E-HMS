-- V2: convert appointments.status from ORDINAL (int) to STRING values
-- Safe approach: add a new varchar column, populate it, drop old column, rename.

ALTER TABLE appointments ADD COLUMN status_new VARCHAR(32);

UPDATE appointments SET status_new = CASE status
  WHEN 0 THEN 'PENDING'
  WHEN 1 THEN 'CONFIRMED'
  WHEN 2 THEN 'CHECKED_IN'
  WHEN 3 THEN 'IN_PROGRESS'
  WHEN 4 THEN 'COMPLETED'
  WHEN 5 THEN 'CANCELLED'
  ELSE 'PENDING' END;

ALTER TABLE appointments MODIFY COLUMN status_new VARCHAR(32) NOT NULL DEFAULT 'PENDING';

-- Drop the old ordinal column and rename the new text column
ALTER TABLE appointments DROP COLUMN status;
ALTER TABLE appointments CHANGE COLUMN status_new status VARCHAR(32) NOT NULL DEFAULT 'PENDING';

-- NOTE: Backup your DB before running this migration (mysqldump recommended).
