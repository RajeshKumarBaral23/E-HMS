-- Insert departments if they do not already exist
INSERT INTO departments (name, description, phone)
SELECT 'Cardiology', 'Heart and cardiovascular disease treatment', '555-2001'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = 'Cardiology');

INSERT INTO departments (name, description, phone)
SELECT 'Pediatrics', 'Child health and development specialists', '555-2002'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = 'Pediatrics');

INSERT INTO departments (name, description, phone)
SELECT 'Orthopedics', 'Bone and joint disorder specialists', '555-2003'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = 'Orthopedics');

INSERT INTO departments (name, description, phone)
SELECT 'Dermatology', 'Skin condition and disorder treatment', '555-2004'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = 'Dermatology');

INSERT INTO departments (name, description, phone)
SELECT 'General Surgery', 'Surgical procedures and interventions', '555-2005'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = 'General Surgery');

-- Insert sample users (doctors) if they do not already exist
INSERT INTO users (name, email, password, role)
SELECT 'Dr. John Smith', 'john.smith@hospital.com', '$2a$10$slYQmyNdGzin7olVN3p5Be7DWRvEJiHf7/N8qF8nK2H5E8F9m5W1K', 'DOCTOR'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'john.smith@hospital.com');

INSERT INTO users (name, email, password, role)
SELECT 'Dr. Sarah Johnson', 'sarah.johnson@hospital.com', '$2a$10$slYQmyNdGzin7olVN3p5Be7DWRvEJiHf7/N8qF8nK2H5E8F9m5W1K', 'DOCTOR'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'sarah.johnson@hospital.com');

INSERT INTO users (name, email, password, role)
SELECT 'Dr. Michael Chen', 'michael.chen@hospital.com', '$2a$10$slYQmyNdGzin7olVN3p5Be7DWRvEJiHf7/N8qF8nK2H5E8F9m5W1K', 'DOCTOR'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'michael.chen@hospital.com');

INSERT INTO users (name, email, password, role)
SELECT 'Dr. Emma Davis', 'emma.davis@hospital.com', '$2a$10$slYQmyNdGzin7olVN3p5Be7DWRvEJiHf7/N8qF8nK2H5E8F9m5W1K', 'DOCTOR'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'emma.davis@hospital.com');

INSERT INTO users (name, email, password, role)
SELECT 'Dr. Robert Wilson', 'robert.wilson@hospital.com', '$2a$10$slYQmyNdGzin7olVN3p5Be7DWRvEJiHf7/N8qF8nK2H5E8F9m5W1K', 'DOCTOR'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'robert.wilson@hospital.com');

INSERT INTO users (name, email, password, role)
SELECT 'Dr. Lisa Anderson', 'lisa.anderson@hospital.com', '$2a$10$slYQmyNdGzin7olVN3p5Be7DWRvEJiHf7/N8qF8nK2H5E8F9m5W1K', 'DOCTOR'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'lisa.anderson@hospital.com');

INSERT INTO users (name, email, password, role)
SELECT 'Dr. James Martinez', 'james.martinez@hospital.com', '$2a$10$slYQmyNdGzin7olVN3p5Be7DWRvEJiHf7/N8qF8nK2H5E8F9m5W1K', 'DOCTOR'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'james.martinez@hospital.com');

INSERT INTO users (name, email, password, role)
SELECT 'Dr. Patricia Taylor', 'patricia.taylor@hospital.com', '$2a$10$slYQmyNdGzin7olVN3p5Be7DWRvEJiHf7/N8qF8nK2H5E8F9m5W1K', 'DOCTOR'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'patricia.taylor@hospital.com');

INSERT INTO users (name, email, password, role)
SELECT 'Dr. David Lee', 'david.lee@hospital.com', '$2a$10$slYQmyNdGzin7olVN3p5Be7DWRvEJiHf7/N8qF8nK2H5E8F9m5W1K', 'DOCTOR'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'david.lee@hospital.com');

INSERT INTO users (name, email, password, role)
SELECT 'Dr. Maria Garcia', 'maria.garcia@hospital.com', '$2a$10$slYQmyNdGzin7olVN3p5Be7DWRvEJiHf7/N8qF8nK2H5E8F9m5W1K', 'DOCTOR'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'maria.garcia@hospital.com');

