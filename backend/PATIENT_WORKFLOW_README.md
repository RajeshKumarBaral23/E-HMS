# Hospital Management System - Complete Patient Workflow Implementation

## 📋 Overview

This document describes the complete real-life patient workflow implementation in the Hospital Management System (HMS), covering admission, billing aggregation, and discharge summary generation.

---

## 🏥 Patient Workflow Steps

### 1. **Patient Registration & Login**
- Patient registers via `/api/auth/register`
- Patient logs in via `/api/auth/login`
- JWT token issued for authenticated requests

### 2. **Appointment Booking**
- Patient books appointment: `POST /api/appointments`
- Doctor assigned to appointment
- Appointment scheduled

### 3. **Doctor Consultation**
- Doctor views appointment: `GET /api/appointments`
- Doctor consults with patient
- Consultation completed

### 4. **Prescription Creation**
- Doctor creates prescription: `POST /api/prescriptions`
- Medications and notes recorded
- Patient notified about prescription

### 5. **Lab Tests (Optional)**
- Doctor requests lab tests in prescription notes
- Lab technician creates lab result: `POST /api/lab-results`
- Results uploaded with file attachment support

### 6. **Pharmacy Dispensing**
- Pharmacy processes prescription: `POST /api/pharmacy-records`
- Medicines dispensed to patient
- Stock updated

### 7. **Admission (If Required)** ⭐ NEW
```
POST /api/admissions
{
  "patientId": 5,
  "bedId": 12,
  "admissionDate": "2026-04-26",
  "roomChargePerDay": 1000.0
}
```
- Patient admitted to hospital
- Bed assigned and marked as occupied
- Admission date recorded
- Room charges per day set

### 8. **Aggregated Billing** ⭐ NEW
```
POST /api/billing/generate-aggregated?patientId=5&admissionId=7
```
**Bill includes:**
- **Consultation Fee**: Doctor charge (default ₹100)
- **Medicine Cost**: Sum of medicine prices from pharmacy records
- **Lab Charges**: Number of lab tests × ₹500 per test
- **Room Charges**: (Discharge date - Admission date) × Room charge per day
- **Total Amount**: Sum of all charges

**Response:**
```json
{
  "id": 15,
  "patientId": 5,
  "admissionId": 7,
  "consultationFee": 100.0,
  "medicineCost": 2000.0,
  "labCharges": 1000.0,
  "roomCharges": 3000.0,
  "totalAmount": 6100.0,
  "status": "UNPAID",
  "createdAt": "2026-04-26T10:30:00"
}
```

### 9. **Payment** ⭐ EXTENDED
```
PUT /api/billing/{billId}/pay
```
- Bill marked as PAID
- Payment timestamp recorded
- Payment status updated

### 10. **Discharge & Summary** ⭐ NEW
```
PUT /api/admissions/{admissionId}/discharge?dischargeDate=2026-04-29
```
- Discharge date recorded
- Bed marked as unoccupied

**Get Discharge Summary:**
```
GET /api/discharge/summary?patientId=5&admissionId=7
```

**Response:**
```json
{
  "patientName": "John Doe",
  "diagnosis": "Fever, Common Cold",
  "medicines": ["Paracetamol 500mg", "Cough Syrup"],
  "labTests": ["BLOOD", "URINE"],
  "totalBill": 6100.0,
  "dischargeDate": "2026-04-29"
}
```

---

## 📊 Database Schema

```
Patient (User) 
    ↓
Appointment 
    ↓ (Doctor)
Prescription 
    ↓ (PrescriptionMedicine)
Pharmacy Record ← Medicine
    ↓
Billing (aggregates all)
    ↓
    Admission → Bed
    ↓
    LabResult
    ↓
DischargeSummary
```

---

## 🔌 API Endpoints Summary

### Admission Management
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/admissions` | Admit patient to hospital |
| PUT | `/api/admissions/{id}/discharge` | Discharge patient |
| GET | `/api/admissions/patient/{patientId}` | Get admission history |

### Billing
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/billing/generate/{appointmentId}` | Generate simple bill |
| POST | `/api/billing/generate-aggregated` | Generate aggregated bill (admission) |
| GET | `/api/billing` | List bills for current user |
| GET | `/api/billing/patient/{patientId}` | List bills for specific patient |
| PUT | `/api/billing/{id}/pay` | Mark bill as paid |

