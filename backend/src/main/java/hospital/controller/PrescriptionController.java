package hospital.controller;

import hospital.dto.PrescriptionMedicineRequest;
import hospital.dto.PrescriptionRequest;
import hospital.entity.Prescription;
import hospital.entity.PrescriptionMedicine;
import hospital.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<Prescription> createPrescription(@Valid @RequestBody PrescriptionRequest request) {
        Prescription p = prescriptionService.createPrescription(request);
        return ResponseEntity.ok(p);
    }

    @GetMapping
    public ResponseEntity<List<Prescription>> getPrescriptions() {
        List<Prescription> list = prescriptionService.getPrescriptionsForCurrentUser();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{id}/medicines")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<PrescriptionMedicine> addMedicine(@PathVariable Long id, @Valid @RequestBody PrescriptionMedicineRequest req) {
        PrescriptionMedicine pm = prescriptionService.addMedicineToPrescription(id, req);
        return ResponseEntity.ok(pm);
    }

    @GetMapping("/{id}/medicines")
    public ResponseEntity<List<PrescriptionMedicine>> getMedicines(@PathVariable Long id) {
        List<PrescriptionMedicine> list = prescriptionService.getMedicinesForPrescription(id);
        return ResponseEntity.ok(list);
    }
}
