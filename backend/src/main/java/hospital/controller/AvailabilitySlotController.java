package hospital.controller;

import hospital.dto.AvailabilitySlotRequest;
import hospital.entity.AvailabilitySlot;
import hospital.service.AvailabilitySlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilitySlotController {

    private final AvailabilitySlotService slotService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<List<AvailabilitySlot>> getCurrentDoctorAvailability() {
        return ResponseEntity.ok(slotService.getCurrentDoctorSlots());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<AvailabilitySlot> create(@Valid @RequestBody AvailabilitySlotRequest req) {
        LocalDateTime start;
        LocalDateTime end;
        try {
            start = LocalDateTime.parse(req.getStartDateTime());
            end = LocalDateTime.parse(req.getEndDateTime());
        } catch (DateTimeParseException ex) {
            throw new RuntimeException("Invalid datetime format");
        }
        
        // If doctorId is provided, use it; otherwise use current authenticated user
        AvailabilitySlot s;
        if (req.getDoctorId() != null) {
            s = slotService.createSlot(req.getDoctorId(), start, end);
        } else {
            s = slotService.createSlotForCurrentDoctor(start, end);
        }
        return ResponseEntity.ok(s);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<AvailabilitySlot> updateSlot(@PathVariable Long id, @RequestBody AvailabilitySlotRequest req) {
        LocalDateTime start = null;
        LocalDateTime end = null;
        try {
            if (req.getStartDateTime() != null) {
                start = LocalDateTime.parse(req.getStartDateTime());
            }
            if (req.getEndDateTime() != null) {
                end = LocalDateTime.parse(req.getEndDateTime());
            }
        } catch (DateTimeParseException ex) {
            throw new RuntimeException("Invalid datetime format");
        }
        AvailabilitySlot updated = slotService.updateSlot(id, start, end, req.getActive());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/doctor/{id}")
    public ResponseEntity<List<AvailabilitySlot>> listForDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(slotService.listForDoctor(id));
    }

    @GetMapping("/doctor/{id}/slots")
    public ResponseEntity<List<String>> listAvailableSlots(@PathVariable Long id,
                                                           @RequestParam String date,
                                                           @RequestParam(required = false) Integer slotMinutes) {
        LocalDate d;
        try {
            d = LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            throw new RuntimeException("Invalid date format. Use YYYY-MM-DD");
        }
        int minutes = slotMinutes != null ? slotMinutes : 15;
        List<LocalDateTime> slots = slotService.getAvailableSlotsForDoctorOnDate(id, d, minutes);
        List<String> iso = slots.stream().map(LocalDateTime::toString).collect(Collectors.toList());
        return ResponseEntity.ok(iso);
    }
}
