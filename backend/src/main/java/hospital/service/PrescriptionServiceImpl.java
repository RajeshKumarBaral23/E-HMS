package hospital.service;

import hospital.dto.PrescriptionRequest;
import hospital.dto.PrescriptionMedicineRequest;
import hospital.entity.Appointment;
import hospital.entity.Medicine;
import hospital.entity.Prescription;
import hospital.entity.PrescriptionMedicine;
import hospital.entity.Role;
import hospital.entity.User;
import hospital.repository.AppointmentRepository;
import hospital.repository.MedicineRepository;
import hospital.repository.PrescriptionMedicineRepository;
import hospital.repository.PrescriptionRepository;
import hospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final MedicineRepository medicineRepository;
    private final PrescriptionMedicineRepository prescriptionMedicineRepository;

    @Override
    public Prescription createPrescription(PrescriptionRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String doctorEmail = auth.getName();
        User doctor = userRepository.findByEmail(doctorEmail).orElseThrow(() -> new RuntimeException("Doctor not found"));
        if (doctor.getRole() != Role.DOCTOR) {
            throw new RuntimeException("Only doctors can create prescriptions");
        }

        Appointment appt = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        Prescription p = Prescription.builder()
                .appointment(appt)
                .doctor(doctor)
                .patient(appt.getPatient())
                .medications(request.getMedications())
                .notes(request.getNotes())
                .createdAt(LocalDateTime.now())
                .build();

        return prescriptionRepository.save(p);
    }

    @Override
    public List<Prescription> getPrescriptionsForCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            return prescriptionRepository.findAll();
        } else if (user.getRole() == Role.DOCTOR) {
            return prescriptionRepository.findByDoctor(user);
        } else {
            return prescriptionRepository.findByPatient(user);
        }
    }

    @Override
    public PrescriptionMedicine addMedicineToPrescription(Long prescriptionId, PrescriptionMedicineRequest req) {
        Prescription p = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));
        Medicine med = medicineRepository.findById(req.getMedicineId())
                .orElseThrow(() -> new RuntimeException("Medicine not found"));
        PrescriptionMedicine pm = PrescriptionMedicine.builder()
                .prescription(p)
                .medicine(med)
                .dosage(req.getDosage())
                .durationDays(req.getDurationDays())
                .instructions(req.getInstructions())
                .build();
        return prescriptionMedicineRepository.save(pm);
    }

    @Override
    public List<PrescriptionMedicine> getMedicinesForPrescription(Long prescriptionId) {
        return prescriptionMedicineRepository.findByPrescriptionId(prescriptionId);
    }
}