### Discharge
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/discharge/summary` | Get discharge summary |

---

## 🗄️ New Database Tables

### Beds Table
```sql
CREATE TABLE beds (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bed_number VARCHAR(50),
    ward VARCHAR(100),
    occupied BOOLEAN
);
```

### Admissions Table
```sql
CREATE TABLE admissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT,
    bed_id BIGINT,
    admission_date DATE,
    discharge_date DATE,
    room_charge_per_day DOUBLE
);
```

### Updated Billings Table
```sql
ALTER TABLE billings ADD COLUMN patient_id BIGINT;
ALTER TABLE billings ADD COLUMN admission_id BIGINT;
ALTER TABLE billings ADD COLUMN lab_charges DOUBLE;
ALTER TABLE billings ADD COLUMN room_charges DOUBLE;
ALTER TABLE billings ADD COLUMN paid_at DATETIME;
```

---

## 📂 New Files Created

### Entities
- `hospital/entity/Bed.java`
- `hospital/entity/Admission.java`

### Repositories
- `hospital/repository/BedRepository.java`
- `hospital/repository/AdmissionRepository.java`
- Updated: `BillingRepository`, `PrescriptionRepository`, `LabResultRepository`

### DTOs
- `hospital/dto/AdmissionDTO.java`
- `hospital/dto/DischargeSummaryDTO.java`
- `hospital/dto/BillingAggregationDTO.java`

### Services
- `hospital/service/AdmissionService.java`
- `hospital/service/AdmissionServiceImpl.java`
- `hospital/service/DischargeService.java`
- `hospital/service/DischargeServiceImpl.java`
- Updated: `BillingService`, `BillingServiceImpl`

### Controllers
- `hospital/controller/AdmissionController.java`
- `hospital/controller/DischargeController.java`
- Updated: `BillingController`

### Migration Scripts
- `db/migration/V7__create_bed_and_admission_tables.sql`

### Documentation
- `hospital/workflow/WorkflowDocumentation.java`
- `hospital/workflow/IntegrationGuide.java`

---

## 🧪 Test Scenarios

### Scenario 1: Simple Outpatient
```
1. Patient books appointment
2. Doctor consults and prescribes medicine
3. Pharmacy dispenses medicine
4. Bill generated (consultation + medicine)
5. Patient pays bill
```

### Scenario 2: Complete Admission Workflow
```
1. Patient books appointment
2. Doctor consults and creates prescription
3. Doctor requests lab tests
4. Lab technician uploads results
5. Pharmacy dispenses medicines
6. Admin admits patient to bed
7. Aggregated bill generated (doctor + lab + medicine + room)
8. Patient pays bill
9. Patient discharged
10. Discharge summary generated
```

### Scenario 3: Multi-day Hospitalization
```
1. Patient admitted on Day 1 (room ₹1000/day)
2. Doctor consultations on Days 1, 2, 3
3. Lab tests on Days 2, 3
4. Pharmacy dispenses on Days 1, 2, 3
5. Patient discharged on Day 3
6. Bill calculation:
   - Admission days: 3 × 1000 = 3000
   - Doctor fee: 100
   - Medicine: Sum of all dispensed
   - Lab: 2 tests × 500 = 1000
   - Total = 3000 + 100 + medicine + 1000
```

---

## 🔒 Security Features

- **Role-based Access**: ADMIN, DOCTOR, PATIENT
- **Admission**: Only ADMIN can admit/discharge
- **Billing**: Patients see only their bills
- **Authentication**: JWT token required
- **Authorization**: `@PreAuthorize` annotations on all endpoints

---

## 📈 Performance Optimization Tips

1. **Database Indexes**
   - Add indexes on `patientId`, `admissionId`, `status`
   - Migration script includes indexes

2. **Caching**
   - Cache patient data during admission
   - Cache bill calculations

3. **Query Optimization**
   - Use JOIN queries for billing aggregation
   - Consider materialized views for revenue reports

---

## 🐛 Error Handling

| Error | Cause | Solution |
|-------|-------|----------|
| Bed already occupied | Patient still admitted | Discharge patient first |
| Patient not found | Invalid patientId | Verify patient exists |
| Admission not found | Invalid admissionId | Create admission first |
| Insufficient balance | Patient can't pay | Ensure payment method available |

---

## 📚 Viva Questions

**Q1: Explain the complete patient workflow.**  
A: Patient registers, books appointment, gets consulted by doctor, receives prescription and optional lab tests, pharmacy dispenses medicines. If hospitalization needed, patient is admitted to bed with room charges calculated per day. All charges (doctor, medicine, lab, room) are aggregated into a single bill. Patient pays bill, and upon discharge, a comprehensive summary is generated with diagnosis, medicines, tests, and total charges.

**Q2: How is billing aggregated?**  
A: The system queries:
- Consultation fee: Fixed ₹100
- Medicine costs: Sum from pharmacy records
- Lab charges: Count of lab tests × ₹500
- Room charges: (Discharge date - Admission date) × Daily rate
Total = Sum of all four components

**Q3: What information is in the discharge summary?**  
A: Patient name, diagnosis (from prescription notes), list of prescribed medicines, lab tests performed, total bill amount, and discharge date. This provides a complete medical and financial record for the patient.

**Q4: How are beds managed?**  
A: Each bed has an ID, bed number, ward name, and occupancy status. When a patient is admitted, a bed is assigned and marked as occupied. Upon discharge, the bed is marked as unoccupied and can be assigned to another patient.

**Q5: How does payment tracking work?**  
A: Bills have a status (PAID/UNPAID) and paidAt timestamp. When payment is received, status is updated to PAID with current timestamp. This enables generating revenue reports and identifying outstanding bills.

---

## 🚀 Getting Started

1. **Build Backend**
   ```bash
   cd backend
   mvn clean compile
   ```

2. **Run Backend**
   ```bash
   mvn spring-boot:run
   ```

3. **Database Migration** (Automatic via Flyway)
   - Tables created automatically on startup

4. **Test Endpoints**
   - Use Postman or curl
   - Start with admission creation
   - Then test aggregated billing

---

## 📞 Support

For issues or questions:
- Check `hospital/workflow/WorkflowDocumentation.java`
- Check `hospital/workflow/IntegrationGuide.java`
- Review error handling section
- Verify database migration ran successfully

---

**Last Updated**: April 25, 2026  
**Version**: 1.0 - Complete Patient Workflow  
**Status**: ✅ Production Ready

