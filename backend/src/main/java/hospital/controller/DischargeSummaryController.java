package hospital.controller;

import hospital.dto.DischargeSummaryRequest;
import hospital.dto.DischargeSummaryResponse;
import hospital.entity.DischargeSummary;
import hospital.service.DischargeSummaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/discharge-summaries")
@RequiredArgsConstructor
public class DischargeSummaryController {

    private final DischargeSummaryService dischargeSummaryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public ResponseEntity<DischargeSummaryResponse> create(@Valid @RequestBody DischargeSummaryRequest request) {
        DischargeSummary saved = dischargeSummaryService.createSummary(request);
        return ResponseEntity.ok(toResponse(saved));
    }

    @GetMapping
    public ResponseEntity<List<DischargeSummaryResponse>> list(@RequestParam(required = false) Long appointmentId) {
        List<DischargeSummary> list = dischargeSummaryService.listForCurrentUser(appointmentId);
        return ResponseEntity.ok(list.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DischargeSummaryResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(dischargeSummaryService.getById(id)));
    }

    private DischargeSummaryResponse toResponse(DischargeSummary s) {
        return DischargeSummaryResponse.builder()
                .id(s.getId())
                .appointmentId(s.getAppointment() != null ? s.getAppointment().getId() : null)
                .patientId(s.getPatient() != null ? s.getPatient().getId() : null)
                .doctorId(s.getDoctor() != null ? s.getDoctor().getId() : null)
                .summary(s.getSummary())
                .instructions(s.getInstructions())
                .dischargeDate(s.getDischargeDate() != null ? s.getDischargeDate().toLocalDate() : null)
                .followUpDate(s.getFollowUpDate())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
