package hospital.controller;

import hospital.repository.AppointmentRepository;
import hospital.repository.PatientRepository;
import hospital.repository.DoctorRepository;
import hospital.repository.BillingRepository;
import hospital.entity.Appointment;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AdminController {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final BillingRepository billingRepository;

    @GetMapping("/appointments-per-day")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Integer>> appointmentsPerDay(@RequestParam(defaultValue = "7") int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        LocalDateTime start = startDate.atStartOfDay();

        List<Object[]> rows = appointmentRepository.countAppointmentsSince(start);
        Map<String, Integer> map = new HashMap<>();
        // initialize days
        for (int i = 0; i < days; i++) {
            LocalDate d = startDate.plusDays(i);
            map.put(d.toString(), 0);
        }
        for (Object[] row : rows) {
            if (row.length >= 2 && row[0] != null) {
                String day = row[0].toString();
                Integer cnt = Integer.parseInt(row[1].toString());
                map.put(day, cnt);
            }
        }
        return ResponseEntity.ok(map);
    }

    @GetMapping("/total-patients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> totalPatients() {
        long count = patientRepository.count();
        return ResponseEntity.ok(java.util.Collections.singletonMap("totalPatients", count));
    }

    @GetMapping("/total-doctors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> totalDoctors() {
        long count = doctorRepository.count();
        return ResponseEntity.ok(java.util.Collections.singletonMap("totalDoctors", count));
    }

    @GetMapping("/revenue-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> revenueSummary(@RequestParam(defaultValue = "30") int days) {
        java.time.LocalDateTime since = java.time.LocalDateTime.now().minusDays(days);
        double total = billingRepository.findAll().stream()
                .filter(b -> b.getCreatedAt() != null && b.getCreatedAt().isAfter(since))
                .mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount() : 0.0)
                .sum();
        Map<String, Object> out = new java.util.HashMap<>();
        out.put("days", days);
        out.put("revenue", total);
        return ResponseEntity.ok(out);
    }

    @GetMapping(value = "/reports/export", produces = "text/csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportReports() {
        List<Appointment> appointments = appointmentRepository.findAll();
        StringBuilder csv = new StringBuilder("id,patient,doctor,appointmentTime,status,duration,reason\n");
        for (Appointment a : appointments) {
            csv.append(safe(a.getId())).append(",")
               .append(safe(a.getPatient() != null ? a.getPatient().getName() : "")).append(",")
               .append(safe(a.getDoctor() != null ? a.getDoctor().getName() : "")).append(",")
               .append(safe(a.getAppointmentTime())).append(",")
               .append(safe(a.getStatus())).append(",")
               .append(safe(a.getDurationMinutes())).append(",")
               .append(safe(a.getReason())).append("\n");
        }
        byte[] bytes = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"appointments-export.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    private static String safe(Object val) {
        if (val == null) return "";
        String s = val.toString().replace("\"", "\"\"");
        return s.contains(",") || s.contains("\"") || s.contains("\n") ? "\"" + s + "\"" : s;
    }
}
