package hospital.service;

import hospital.dto.DoctorRequest;
import hospital.entity.Doctor;
import hospital.entity.Role;
import hospital.entity.User;
import hospital.repository.DoctorRepository;
import hospital.repository.UserRepository;
import hospital.repository.DepartmentRepository;
import hospital.repository.AppointmentRepository;
import hospital.repository.PrescriptionRepository;
import hospital.repository.MedicalRecordRepository;
import hospital.repository.LabResultRepository;
import hospital.repository.AvailabilitySlotRepository;
import hospital.repository.FileAttachmentRepository;
import hospital.service.AvailabilitySlotService;
import hospital.entity.Department;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;
    private final AvailabilitySlotService availabilitySlotService;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final LabResultRepository labResultRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final FileAttachmentRepository fileAttachmentRepository;

    @Override
    public Doctor createDoctor(DoctorRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with email already exists");
        }

        String rawPassword = request.getPassword() == null ? "password" : request.getPassword();

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.DOCTOR)
                .build();

        user = userRepository.save(user);

        Department dept = null;
        if (request.getDepartmentId() != null) {
            dept = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));
        }

        Doctor doctor = Doctor.builder()
            .user(user)
            .department(dept)
            .specialization(request.getSpecialization())
            .phone(request.getPhone())
            .bio(request.getBio())
            .build();

        doctor = doctorRepository.save(doctor);

        if (request.getAvailabilityStartDateTime() != null && request.getAvailabilityEndDateTime() != null) {
            LocalDateTime start;
            LocalDateTime end;
            try {
                start = LocalDateTime.parse(request.getAvailabilityStartDateTime());
                end = LocalDateTime.parse(request.getAvailabilityEndDateTime());
            } catch (Exception ex) {
                throw new RuntimeException("Invalid availability datetime format. Use ISO local datetime, e.g. 2026-04-24T09:00");
            }
            if (end.isBefore(start) || end.equals(start)) {
                throw new RuntimeException("Availability end time must be after start time");
            }
            availabilitySlotService.createSlot(user.getId(), start, end);
        }

        return doctor;
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Override
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    @Override
    public Doctor updateDoctor(Long id, DoctorRequest request) {
        Doctor doc = doctorRepository.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));

        User user = doc.getUser();
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

        if (request.getSpecialization() != null) doc.setSpecialization(request.getSpecialization());
        if (request.getPhone() != null) doc.setPhone(request.getPhone());
        if (request.getBio() != null) doc.setBio(request.getBio());
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));
            doc.setDepartment(dept);
        }

        return doctorRepository.save(doc);
    }

    @Override
    public void deleteDoctor(Long id) {
        Doctor doc = doctorRepository.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
        User user = doc.getUser();

        // Remove availability slots for this doctor (best-effort)
        try {
            var slots = availabilitySlotRepository.findByDoctor(user);
            if (slots != null && !slots.isEmpty()) {
                availabilitySlotRepository.deleteAll(slots);
            }
        } catch (Exception ignored) {
        }

        // Delete appointments where this user is the doctor (best-effort)
        try {
            var appts = appointmentRepository.findByDoctor(user);
            if (appts != null && !appts.isEmpty()) {
                appointmentRepository.deleteAll(appts);
            }
        } catch (Exception ignored) {
        }

        // Delete prescriptions authored by this doctor (best-effort)
        try {
            var pres = prescriptionRepository.findByDoctor(user);
            if (pres != null && !pres.isEmpty()) {
                prescriptionRepository.deleteAll(pres);
            }
        } catch (Exception ignored) {
        }

        // Delete medical records where this user is the doctor (best-effort)
        try {
            var mrs = medicalRecordRepository.findByDoctor(user);
            if (mrs != null && !mrs.isEmpty()) {
                medicalRecordRepository.deleteAll(mrs);
            }
        } catch (Exception ignored) {
        }

        // Delete lab results authored by this doctor (best-effort)
        try {
            var labs = labResultRepository.findByDoctor_Id(user.getId());
            if (labs != null && !labs.isEmpty()) {
                labResultRepository.deleteAll(labs);
            }
        } catch (Exception ignored) {
        }

        // Delete file attachments related to this doctor (best-effort)
        try {
            var files = fileAttachmentRepository.findByRelatedTypeAndRelatedId("doctor", doc.getId());
            if (files != null && !files.isEmpty()) {
                fileAttachmentRepository.deleteAll(files);
            }
        } catch (Exception ignored) {
        }

        // Finally delete doctor record and associated user
        doctorRepository.delete(doc);
        userRepository.delete(user);
    }
}
