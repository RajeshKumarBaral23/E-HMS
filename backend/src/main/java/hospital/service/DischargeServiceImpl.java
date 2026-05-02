package hospital.service;

import hospital.dto.DischargeSummaryDTO;
import hospital.entity.*;
import hospital.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DischargeServiceImpl implements DischargeService {
    @Autowired private PatientRepository patientRepo;
    @Autowired private PrescriptionRepository prescriptionRepo;
    @Autowired private LabResultRepository labResultRepo;
    @Autowired private BillingRepository billingRepo;
    @Autowired private AdmissionRepository admissionRepo;

    @Override
    public DischargeSummaryDTO generateSummary(Long patientId, Long admissionId) {
        Patient patient = patientRepo.findById(patientId).orElseThrow();
        List<Prescription> prescriptions = prescriptionRepo.findByPatientId(patientId);
        List<LabResult> labResults = labResultRepo.findByPatient_Id(patientId);

        // Aggregate medicine names from prescriptions
        List<String> medicineNames = prescriptions.stream()
            .filter(p -> p.getPrescriptionMedicines() != null)
            .flatMap(p -> p.getPrescriptionMedicines().stream())
            .map(pm -> pm.getMedicine() != null ? pm.getMedicine().getName() : "Unknown")
            .collect(Collectors.toList());

        // Aggregate lab test names
        List<String> labTestNames = labResults.stream()
            .map(lr -> lr.getTestType() != null ? lr.getTestType().name() : "Unknown")
            .collect(Collectors.toList());

        // Get billing for this admission
        List<Billing> bills = billingRepo.findByPatientId(patientId);
        Double totalBill = bills.stream()
            .filter(b -> admissionId.equals(b.getAdmissionId()))
            .map(Billing::getTotalAmount)
            .findFirst()
            .orElse(0.0);

        // Get admission details for discharge date
        Admission admission = admissionRepo.findById(admissionId).orElse(null);

        DischargeSummaryDTO dto = new DischargeSummaryDTO();
        dto.setPatientName(patient.getUser().getName());
        dto.setDiagnosis(prescriptions.isEmpty() ? "" : prescriptions.get(0).getNotes());
        dto.setMedicines(medicineNames);
        dto.setLabTests(labTestNames);
        dto.setTotalBill(totalBill);
        dto.setDischargeDate(admission != null && admission.getDischargeDate() != null ? admission.getDischargeDate().toString() : "");
        return dto;
    }
}


