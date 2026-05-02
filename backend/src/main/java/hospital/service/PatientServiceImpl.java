package hospital.service;

import hospital.dto.PatientRequest;
import hospital.entity.Appointment;
import hospital.entity.LabResult;
import hospital.entity.Patient;
import hospital.entity.Role;
import hospital.entity.User;
import hospital.repository.AppointmentRepository;
import hospital.repository.LabResultRepository;
import hospital.repository.PatientRepository;
import hospital.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppointmentRepository appointmentRepository;
    private final LabResultRepository labResultRepository;

    @Override
    public Patient createPatient(PatientRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with email already exists");
        }

        String rawPassword = request.getPassword() == null ? "password" : request.getPassword();

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.PATIENT)
                .build();

        user = userRepository.save(user);

        Patient.PatientBuilder pb = Patient.builder()
            .user(user)
            .phone(request.getPhone())
            .address(request.getAddress())
            .dob(parseDob(request.getDob()));
        if (request.getSex() != null) pb.sex(request.getSex());
        if (request.getAge() != null) pb.age(request.getAge());
        Patient patient = pb.build();

        return patientRepository.save(patient);
    }

    @Override
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Override
    public Patient getPatientById(Long id) {
        return patientRepository.findById(id).orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    @Override
    public Patient getCurrentPatient() {
        String email = currentUserEmail();
        return patientRepository.findByUserEmail(email).orElseThrow(() -> new RuntimeException("Patient profile not found"));
    }

    @Override
    public Patient updatePatient(Long id, PatientRequest request) {
        Patient patient = getPatientById(id);
        updatePatientInternal(patient, request);
        return patientRepository.save(patient);
    }

    @Override
    public Patient updateCurrentPatient(PatientRequest request) {
        Patient patient = getCurrentPatient();
        updatePatientInternal(patient, request);
        return patientRepository.save(patient);
    }

    @Override
    public void deletePatient(Long id) {
        Patient patient = getPatientById(id);
        User user = patient.getUser();
        patientRepository.delete(patient);
        userRepository.delete(user);
    }

    @Override
    public java.util.Map<String, Object> getPatientHistory(Long id) {
        Patient patient = getPatientById(id);
        Long userId = patient.getUser() != null ? patient.getUser().getId() : null;
        java.util.List<Appointment> appts = userId != null ? appointmentRepository.findByPatient(patient.getUser()) : java.util.List.of();
        java.util.List<LabResult> labs = userId != null ? labResultRepository.findByPatient_Id(userId) : java.util.List.of();

        java.util.List<java.util.Map<String, Object>> apptsOut = new java.util.ArrayList<>();
        for (Appointment a : appts) {
            apptsOut.add(java.util.Map.of(
                    "id", a.getId(),
                    "appointmentTime", a.getAppointmentTime() != null ? a.getAppointmentTime().toString() : null,
                    "doctor", a.getDoctor() != null ? a.getDoctor().getName() : null,
                    "status", a.getStatus() != null ? a.getStatus().name() : null
            ));
        }

        java.util.List<java.util.Map<String, Object>> labsOut = new java.util.ArrayList<>();
        for (LabResult l : labs) {
            labsOut.add(java.util.Map.of(
                    "id", l.getId(),
                    "testType", l.getTestType() != null ? l.getTestType().name() : null,
                    "createdAt", l.getCreatedAt() != null ? l.getCreatedAt().toString() : null,
                    "doctor", l.getDoctor() != null ? l.getDoctor().getName() : null,
                    "appointmentId", l.getAppointment() != null ? l.getAppointment().getId() : null
            ));
        }

        return java.util.Map.of("appointments", apptsOut, "labResults", labsOut);
    }

    private void updatePatientInternal(Patient patient, PatientRequest request) {
        User user = patient.getUser();

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);

        if (request.getPhone() != null) {
            patient.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            patient.setAddress(request.getAddress());
        }
        if (request.getDob() != null) {
            patient.setDob(parseDob(request.getDob()));
        }
        if (request.getSex() != null) patient.setSex(request.getSex());
        if (request.getAge() != null) patient.setAge(request.getAge());
    }

    private LocalDate parseDob(String dob) {
        if (dob == null || dob.isBlank()) {
            return null;
        }
        return LocalDate.parse(dob);
    }

    private String currentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new RuntimeException("Unauthorized");
        }
        return auth.getName();
    }
}
