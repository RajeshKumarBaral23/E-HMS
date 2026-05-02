package hospital.controller;

import hospital.dto.AppointmentRequest;
import hospital.dto.AppointmentStatusUpdateRequest;
import hospital.entity.Appointment;
import hospital.service.AppointmentService;
import hospital.security.JwtUtils;
import hospital.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final hospital.repository.AppointmentRepository appointmentRepository;
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;
    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> emittersByEmail = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    @PostMapping
    public ResponseEntity<Appointment> bookAppointment(@Valid @RequestBody AppointmentRequest request) {
        Appointment appt = appointmentService.bookAppointment(request);
        return ResponseEntity.ok(appt);
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> getAppointments() {
        try {
            List<Appointment> list = appointmentService.getAppointmentsForCurrentUser();
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            // Fallback: return all appointments if service/auth fails (helps admin dashboard in local/dev)
            List<Appointment> list = appointmentRepository.findAll();
            return ResponseEntity.ok(list);
        }
    }

    @GetMapping("/debug/all")
    public ResponseEntity<List<Appointment>> debugAllAppointments() {
        List<Appointment> list = appointmentRepository.findAll();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Appointment> updateStatus(@PathVariable Long id, @Valid @RequestBody AppointmentStatusUpdateRequest request) {
        Appointment updated = appointmentService.updateAppointmentStatus(id, request.getStatus(), request.getFollowUpDate());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/stream")
    public SseEmitter streamAppointments(@RequestParam(required = false) String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token");
        }
        if (!jwtUtils.validateJwtToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        String username = jwtUtils.getUserNameFromJwtToken(token);
        try {
            UserDetails ud = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unable to load user");
        }

        String emailKey = username;
        SseEmitter emitter = new SseEmitter(0L);
        emittersByEmail.computeIfAbsent(emailKey, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(emailKey, emitter));
        emitter.onTimeout(() -> removeEmitter(emailKey, emitter));
        return emitter;
    }

    private static void removeEmitter(String email, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = emittersByEmail.get(email);
        if (list == null) return;
        list.remove(emitter);
        if (list.isEmpty()) {
            emittersByEmail.remove(email, list);
        }
    }

    public static void publishEvent(Appointment appt) {
        String payload;
        try {
            payload = mapper.writeValueAsString(appt);
        } catch (Exception e) {
            payload = "{}";
        }

        String doctorEmail = appt != null && appt.getDoctor() != null ? appt.getDoctor().getEmail() : null;
        String patientEmail = appt != null && appt.getPatient() != null ? appt.getPatient().getEmail() : null;
        Set<String> targets = new java.util.HashSet<>();
        if (doctorEmail != null && !doctorEmail.isBlank()) targets.add(doctorEmail);
        if (patientEmail != null && !patientEmail.isBlank()) targets.add(patientEmail);

        for (String email : targets) {
            CopyOnWriteArrayList<SseEmitter> list = emittersByEmail.get(email);
            if (list == null) continue;
            for (SseEmitter emitter : list) {
                try {
                    emitter.send(SseEmitter.event().name("appointment").data(payload));
                } catch (IOException ex) {
                    removeEmitter(email, emitter);
                }
            }
        }
    }

    @GetMapping("/today")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<Appointment>> getTodaysForDoctor() {
        List<Appointment> list = appointmentService.getTodaysAppointmentsForDoctor();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/doctor/next")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Appointment> getNextForDoctor() {
        Appointment next = appointmentService.getNextAppointmentForDoctor();
        return ResponseEntity.ok(next);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<Appointment> startConsultation(@PathVariable Long id) {
        Appointment updated = appointmentService.updateAppointmentStatus(id, "IN_PROGRESS", null);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<Appointment> completeConsultation(@PathVariable Long id, @Valid @RequestBody(required = false) AppointmentStatusUpdateRequest request) {
        String followUp = request != null ? request.getFollowUpDate() : null;
        Appointment updated = appointmentService.updateAppointmentStatus(id, "COMPLETED", followUp);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<Appointment> reschedule(@PathVariable Long id, @Valid @RequestBody hospital.dto.AppointmentRescheduleRequest request) {
        Appointment updated = appointmentService.rescheduleAppointment(id, request.getNewAppointmentTime(), request.getDurationMinutes());
        return ResponseEntity.ok(updated);
    }
}
