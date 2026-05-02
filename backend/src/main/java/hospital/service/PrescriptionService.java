package hospital.service;

import hospital.dto.PrescriptionRequest;
import hospital.dto.PrescriptionMedicineRequest;
import hospital.entity.Prescription;
import hospital.entity.PrescriptionMedicine;

import java.util.List;

public interface PrescriptionService {
    Prescription createPrescription(PrescriptionRequest request);
    List<Prescription> getPrescriptionsForCurrentUser();
    PrescriptionMedicine addMedicineToPrescription(Long prescriptionId, PrescriptionMedicineRequest req);
    List<PrescriptionMedicine> getMedicinesForPrescription(Long prescriptionId);
}
