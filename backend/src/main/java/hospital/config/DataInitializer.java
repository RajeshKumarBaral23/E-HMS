package hospital.config;

import hospital.entity.Doctor;
import hospital.entity.Patient;
import hospital.entity.Role;
import hospital.entity.User;
import hospital.repository.DoctorRepository;
import hospital.repository.PatientRepository;
import hospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        User admin = upsertUser("Admin User", "admin@ehealth.com", "admin123", Role.ADMIN);

        User doctor = upsertUser("Dr. John Smith", "doctor@ehealth.com", "doctor123", Role.DOCTOR);
        upsertDoctorProfile(doctor, "Cardiology", "+1-555-0100", "Experienced cardiologist with 10+ years of practice");

        User patient = upsertUser("Jane Doe", "patient@ehealth.com", "patient123", Role.PATIENT);
        upsertPatientProfile(patient, "+1-555-0200", "123 Main St, Springfield");

        System.out.println("\n📋 E-Healthcare System initialized with test accounts");
        System.out.println("   Admin:   admin@ehealth.com / admin123");
        System.out.println("   Doctor:  doctor@ehealth.com / doctor123");
        System.out.println("   Patient: patient@ehealth.com / patient123\n");
    }

    private User upsertUser(String name, String email, String rawPassword, Role role) {
        User user = userRepository.findByEmail(email).orElseGet(() -> User.builder().email(email).build());
        boolean isNew = user.getId() == null;
        boolean changed = false;

        if (!name.equals(user.getName())) {
            user.setName(name);
            changed = true;
        }

        if (user.getRole() != role) {
            user.setRole(role);
            changed = true;
        }

        if (user.getPassword() == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            changed = true;
        }

        if (isNew || changed) {
            user = userRepository.save(user);
            System.out.println((isNew ? "✓ Created " : "✓ Updated ") + "default " + role.name().toLowerCase() + " user: " + email + " / " + rawPassword);
        }

        return user;
    }

    private void upsertDoctorProfile(User doctorUser, String specialization, String phone, String bio) {
        Doctor doctor = doctorRepository.findByUserEmail(doctorUser.getEmail())
                .orElseGet(() -> Doctor.builder().user(doctorUser).build());

        doctor.setSpecialization(specialization);
        doctor.setPhone(phone);
        doctor.setBio(bio);
        doctorRepository.save(doctor);
    }

    private void upsertPatientProfile(User patientUser, String phone, String address) {
        Patient patient = patientRepository.findByUserEmail(patientUser.getEmail())
                .orElseGet(() -> Patient.builder().user(patientUser).build());

        patient.setPhone(phone);
        patient.setAddress(address);
        patientRepository.save(patient);
    }
}