-- Insert doctors if they do not already exist
INSERT INTO doctors (user_id, department_id, specialization, phone, bio)
SELECT u.id, d.id, 'Interventional Cardiology', '555-0101', 'Experienced interventional cardiologist with 15 years of practice'
FROM users u
JOIN departments d ON d.name = 'Cardiology'
WHERE u.email = 'john.smith@hospital.com'
	AND NOT EXISTS (SELECT 1 FROM doctors existing WHERE existing.user_id = u.id);

INSERT INTO doctors (user_id, department_id, specialization, phone, bio)
SELECT u.id, d.id, 'Pediatric Cardiology', '555-0102', 'Specialist in congenital heart diseases'
FROM users u
JOIN departments d ON d.name = 'Cardiology'
WHERE u.email = 'sarah.johnson@hospital.com'
	AND NOT EXISTS (SELECT 1 FROM doctors existing WHERE existing.user_id = u.id);

INSERT INTO doctors (user_id, department_id, specialization, phone, bio)
SELECT u.id, d.id, 'General Pediatrics', '555-0103', 'Expert in child health and development'
FROM users u
JOIN departments d ON d.name = 'Pediatrics'
WHERE u.email = 'michael.chen@hospital.com'
	AND NOT EXISTS (SELECT 1 FROM doctors existing WHERE existing.user_id = u.id);

INSERT INTO doctors (user_id, department_id, specialization, phone, bio)
SELECT u.id, d.id, 'Neonatology', '555-0104', 'Specializes in newborn care'
FROM users u
JOIN departments d ON d.name = 'Pediatrics'
WHERE u.email = 'emma.davis@hospital.com'
	AND NOT EXISTS (SELECT 1 FROM doctors existing WHERE existing.user_id = u.id);

INSERT INTO doctors (user_id, department_id, specialization, phone, bio)
SELECT u.id, d.id, 'Orthopedic Surgery', '555-0105', 'Expert in bone and joint disorders'
FROM users u
JOIN departments d ON d.name = 'Orthopedics'
WHERE u.email = 'robert.wilson@hospital.com'
	AND NOT EXISTS (SELECT 1 FROM doctors existing WHERE existing.user_id = u.id);

INSERT INTO doctors (user_id, department_id, specialization, phone, bio)
SELECT u.id, d.id, 'Sports Medicine', '555-0106', 'Specializes in sports-related injuries'
FROM users u
JOIN departments d ON d.name = 'Orthopedics'
WHERE u.email = 'lisa.anderson@hospital.com'
	AND NOT EXISTS (SELECT 1 FROM doctors existing WHERE existing.user_id = u.id);

INSERT INTO doctors (user_id, department_id, specialization, phone, bio)
SELECT u.id, d.id, 'Medical Dermatology', '555-0107', 'Treats skin conditions and disorders'
FROM users u
JOIN departments d ON d.name = 'Dermatology'
WHERE u.email = 'james.martinez@hospital.com'
	AND NOT EXISTS (SELECT 1 FROM doctors existing WHERE existing.user_id = u.id);

INSERT INTO doctors (user_id, department_id, specialization, phone, bio)
SELECT u.id, d.id, 'Cosmetic Dermatology', '555-0108', 'Specialist in aesthetic skin procedures'
FROM users u
JOIN departments d ON d.name = 'Dermatology'
WHERE u.email = 'patricia.taylor@hospital.com'
	AND NOT EXISTS (SELECT 1 FROM doctors existing WHERE existing.user_id = u.id);

INSERT INTO doctors (user_id, department_id, specialization, phone, bio)
SELECT u.id, d.id, 'General Surgery', '555-0109', 'Experienced surgical specialist'
FROM users u
JOIN departments d ON d.name = 'General Surgery'
WHERE u.email = 'david.lee@hospital.com'
	AND NOT EXISTS (SELECT 1 FROM doctors existing WHERE existing.user_id = u.id);

INSERT INTO doctors (user_id, department_id, specialization, phone, bio)
SELECT u.id, d.id, 'Laparoscopic Surgery', '555-0110', 'Expert in minimally invasive procedures'
FROM users u
JOIN departments d ON d.name = 'General Surgery'
WHERE u.email = 'maria.garcia@hospital.com'
	AND NOT EXISTS (SELECT 1 FROM doctors existing WHERE existing.user_id = u.id);

