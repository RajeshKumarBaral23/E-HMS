package hospital.service;

import hospital.dto.LabResultRequest;
import hospital.entity.Appointment;
import hospital.entity.LabResult;
import hospital.entity.LabTestType;
import hospital.entity.User;
import hospital.repository.AppointmentRepository;
import hospital.repository.LabResultRepository;
import hospital.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LabResultServiceImpl implements LabResultService {

    private final LabResultRepository labResultRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    public LabResult create(LabResultRequest request) {
        User patient = userRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient user not found"));

        User doctor = null;
        if (request.getDoctorId() != null) {
            doctor = userRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new RuntimeException("Doctor user not found"));
        }

        Appointment appointment = null;
        if (request.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
        }

        LabTestType type;
        try {
            type = LabTestType.valueOf(request.getTestType());
        } catch (Exception ex) {
            type = LabTestType.OTHER;
        }

        LabResult lr = LabResult.builder()
                .patient(patient)
                .doctor(doctor)
                .appointment(appointment)
                .testType(type)
                .result(request.getResult())
                .createdAt(LocalDateTime.now())
                .build();

        return labResultRepository.save(lr);
    }

    @Override
    public List<LabResult> getByPatientId(Long patientId) {
        return labResultRepository.findByPatient_Id(patientId);
    }

    @Override
    public List<LabResult> getAll() {
        return labResultRepository.findAll();
    }

    @Override
    public LabResult getById(Long id) {
        return labResultRepository.findById(id).orElseThrow(() -> new RuntimeException("LabResult not found"));
    }
}
