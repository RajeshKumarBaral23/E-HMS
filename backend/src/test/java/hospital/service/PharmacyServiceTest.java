package hospital.service;

import hospital.entity.Medicine;
import hospital.entity.PharmacyRecord;
import hospital.entity.User;
import hospital.repository.AppointmentRepository;
import hospital.repository.MedicineRepository;
import hospital.repository.PharmacyRecordRepository;
import hospital.repository.PrescriptionRepository;
import hospital.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PharmacyServiceTest {

    private MedicineRepository medicineRepository;
    private PharmacyRecordRepository pharmacyRecordRepository;
    private PrescriptionRepository prescriptionRepository;
    private UserRepository userRepository;
    private AppointmentRepository appointmentRepository;
    private PharmacyServiceImpl service;

    @BeforeEach
    void setUp() {
        medicineRepository = mock(MedicineRepository.class);
        pharmacyRecordRepository = mock(PharmacyRecordRepository.class);
        prescriptionRepository = mock(PrescriptionRepository.class);
        userRepository = mock(UserRepository.class);
        appointmentRepository = mock(AppointmentRepository.class);
        service = new PharmacyServiceImpl(medicineRepository, pharmacyRecordRepository, prescriptionRepository, userRepository, appointmentRepository);
    }

    @Test
    void dispenseMedicineSuccess() {
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(mock(hospital.entity.Prescription.class)));
        Medicine med = new Medicine(1L, "TestMed", 10, 5.0, "desc");
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(med));
        when(medicineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(pharmacyRecordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PharmacyRecord r = service.dispenseMedicine(1L, 1L, 2);
        assertEquals(2, r.getQuantityDispensed());
        assertEquals(8, medicineRepository.findById(1L).get().getQuantity());
    }

    @Test
    void purchaseMedicineSuccess() {
        User patient = new User(2L, "Rajesh", "rajesh@example.com", "pass", null);
        when(userRepository.findByEmail("rajesh@example.com")).thenReturn(Optional.of(patient));
        Medicine med = new Medicine(1L, "TestMed", 10, 5.0, "desc");
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(med));
        when(medicineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(pharmacyRecordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PharmacyRecord r = service.purchaseMedicine(1L, 3, "rajesh@example.com", null);
        assertEquals(3, r.getQuantityDispensed());
        assertEquals(7, medicineRepository.findById(1L).get().getQuantity());
        assertEquals(patient.getId(), r.getPatientId());
    }
}
