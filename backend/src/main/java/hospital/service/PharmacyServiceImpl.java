package hospital.service;

import hospital.entity.Appointment;
import hospital.entity.Medicine;
import hospital.entity.PharmacyRecord;
import hospital.entity.User;
import hospital.repository.AppointmentRepository;
import hospital.repository.MedicineRepository;
import hospital.repository.PharmacyRecordRepository;
import hospital.repository.PrescriptionRepository;
import hospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PharmacyServiceImpl implements PharmacyService {

    private final MedicineRepository medicineRepository;
    private final PharmacyRecordRepository pharmacyRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    public PharmacyRecord dispenseMedicine(Long prescriptionId, Long medicineId, int quantity) {
        // verify prescription exists
        prescriptionRepository.findById(prescriptionId).orElseThrow(() -> new RuntimeException("Prescription not found"));

        Medicine med = medicineRepository.findById(medicineId).orElseThrow(() -> new RuntimeException("Medicine not found"));
        if (med.getQuantity() < quantity) throw new RuntimeException("Insufficient stock");
        med.setQuantity(med.getQuantity() - quantity);
        medicineRepository.save(med);

        PharmacyRecord rec = PharmacyRecord.builder()
                .prescriptionId(prescriptionId)
                .medicineId(medicineId)
                .quantityDispensed(quantity)
                .dispensedAt(LocalDateTime.now())
                .build();

        return pharmacyRecordRepository.save(rec);
    }

    @Override
    public PharmacyRecord purchaseMedicine(Long medicineId, int quantity, String patientEmail, Long appointmentId) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("Patient user not found"));

        if (appointmentId != null) {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            if (!appointment.getPatient().getId().equals(patient.getId())) {
                throw new RuntimeException("Purchase appointment does not belong to current patient");
            }
        }

        Medicine med = medicineRepository.findById(medicineId).orElseThrow(() -> new RuntimeException("Medicine not found"));
        if (med.getQuantity() < quantity) throw new RuntimeException("Insufficient stock");
        med.setQuantity(med.getQuantity() - quantity);
        medicineRepository.save(med);

        PharmacyRecord rec = PharmacyRecord.builder()
                .patientId(patient.getId())
                .appointmentId(appointmentId)
                .medicineId(medicineId)
                .quantityDispensed(quantity)
                .dispensedAt(LocalDateTime.now())
                .build();

        return pharmacyRecordRepository.save(rec);
    }
}
