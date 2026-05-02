package hospital.service;

import hospital.dto.AppointmentRequest;
import hospital.entity.Appointment;
import hospital.entity.AppointmentStatus;
import hospital.entity.Role;
import hospital.entity.User;
import hospital.repository.AppointmentRepository;
import hospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.time.LocalDate;
import hospital.dto.AppointmentStatusUpdateRequest;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final AvailabilitySlotService availabilitySlotService;
    private final hospital.service.EmailService emailService;

    @Override
    public Appointment bookAppointment(AppointmentRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String patientEmail = auth.getName();
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("Patient user not found"));

        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor user not found"));

        if (doctor.getRole() != Role.DOCTOR) {
            throw new RuntimeException("Selected user is not a doctor");
        }

        java.time.LocalDateTime apptTime = LocalDateTime.parse(request.getAppointmentTime());

        java.time.LocalDate startDate = apptTime.toLocalDate();
        java.time.LocalDateTime dayStart = startDate.atStartOfDay();
        java.time.LocalDateTime dayEnd = dayStart.plusDays(1);

        // compute requested slot
        java.time.LocalDateTime start = apptTime;
        int duration = request.getDurationMinutes() != null ? request.getDurationMinutes() : 30;
        java.time.LocalDateTime end = start.plusMinutes(duration);

        // availability check via configured slots
        if (!availabilitySlotService.isDoctorAvailable(doctor.getId(), start, end)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor not available at requested time");
        }

        List<Appointment> existing = appointmentRepository.findByDoctorAndAppointmentTimeBetween(doctor, dayStart, dayEnd);
        for (Appointment a : existing) {
            if (a.getStatus() != null && a.getStatus() == AppointmentStatus.CANCELLED) continue;
            java.time.LocalDateTime aStart = a.getAppointmentTime();
            java.time.LocalDateTime aEnd = aStart.plusMinutes(a.getDurationMinutes() != null ? a.getDurationMinutes() : 30);
            if (start.isBefore(aEnd) && end.isAfter(aStart)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Requested time overlaps an existing appointment");
            }
        }

        int maxQueue = existing.stream()
            .map(Appointment::getQueueNumber)
            .filter(java.util.Objects::nonNull)
            .max(Integer::compareTo)
            .orElse(0);

        int queueNumber = maxQueue + 1;

        Appointment appointment = Appointment.builder()
            .doctor(doctor)
            .patient(patient)
            .appointmentTime(apptTime)
            .durationMinutes(request.getDurationMinutes())
            .status(AppointmentStatus.PENDING)
            .reason(request.getReason())
            .queueNumber(queueNumber)
            .build();

        Appointment saved = appointmentRepository.save(appointment);
        try { hospital.controller.AppointmentController.publishEvent(saved); } catch (Exception ignored) {}
        try { emailService.sendAppointmentCreated(saved); } catch (Exception ignored) {}
        return saved;
    }

    @Override
    public Appointment bookAppointmentForPatient(hospital.dto.AdminAppointmentRequest request) {
        User patient = userRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient user not found"));

        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor user not found"));

        if (doctor.getRole() != Role.DOCTOR) {
            throw new RuntimeException("Selected user is not a doctor");
        }

        java.time.LocalDateTime apptTime = LocalDateTime.parse(request.getAppointmentTime());

        java.time.LocalDate startDate = apptTime.toLocalDate();
        java.time.LocalDateTime dayStart = startDate.atStartOfDay();
        java.time.LocalDateTime dayEnd = dayStart.plusDays(1);

        // compute requested slot
        java.time.LocalDateTime start = apptTime;
        int duration = request.getDurationMinutes() != null ? request.getDurationMinutes() : 30;
        java.time.LocalDateTime end = start.plusMinutes(duration);

        // availability check via configured slots
        if (!availabilitySlotService.isDoctorAvailable(doctor.getId(), start, end)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor not available at requested time");
        }

        List<Appointment> existing = appointmentRepository.findByDoctorAndAppointmentTimeBetween(doctor, dayStart, dayEnd);
        for (Appointment a : existing) {
            if (a.getStatus() != null && a.getStatus() == AppointmentStatus.CANCELLED) continue;
            java.time.LocalDateTime aStart = a.getAppointmentTime();
            java.time.LocalDateTime aEnd = aStart.plusMinutes(a.getDurationMinutes() != null ? a.getDurationMinutes() : 30);
            if (start.isBefore(aEnd) && end.isAfter(aStart)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Requested time overlaps an existing appointment");
            }
        }

        int maxQueue = existing.stream()
                .map(Appointment::getQueueNumber)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);

        int queueNumber = maxQueue + 1;

        Appointment appt = Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .appointmentTime(apptTime)
                .durationMinutes(request.getDurationMinutes())
                .status(AppointmentStatus.PENDING)
                .reason(request.getReason())
                .queueNumber(queueNumber)
                .build();

        Appointment savedAdmin = appointmentRepository.save(appt);
        try { hospital.controller.AppointmentController.publishEvent(savedAdmin); } catch (Exception ignored) {}
        try { emailService.sendAppointmentCreated(savedAdmin); } catch (Exception ignored) {}
        return savedAdmin;
    }

    @Override
    public Appointment rescheduleAppointment(Long appointmentId, String newAppointmentTime, Integer newDurationMinutes) {
        Appointment original = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        // Authorization: patient may reschedule their own, doctor may reschedule their own, admin may reschedule any
        if (user.getRole() == Role.PATIENT) {
            if (!original.getPatient().getId().equals(user.getId())) throw new RuntimeException("Patient may only reschedule their own appointment");
        } else if (user.getRole() == Role.DOCTOR) {
            if (!original.getDoctor().getId().equals(user.getId())) throw new RuntimeException("Doctor may only reschedule their own appointments");
        }

        java.time.LocalDateTime newStart;
        try {
            newStart = java.time.LocalDateTime.parse(newAppointmentTime);
        } catch (Exception ex) {
            throw new RuntimeException("Invalid datetime format for new appointment");
        }

        int duration = newDurationMinutes != null ? newDurationMinutes : (original.getDurationMinutes() != null ? original.getDurationMinutes() : 30);
        java.time.LocalDateTime newEnd = newStart.plusMinutes(duration);

        // Availability check - requires a slot covering the requested time and no overlapping appointments
        boolean available = availabilitySlotService.isDoctorAvailable(original.getDoctor().getId(), newStart, newEnd);
        if (!available) throw new RuntimeException("Doctor not available at requested time");

        // Check overlapping appointments for the day
        java.time.LocalDate startDate = newStart.toLocalDate();
        java.time.LocalDateTime dayStart = startDate.atStartOfDay();
        java.time.LocalDateTime dayEnd = dayStart.plusDays(1);
        java.util.List<Appointment> existing = appointmentRepository.findByDoctorAndAppointmentTimeBetween(original.getDoctor(), dayStart, dayEnd);
        for (Appointment a : existing) {
            if (a.getId().equals(original.getId())) continue;
            if (a.getStatus() != null && a.getStatus() == AppointmentStatus.CANCELLED) continue;
            java.time.LocalDateTime aStart = a.getAppointmentTime();
            java.time.LocalDateTime aEnd = aStart.plusMinutes(a.getDurationMinutes() != null ? a.getDurationMinutes() : 30);
            if (newStart.isBefore(aEnd) && newEnd.isAfter(aStart)) {
                throw new RuntimeException("Requested time overlaps an existing appointment");
            }
        }

        // Compute queue number for that day
        int maxQueue = existing.stream()
                .map(Appointment::getQueueNumber)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
        int queueNumber = maxQueue + 1;

        Appointment newAppt = Appointment.builder()
                .doctor(original.getDoctor())
                .patient(original.getPatient())
                .appointmentTime(newStart)
                .durationMinutes(duration)
                .status(AppointmentStatus.PENDING)
                .reason(original.getReason() != null ? "Rescheduled: " + original.getReason() : "Rescheduled")
                .queueNumber(queueNumber)
                .build();

        Appointment saved = appointmentRepository.save(newAppt);

        // Cancel original
        original.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(original);

        // Publish events and send emails (best-effort)
        try { hospital.controller.AppointmentController.publishEvent(saved); } catch (Exception ignored) {}
        try { hospital.controller.AppointmentController.publishEvent(original); } catch (Exception ignored) {}
        try { emailService.sendAppointmentRescheduled(original, saved); } catch (Exception ignored) {}

        return saved;
    }

    @Override
    public Appointment updateAppointmentStatus(Long appointmentId, String newStatusStr, String followUpDate) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        AppointmentStatus newStatus;
        try {
            newStatus = AppointmentStatus.valueOf(newStatusStr);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid status: " + newStatusStr);
        }

        AppointmentStatus current = appointment.getStatus();

        if (!isValidTransition(current, newStatus)) {
            throw new RuntimeException("Invalid status transition from " + current + " to " + newStatus);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.DOCTOR) {
            if (!appointment.getDoctor().getId().equals(user.getId())) {
                throw new RuntimeException("Doctor may only update their own appointments");
            }
        } else if (user.getRole() == Role.PATIENT) {
            // Allow patients to check in or cancel their own appointment
            if (!appointment.getPatient().getId().equals(user.getId())) {
                throw new RuntimeException("Patient may only update their own appointments");
            }
            if (!(newStatus == AppointmentStatus.CHECKED_IN || newStatus == AppointmentStatus.CANCELLED)) {
                throw new RuntimeException("Patients may only perform check-in or cancel");
            }
        } else if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only doctors, admins, or patients (limited) can update appointment status");
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        switch (newStatus) {
            case CHECKED_IN:
                appointment.setCheckInTime(now);
                break;
            case IN_PROGRESS:
                appointment.setConsultationStartTime(now);
                break;
            case COMPLETED:
                appointment.setConsultationEndTime(now);
                break;
            default:
                break;
        }

        if (followUpDate != null && !followUpDate.isBlank()) {
            appointment.setFollowUpDate(LocalDate.parse(followUpDate));
        }

        appointment.setStatus(newStatus);

        Appointment saved = appointmentRepository.save(appointment);
        try {
            hospital.controller.AppointmentController.publishEvent(saved);
        } catch (Exception ignored) {
        }
        if (newStatus == AppointmentStatus.CANCELLED) {
            try { emailService.sendAppointmentCancelled(saved); } catch (Exception ignored) {}
        }
        return saved;
    }

    private boolean isValidTransition(AppointmentStatus from, AppointmentStatus to) {
        if (from == null) return true;
        switch (from) {
            case PENDING:
                return to == AppointmentStatus.CONFIRMED || to == AppointmentStatus.CANCELLED;
            case CONFIRMED:
                return to == AppointmentStatus.CHECKED_IN || to == AppointmentStatus.CANCELLED;
            case CHECKED_IN:
                return to == AppointmentStatus.IN_PROGRESS || to == AppointmentStatus.CANCELLED;
            case IN_PROGRESS:
                return to == AppointmentStatus.COMPLETED || to == AppointmentStatus.CANCELLED;
            case COMPLETED:
            case CANCELLED:
                return false;
            default:
                return false;
        }
    }

    @Override
    public List<Appointment> getAppointmentsForCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            return appointmentRepository.findAll();
        } else if (user.getRole() == Role.DOCTOR) {
            return appointmentRepository.findByDoctor(user);
        } else {
            return appointmentRepository.findByPatient(user);
        }
    }

    @Override
    public java.util.List<Appointment> getTodaysAppointmentsForDoctor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.DOCTOR) {
            throw new RuntimeException("Only doctors can view today's appointments for doctors");
        }

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDateTime start = today.atStartOfDay();
        java.time.LocalDateTime end = start.plusDays(1);

        java.util.List<Appointment> list = appointmentRepository.findByDoctorAndAppointmentTimeBetween(user, start, end);
        list.sort((a, b) -> {
            Integer qa = a.getQueueNumber() != null ? a.getQueueNumber() : Integer.MAX_VALUE;
            Integer qb = b.getQueueNumber() != null ? b.getQueueNumber() : Integer.MAX_VALUE;
            return qa.compareTo(qb);
        });
        return list;
    }

    @Override
    public Appointment getNextAppointmentForDoctor() {
        java.util.List<Appointment> todays = getTodaysAppointmentsForDoctor();
        // Prefer checked-in patients by queue order
        return todays.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CHECKED_IN)
                .min((a, b) -> {
                    Integer qa = a.getQueueNumber() != null ? a.getQueueNumber() : Integer.MAX_VALUE;
                    Integer qb = b.getQueueNumber() != null ? b.getQueueNumber() : Integer.MAX_VALUE;
                    return qa.compareTo(qb);
                })
                .orElseGet(() -> todays.stream()
                        .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED || a.getStatus() == AppointmentStatus.PENDING)
                        .min((a, b) -> {
                            Integer qa = a.getQueueNumber() != null ? a.getQueueNumber() : Integer.MAX_VALUE;
                            Integer qb = b.getQueueNumber() != null ? b.getQueueNumber() : Integer.MAX_VALUE;
                            return qa.compareTo(qb);
                        }).orElse(null));
    }
}
