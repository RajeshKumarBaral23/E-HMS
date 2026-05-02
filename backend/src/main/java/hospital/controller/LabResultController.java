package hospital.controller;

import hospital.dto.LabResultRequest;
import hospital.dto.LabResultResponse;
import hospital.entity.LabResult;
import hospital.entity.Role;
import hospital.entity.User;
import hospital.repository.UserRepository;
import hospital.service.LabResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lab-results")
@RequiredArgsConstructor
public class LabResultController {

    private final LabResultService labResultService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<LabResultResponse> create(@Valid @RequestBody LabResultRequest request) {
        LabResult lr = labResultService.create(request);
        return ResponseEntity.ok(toResponse(lr));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<List<LabResultResponse>> list(@RequestParam(required = false) Long patientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        List<LabResult> list;
        if (user.getRole() == Role.PATIENT) {
            list = labResultService.getByPatientId(user.getId());
        } else if (patientId != null) {
            list = labResultService.getByPatientId(patientId);
        } else {
            list = labResultService.getAll();
        }

        List<LabResultResponse> resp = list.stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<LabResultResponse> get(@PathVariable Long id) {
        LabResult lr = labResultService.getById(id);
        // Authorization: patients can only view their own
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() == Role.PATIENT && !lr.getPatient().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(toResponse(lr));
    }

    private LabResultResponse toResponse(LabResult lr) {
        return LabResultResponse.builder()
                .id(lr.getId())
                .patientId(lr.getPatient() != null ? lr.getPatient().getId() : null)
                .doctorId(lr.getDoctor() != null ? lr.getDoctor().getId() : null)
                .appointmentId(lr.getAppointment() != null ? lr.getAppointment().getId() : null)
                .testType(lr.getTestType() != null ? lr.getTestType().name() : null)
                .result(lr.getResult())
                .createdAt(lr.getCreatedAt())
                .build();
    }
}
