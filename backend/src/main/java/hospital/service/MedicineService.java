package hospital.service;

import hospital.entity.Medicine;

import java.util.List;

public interface MedicineService {
    Medicine addMedicine(Medicine medicine);
    Medicine updateStock(Long id, int newQuantity);
    List<Medicine> getAllMedicines();
}
