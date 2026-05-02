package hospital.controller;

import hospital.dto.DoctorRequest;
import hospital.dto.DoctorResponse;
import hospital.entity.Doctor;
import hospital.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponse> createDoctor(@Valid @RequestBody DoctorRequest request) {
        Doctor d = doctorService.createDoctor(request);
        return ResponseEntity.ok(toResponse(d));
    }

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> listDoctors() {
        List<Doctor> list = doctorService.getAllDoctors();
        List<DoctorResponse> resp = list.stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getDoctor(@PathVariable Long id) {
        Doctor d = doctorService.getDoctorById(id);
        return ResponseEntity.ok(toResponse(d));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponse> updateDoctor(@PathVariable Long id, @Valid @RequestBody DoctorRequest request) {
        Doctor d = doctorService.updateDoctor(id, request);
        return ResponseEntity.ok(toResponse(d));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    private DoctorResponse toResponse(Doctor d) {
        return DoctorResponse.builder()
                .id(d.getId())
                .userId(d.getUser() != null ? d.getUser().getId() : null)
                .name(d.getUser() != null ? d.getUser().getName() : null)
                .email(d.getUser() != null ? d.getUser().getEmail() : null)
                .specialization(d.getSpecialization())
                .phone(d.getPhone())
                .bio(d.getBio())
                .departmentId(d.getDepartment() != null ? d.getDepartment().getId() : null)
                .departmentName(d.getDepartment() != null ? d.getDepartment().getName() : null)
                .build();
    }
}
