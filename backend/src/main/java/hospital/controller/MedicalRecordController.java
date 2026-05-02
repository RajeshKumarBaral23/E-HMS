package hospital.controller;

import hospital.dto.MedicalRecordRequest;
import hospital.entity.MedicalRecord;
import hospital.entity.Role;
import hospital.entity.User;
import hospital.service.MedicalRecordService;
import hospital.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<MedicalRecord> create(@Valid @RequestBody MedicalRecordRequest req) {
        MedicalRecord mr = medicalRecordService.create(req);
        return ResponseEntity.ok(mr);
    }

    @GetMapping
    public ResponseEntity<List<MedicalRecord>> listForCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            List<MedicalRecord> list = medicalRecordService.listAll();
            return ResponseEntity.ok(list);
        } else if (user.getRole() == Role.DOCTOR) {
            List<MedicalRecord> list = medicalRecordService.listForDoctor(user.getId());
            return ResponseEntity.ok(list);
        } else {
            List<MedicalRecord> list = medicalRecordService.listForPatient(user.getId());
            return ResponseEntity.ok(list);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecord> get(@PathVariable Long id) {
        MedicalRecord mr = medicalRecordService.getById(id);
        return ResponseEntity.ok(mr);
    }
}
