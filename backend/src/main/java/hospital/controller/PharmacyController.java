package hospital.controller;

import hospital.entity.Medicine;
import hospital.entity.PharmacyRecord;
import hospital.service.MedicineService;
import hospital.service.PharmacyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacy")
@RequiredArgsConstructor
public class PharmacyController {

    private final MedicineService medicineService;
    private final PharmacyService pharmacyService;

    @PostMapping("/medicines")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Medicine> addMedicine(@RequestBody Medicine m) {
        return ResponseEntity.ok(medicineService.addMedicine(m));
    }

    @GetMapping("/medicines")
    public ResponseEntity<List<Medicine>> listMedicines() {
        return ResponseEntity.ok(medicineService.getAllMedicines());
    }

    @PutMapping("/medicines/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Medicine> updateMedicine(@PathVariable Long id, @RequestBody Medicine m) {
        return ResponseEntity.ok(medicineService.updateStock(id, m.getQuantity()));
    }

    @PostMapping("/dispense")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<PharmacyRecord> dispense(@RequestParam Long prescriptionId, @RequestParam Long medicineId, @RequestParam int quantity) {
        return ResponseEntity.ok(pharmacyService.dispenseMedicine(prescriptionId, medicineId, quantity));
    }

    @PostMapping("/purchase")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN','DOCTOR')")
    public ResponseEntity<PharmacyRecord> purchase(@RequestParam Long medicineId,
                                                   @RequestParam int quantity,
                                                   @RequestParam(required = false) Long appointmentId,
                                                   Authentication authentication) {
        return ResponseEntity.ok(pharmacyService.purchaseMedicine(medicineId, quantity, authentication.getName(), appointmentId));
    }
}
