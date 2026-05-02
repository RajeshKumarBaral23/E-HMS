package hospital.workflow;

/**
 * COMPLETE REAL-LIFE PATIENT WORKFLOW DOCUMENTATION
 *
 * This document explains how all modules work together in the Hospital Management System
 * to implement a complete patient workflow from registration to discharge.
 *
 * ============================================================================
 * WORKFLOW STEPS:
 * ============================================================================
 *
 * 1. PATIENT REGISTRATION & LOGIN (Existing)
 *    - Patient registers via /api/auth/register
 *    - Patient logs in via /api/auth/login
 *    - JWT token issued for authenticated requests
 *
 * 2. PATIENT BOOKS APPOINTMENT (Existing)
 *    - Patient calls POST /api/appointments with:
 *      { doctorId, appointmentDate, reason }
 *    - Appointment status = SCHEDULED
 *    - Patient receives confirmation notification
 *
 * 3. DOCTOR PERFORMS CONSULTATION (Existing)
 *    - Doctor views appointment via GET /api/appointments
 *    - Doctor marks consultation as completed (optional endpoint)
 *
 * 4. DOCTOR CREATES PRESCRIPTION (Existing)
 *    - Doctor calls POST /api/prescriptions with:
 *      { appointmentId, medications, notes, medicines[] }
 *    - Prescription linked to appointment and patient
 *    - Patient notified about prescription availability
 *
 * 5. LAB TECHNICIAN UPLOADS LAB RESULTS (Existing with File Attachment)
 *    - Doctor may request lab tests in prescription notes
 *    - Lab technician calls POST /api/lab-results with:
 *      { patientId, appointmentId, testType, result }
 *    - Results linked to patient and appointment
 *
 * 6. PHARMACY PROCESSES MEDICINES (Existing)
 *    - Pharmacy calls POST /api/pharmacy-records with:
 *      { prescriptionId, medicineId, quantityDispensed }
 *    - Medicine stock updated
 *    - Pharmacy notified of prescription availability
 *
 * 7. ADMISSION (IF REQUIRED) [NEW]
 *    - Doctor determines patient needs hospitalization
 *    - Admin calls POST /api/admissions with:
 *      { patientId, bedId, admissionDate, roomChargePerDay }
 *    - Bed marked as occupied
 *    - Room charges calculated per day
 *
 * 8. BILLING - AGGREGATED CHARGES [NEW]
 *    - Admin calls POST /api/billing/generate-aggregated with:
 *      { patientId, admissionId }
 *    - System calculates:
 *      - consultationFee: Doctor consultation charge (default 100)
 *      - medicineCost: Sum of medicine prices from pharmacy records
 *      - labCharges: Number of lab tests × 500
 *      - roomCharges: Days admitted × roomChargePerDay
 *      - totalAmount: SUM of all charges
 *    - Billing status = UNPAID
 *
 * 9. PAYMENT [NEW/EXTENDED]
 *    - Patient calls PUT /api/billing/{billId}/pay
 *    - Billing status = PAID
 *    - Payment timestamp recorded
 *
 * 10. DISCHARGE WITH SUMMARY [NEW]
 *    - Doctor requests discharge
 *    - Admin calls PUT /api/admissions/{admissionId}/discharge
 *    - Discharge date recorded
 *    - Bed marked as unoccupied
 *    - Patient calls GET /api/discharge/summary with:
 *      { patientId, admissionId }
 *    - Summary includes:
 *      - Patient name and diagnosis (from prescription notes)
 *      - Medicines prescribed (aggregated from prescriptions)
 *      - Lab tests performed (list of test types)
 *      - Total billing amount
 *      - Discharge date
 *
 * ============================================================================
 * DATABASE RELATIONSHIPS:
 * ============================================================================
 *
 * User (Patient) → Appointment ← User (Doctor)
 *     ↓                              ↓
 *  Patient                     Prescription
 *     ↓                              ↓
 *   Admission ← Bed           PrescriptionMedicine ← Medicine
 *     ↓                              ↓
 *   Billing ← PharmacyRecord        Billing
 *     ↓
 *   LabResult
 *
 * ============================================================================
 * API ENDPOINTS SUMMARY:
 * ============================================================================
 *
 * ADMISSION ENDPOINTS:
 *   POST   /api/admissions                    - Admit patient
 *   PUT    /api/admissions/{id}/discharge     - Discharge patient
 *   GET    /api/admissions/patient/{patientId} - Get admissions for patient
 *
 * BILLING ENDPOINTS:
 *   POST   /api/billing/generate/{appointmentId} - Generate simple bill
 *   POST   /api/billing/generate-aggregated - Generate aggregated bill (WORKFLOW)
 *   GET    /api/billing                      - List bills for current user
 *   GET    /api/billing/patient/{patientId}  - List bills for patient
 *   PUT    /api/billing/{id}/pay             - Mark bill as paid
 *
 * DISCHARGE ENDPOINTS:
 *   GET    /api/discharge/summary            - Get discharge summary
 *
 * ============================================================================
 * SAMPLE WORKFLOW API CALLS:
 * ============================================================================
 *
 * 1. Register & Login
 *    POST /api/auth/register → JWT token
 *
 * 2. Book Appointment
 *    POST /api/appointments { doctorId: 1, appointmentDate: "2026-04-26", reason: "Checkup" }
 *    Response: appointmentId = 10
 *
 * 3. Doctor adds Prescription
 *    POST /api/prescriptions {
 *      appointmentId: 10,
 *      medications: "Paracetamol 500mg",
 *      notes: "Patient has fever, blood test recommended",
 *      medicines: [{ medicineId: 1, quantity: 10 }]
 *    }
 *
 * 4. Lab Technician adds Lab Results
 *    POST /api/lab-results {
 *      patientId: 5,
 *      appointmentId: 10,
 *      testType: "BLOOD",
 *      result: "All normal"
 *    }
 *
 * 5. Pharmacy dispenses medicines
 *    POST /api/pharmacy-records {
 *      prescriptionId: 3,
 *      medicineId: 1,
 *      quantityDispensed: 10
 *    }
 *
 * 6. Admin admits patient (if required)
 *    POST /api/admissions {
 *      patientId: 5,
 *      bedId: 12,
 *      admissionDate: "2026-04-26",
 *      roomChargePerDay: 1000.0
 *    }
 *    Response: admissionId = 7
 *
 * 7. Admin generates aggregated bill
 *    POST /api/billing/generate-aggregated?patientId=5&admissionId=7
 *    Response: {
 *      consultationFee: 100.0,
 *      medicineCost: 2000.0,
 *      labCharges: 500.0,
 *      roomCharges: 3000.0 (3 days × 1000),
 *      totalAmount: 5600.0,
 *      status: "UNPAID"
 *    }
 *
 * 8. Patient pays bill
 *    PUT /api/billing/{billId}/pay
 *    Response: status = "PAID", paidAt = "2026-04-29T10:30:00"
 *
 * 9. Admin discharges patient
 *    PUT /api/admissions/7/discharge?dischargeDate=2026-04-29
 *
 * 10. Patient views discharge summary
 *    GET /api/discharge/summary?patientId=5&admissionId=7
 *    Response: {
 *      patientName: "John Doe",
 *      diagnosis: "Fever",
 *      medicines: ["Paracetamol 500mg"],
 *      labTests: ["BLOOD"],
 *      totalBill: 5600.0,
 *      dischargeDate: "2026-04-29"
 *    }
 *
 * ============================================================================
 * KEY FEATURES IMPLEMENTED:
 * ============================================================================
 *
 * ✓ Admission Management
 *   - Bed assignment and occupancy tracking
 *   - Room charges calculated per day
 *   - Admission and discharge date tracking
 *
 * ✓ Aggregated Billing
 *   - Consultation fees (doctor)
 *   - Medicine costs (pharmacy)
 *   - Lab test charges (lab)
 *   - Room charges (admission)
 *   - Dynamic total calculation
 *   - Payment tracking with timestamp
 *
 * ✓ Discharge Summary
 *   - Patient diagnosis (from prescription notes)
 *   - Prescribed medicines list
 *   - Lab tests performed
 *   - Final billing amount
 *   - Discharge date
 *
 * ✓ Complete Workflow
 *   - All modules connected
 *   - Proper relationships maintained
 *   - Exception handling in place
 *   - Authorization checks via @PreAuthorize
 *
 * ============================================================================
 * VIVA EXPLANATION:
 * ============================================================================
 *
 * Q: What is the complete patient workflow?
 * A: Patient registers, books appointment, gets consulted by doctor,
 *    receives prescription, undergoes lab tests, pharmacy dispenses medicines,
 *    if required gets admitted (bed assignment), aggregated billing calculates
 *    all charges (doctor, medicine, lab, room), patient pays, and finally
 *    gets discharged with summary containing diagnosis, medicines, tests, and bill.
 *
 * Q: How is billing aggregated?
 * A: The system queries pharmacy records for medicine costs, lab results for
 *    lab charges, and admission data for room charges. Default consultation fee
 *    is added, and all are summed to calculate totalAmount. This is stored in
 *    the Billing entity and can be marked as paid with timestamp.
 *
 * Q: What is in discharge summary?
 * A: Discharge summary aggregates all patient information: name, diagnosis (from
 *    prescription notes), prescribed medicines (from prescriptions), lab tests
 *    performed, and the total bill amount. This provides a complete record for
 *    the patient at discharge.
 *
 * ============================================================================
 */

public class WorkflowDocumentation {
    // This is a documentation file
}

