package hospital.service;

import hospital.dto.MedicalRecordRequest;
import hospital.entity.MedicalRecord;
import hospital.entity.User;
import hospital.repository.MedicalRecordRepository;
import hospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import hospital.repository.AppointmentRepository;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    public MedicalRecord create(MedicalRecordRequest req) {
        User patient = userRepository.findById(req.getPatientId()).orElseThrow(() -> new RuntimeException("Patient not found"));
        User doctor = userRepository.findById(req.getDoctorId()).orElseThrow(() -> new RuntimeException("Doctor not found"));

        LocalDateTime visit;
        try {
            visit = LocalDateTime.parse(req.getVisitDate());
        } catch (DateTimeParseException ex) {
            throw new RuntimeException("Invalid visitDate format, expected ISO local date-time");
        }

        hospital.entity.Appointment appt = null;
        if (req.getAppointmentId() != null) {
            appt = appointmentRepository.findById(req.getAppointmentId()).orElse(null);
        }

        MedicalRecord mr = MedicalRecord.builder()
                .patient(patient)
                .doctor(doctor)
                .appointment(appt)
                .diagnosis(req.getDiagnosis())
                .treatment(req.getTreatment())
                .notes(req.getNotes())
                .visitDate(visit)
                .createdAt(LocalDateTime.now())
                .build();

        return medicalRecordRepository.save(mr);
    }

    @Override
    public List<MedicalRecord> listForPatient(Long patientId) {
        User patient = userRepository.findById(patientId).orElseThrow(() -> new RuntimeException("Patient not found"));
        return medicalRecordRepository.findByPatient(patient);
    }

    @Override
    public List<MedicalRecord> listForDoctor(Long doctorId) {
        User doctor = userRepository.findById(doctorId).orElseThrow(() -> new RuntimeException("Doctor not found"));
        return medicalRecordRepository.findByDoctor(doctor);
    }

    @Override
    public MedicalRecord getById(Long id) {
        return medicalRecordRepository.findById(id).orElseThrow(() -> new RuntimeException("Medical record not found"));
    }

    @Override
    public List<MedicalRecord> listAll() {
        return medicalRecordRepository.findAll();
    }
}
