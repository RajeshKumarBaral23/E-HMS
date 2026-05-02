package hospital.service;

import hospital.entity.Medicine;
import hospital.repository.MedicineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MedicineServiceTest {

    private MedicineRepository medicineRepository;
    private MedicineServiceImpl service;

    @BeforeEach
    void setUp() {
        medicineRepository = mock(MedicineRepository.class);
        service = new MedicineServiceImpl(medicineRepository);
    }

    @Test
    void addMedicine() {
        Medicine m = new Medicine(null, "Paracetamol", 100, 1.5, "Painkiller");
        when(medicineRepository.save(any())).thenAnswer(inv -> { Medicine arg = inv.getArgument(0); arg.setId(1L); return arg; });
        Medicine res = service.addMedicine(m);
        assertNotNull(res.getId());
        assertEquals("Paracetamol", res.getName());
    }

    @Test
    void updateStock() {
        Medicine m = new Medicine(2L, "Ibuprofen", 50, 2.0, "Anti-inflammatory");
        when(medicineRepository.findById(2L)).thenReturn(Optional.of(m));
        when(medicineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Medicine res = service.updateStock(2L, 30);
        assertEquals(30, res.getQuantity());
    }
}
