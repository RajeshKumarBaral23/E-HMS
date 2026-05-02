package hospital.controller;

import hospital.dto.AdminAppointmentRequest;
import hospital.entity.Appointment;
import hospital.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/appointments")
@RequiredArgsConstructor
public class AdminAppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Appointment> create(@RequestBody AdminAppointmentRequest request) {
        Appointment a = appointmentService.bookAppointmentForPatient(request);
        return ResponseEntity.ok(a);
    }
}
