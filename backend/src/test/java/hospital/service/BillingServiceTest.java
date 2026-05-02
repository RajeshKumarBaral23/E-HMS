package hospital.service;

import hospital.entity.Appointment;
import hospital.entity.Billing;
import hospital.entity.Medicine;
import hospital.entity.PharmacyRecord;
import hospital.repository.AppointmentRepository;
import hospital.repository.BillingRepository;
import hospital.repository.MedicineRepository;
import hospital.repository.PharmacyRecordRepository;
import hospital.repository.PrescriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillingServiceTest {

    private BillingRepository billingRepository;
    private AppointmentRepository appointmentRepository;
    private PharmacyRecordRepository pharmacyRecordRepository;
    private MedicineRepository medicineRepository;
    private PrescriptionRepository prescriptionRepository;
    private BillingServiceImpl service;

    @BeforeEach
    void setUp() {
        billingRepository = mock(BillingRepository.class);
        appointmentRepository = mock(AppointmentRepository.class);
        pharmacyRecordRepository = mock(PharmacyRecordRepository.class);
        medicineRepository = mock(MedicineRepository.class);
        prescriptionRepository = mock(PrescriptionRepository.class);
        service = new BillingServiceImpl(billingRepository, appointmentRepository, pharmacyRecordRepository, medicineRepository, prescriptionRepository);
    }

    @Test
    void generateBillCreatesBilling() {
        Appointment appt = new Appointment(); appt.setId(1L);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appt));

        // mock prescription for this appointment
        hospital.entity.Prescription p = mock(hospital.entity.Prescription.class);
        when(p.getId()).thenReturn(2L);
        when(prescriptionRepository.findByAppointmentId(1L)).thenReturn(Arrays.asList(p));

        PharmacyRecord r = PharmacyRecord.builder().medicineId(2L).quantityDispensed(1).build();
        when(pharmacyRecordRepository.findByPrescriptionId(2L)).thenReturn(Arrays.asList(r));

        when(medicineRepository.findById(2L)).thenReturn(Optional.of(new Medicine(2L, "M", 10, 5.0, "")));
        when(billingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Billing b = service.generateBill(1L);
        assertEquals(5.0 + 100.0, b.getTotalAmount());
        assertEquals("UNPAID", b.getStatus());
    }
}
