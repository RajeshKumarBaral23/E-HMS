package hospital.controller;

import hospital.entity.Billing;
import hospital.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/generate/{appointmentId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public ResponseEntity<Billing> generate(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(billingService.generateBill(appointmentId));
    }

    @PostMapping("/generate-aggregated")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public ResponseEntity<Billing> generateAggregated(@RequestParam Long patientId, @RequestParam Long admissionId) {
        return ResponseEntity.ok(billingService.generateAggregatedBill(patientId, admissionId));
    }

    @GetMapping
    public ResponseEntity<List<Billing>> listBills() {
        return ResponseEntity.ok(billingService.listBillsForCurrentUser());
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Billing>> listBillsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(billingService.getBillsByPatient(patientId));
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Billing> pay(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.markAsPaid(id));
    }
}
