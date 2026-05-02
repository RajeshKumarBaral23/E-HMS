package hospital.service;

import hospital.entity.Appointment;
import hospital.entity.Billing;
import hospital.entity.Medicine;
import hospital.entity.PharmacyRecord;
import hospital.entity.Role;
import hospital.entity.User;
import hospital.repository.AppointmentRepository;
import hospital.repository.BillingRepository;
import hospital.repository.MedicineRepository;
import hospital.repository.PharmacyRecordRepository;
import hospital.repository.PrescriptionRepository;
import hospital.repository.UserRepository;
import hospital.repository.AdmissionRepository;
import hospital.repository.LabResultRepository;
import hospital.repository.PatientRepository;
import hospital.entity.Patient;
import hospital.entity.Admission;
import hospital.entity.LabResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillingServiceImpl implements BillingService {

    private final BillingRepository billingRepository;
    private final AppointmentRepository appointmentRepository;
    private final PharmacyRecordRepository pharmacyRecordRepository;
    private final MedicineRepository medicineRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;

    @Autowired
    public BillingServiceImpl(BillingRepository billingRepository,
                              AppointmentRepository appointmentRepository,
                              PharmacyRecordRepository pharmacyRecordRepository,
                              MedicineRepository medicineRepository,
                              PrescriptionRepository prescriptionRepository,
                              UserRepository userRepository) {
        this.billingRepository = billingRepository;
        this.appointmentRepository = appointmentRepository;
        this.pharmacyRecordRepository = pharmacyRecordRepository;
        this.medicineRepository = medicineRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.userRepository = userRepository;
    }

    // Backwards-compatible constructor (in case tests or other code use old signature)
    public BillingServiceImpl(BillingRepository billingRepository,
                              AppointmentRepository appointmentRepository,
                              PharmacyRecordRepository pharmacyRecordRepository,
                              MedicineRepository medicineRepository,
                              PrescriptionRepository prescriptionRepository) {
        this(billingRepository, appointmentRepository, pharmacyRecordRepository, medicineRepository, prescriptionRepository, null);
    }

    public BillingServiceImpl(BillingRepository billingRepository,
                              AppointmentRepository appointmentRepository,
                              PharmacyRecordRepository pharmacyRecordRepository,
                              MedicineRepository medicineRepository) {
        this(billingRepository, appointmentRepository, pharmacyRecordRepository, medicineRepository, null, null);
    }

    @Override
    public Billing generateBill(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));

        double consultationFee = DEFAULT_CONSULTATION_FEE;

        // Sum all pharmacy records tied to this appointment
        double medicineCost = 0.0;
        List<PharmacyRecord> records = pharmacyRecordRepository.findByAppointmentId(appointmentId);
        for (PharmacyRecord r : records) {
            Medicine med = medicineRepository.findById(r.getMedicineId()).orElse(null);
            if (med != null) {
                medicineCost += med.getPrice() * r.getQuantityDispensed();
            }
        }

        double total = consultationFee + medicineCost;

        Billing bill = Billing.builder()
                .appointmentId(appointmentId)
                .consultationFee(consultationFee)
                .medicineCost(medicineCost)
                .labCharges(0.0)
                .roomCharges(0.0)
                .totalAmount(total)
                .status("UNPAID")
                .paymentStatus(hospital.entity.PaymentStatus.UNPAID)
                .createdAt(LocalDateTime.now())
                .build();

        return billingRepository.save(bill);
    }

    private static final double DEFAULT_CONSULTATION_FEE = 100.0;
    @Autowired private AdmissionRepository admissionRepository;
    @Autowired private LabResultRepository labResultRepository;
    @Autowired private PatientRepository patientRepository;

    @Override
    public Billing generateAggregatedBill(Long patientId, Long admissionId) {
        Patient patient = patientRepository.findById(patientId).orElseThrow(() -> new RuntimeException("Patient not found"));
        Admission admission = admissionRepository.findById(admissionId).orElseThrow(() -> new RuntimeException("Admission not found"));

        // Calculate consultation fee (from prescriptions)
        double consultationFee = DEFAULT_CONSULTATION_FEE;

        // Calculate medicine cost
        double medicineCost = 0.0;
        List<hospital.entity.Prescription> prescriptions = prescriptionRepository.findByPatient(patient.getUser());
        for (hospital.entity.Prescription prescription : prescriptions) {
            List<PharmacyRecord> records = pharmacyRecordRepository.findByPrescriptionId(prescription.getId());
            for (PharmacyRecord r : records) {
                Medicine med = medicineRepository.findById(r.getMedicineId()).orElse(null);
                if (med != null) {
                    medicineCost += med.getPrice() * r.getQuantityDispensed();
                }
            }
        }

        // Calculate lab charges
        double labCharges = 0.0;
        List<LabResult> labResults = labResultRepository.findByPatient_Id(patientId);
        for (LabResult lab : labResults) {
            labCharges += 500.0; // Fixed lab test charge
        }

        // Calculate room charges
        double roomCharges = 0.0;
        if (admission.getAdmissionDate() != null && admission.getDischargeDate() != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(admission.getAdmissionDate(), admission.getDischargeDate());
            roomCharges = days * admission.getRoomChargePerDay();
        }

        double total = consultationFee + medicineCost + labCharges + roomCharges;

        Billing bill = Billing.builder()
            .patientId(patientId)
            .admissionId(admissionId)
            .consultationFee(consultationFee)
            .medicineCost(medicineCost)
            .labCharges(labCharges)
            .roomCharges(roomCharges)
            .totalAmount(total)
            .status("UNPAID")
            .paymentStatus(hospital.entity.PaymentStatus.UNPAID)
            .createdAt(LocalDateTime.now())
            .build();

        return billingRepository.save(bill);
    }

    @Override
    public Billing markAsPaid(Long billId) {
        Billing b = billingRepository.findById(billId).orElseThrow(() -> new RuntimeException("Bill not found"));
        b.setStatus("PAID");
        b.setPaidAt(LocalDateTime.now());
        b.setPaymentStatus(hospital.entity.PaymentStatus.PAID);
        return billingRepository.save(b);
    }

    @Override
    public java.util.List<Billing> getBillsByPatient(Long patientId) {
        return billingRepository.findByPatientId(patientId);
    }

    // ...existing code...

    @Override
    public List<Billing> listBillsForCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? auth.getName() : null;
        if (email == null || email.isBlank()) throw new RuntimeException("Unauthorized");

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            // Keep predictable ordering for the frontend “latestBill” widget
            List<Billing> all = billingRepository.findAll();
            all.sort((a, b) -> {
                if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });
            return all;
        }

        List<Appointment> appts;
        if (user.getRole() == Role.DOCTOR) {
            appts = appointmentRepository.findByDoctor(user);
        } else if (user.getRole() == Role.PATIENT) {
            appts = appointmentRepository.findByPatient(user);
        } else {
            throw new RuntimeException("Unsupported role");
        }

        List<Long> ids = appts.stream().map(Appointment::getId).collect(Collectors.toList());
        if (ids.isEmpty()) return java.util.Collections.emptyList();
        return billingRepository.findByAppointmentIdInOrderByCreatedAtDesc(ids);
    }
}
