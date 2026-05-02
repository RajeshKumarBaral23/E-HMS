package hospital.service;

import hospital.entity.PharmacyRecord;

public interface PharmacyService {
    PharmacyRecord dispenseMedicine(Long prescriptionId, Long medicineId, int quantity);
    PharmacyRecord purchaseMedicine(Long medicineId, int quantity, String patientEmail, Long appointmentId);
}
