package hospital.controller;

import hospital.dto.PatientRequest;
import hospital.dto.PatientResponse;
import hospital.entity.Patient;
import hospital.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientRequest request) {
        Patient patient = patientService.createPatient(request);
        return ResponseEntity.ok(toResponse(patient));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PatientResponse>> listPatients() {
        List<Patient> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/me")
    public ResponseEntity<PatientResponse> getCurrentPatient() {
        Patient patient = patientService.getCurrentPatient();
        return ResponseEntity.ok(toResponse(patient));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PatientResponse> getPatient(@PathVariable Long id) {
        Patient patient = patientService.getPatientById(id);
        return ResponseEntity.ok(toResponse(patient));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPatientHistory(@PathVariable Long id) {
        var history = patientService.getPatientHistory(id);
        return ResponseEntity.ok(history);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PatientResponse> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientRequest request) {
        Patient patient = patientService.updatePatient(id, request);
        return ResponseEntity.ok(toResponse(patient));
    }

    @PutMapping("/me")
    public ResponseEntity<PatientResponse> updateCurrentPatient(@Valid @RequestBody PatientRequest request) {
        Patient patient = patientService.updateCurrentPatient(request);
        return ResponseEntity.ok(toResponse(patient));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    private PatientResponse toResponse(Patient patient) {
        return PatientResponse.builder()
                .id(patient.getId())
                .userId(patient.getUser() != null ? patient.getUser().getId() : null)
                .name(patient.getUser() != null ? patient.getUser().getName() : null)
                .email(patient.getUser() != null ? patient.getUser().getEmail() : null)
                .phone(patient.getPhone())
                .address(patient.getAddress())
                .dob(patient.getDob() != null ? patient.getDob().toString() : null)
                .sex(patient.getSex())
                .age(patient.getAge())
                .build();
    }
}
