package hospital.service;

import hospital.entity.Medicine;
import hospital.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;

    @Override
    public Medicine addMedicine(Medicine medicine) {
        return medicineRepository.save(medicine);
    }

    @Override
    public Medicine updateStock(Long id, int newQuantity) {
        Medicine m = medicineRepository.findById(id).orElseThrow(() -> new RuntimeException("Medicine not found"));
        m.setQuantity(newQuantity);
        return medicineRepository.save(m);
    }

    @Override
    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }
}
