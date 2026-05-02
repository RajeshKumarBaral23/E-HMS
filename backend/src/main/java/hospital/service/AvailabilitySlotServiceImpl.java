package hospital.service;

import hospital.entity.AvailabilitySlot;
import hospital.entity.Appointment;
import hospital.entity.User;
import hospital.repository.AvailabilitySlotRepository;
import hospital.repository.AppointmentRepository;
import hospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilitySlotServiceImpl implements AvailabilitySlotService {

    private final AvailabilitySlotRepository slotRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    public AvailabilitySlot createSlot(Long doctorId, LocalDateTime start, LocalDateTime end) {
        User doctor = userRepository.findById(doctorId).orElseThrow(() -> new RuntimeException("Doctor not found"));
        AvailabilitySlot s = AvailabilitySlot.builder()
                .doctor(doctor)
                .startDateTime(start)
                .endDateTime(end)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        return slotRepository.save(s);
    }

    @Override
    public List<AvailabilitySlot> listForDoctor(Long doctorId) {
        User doctor = userRepository.findById(doctorId).orElseThrow(() -> new RuntimeException("Doctor not found"));
        return slotRepository.findByDoctor(doctor);
    }

    @Override
    public boolean isDoctorAvailable(Long doctorId, LocalDateTime start, LocalDateTime end) {
        User doctor = userRepository.findById(doctorId).orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Must be within at least one active availability slot
        // If no slots are configured at all, allow booking (opt-in availability)
        List<AvailabilitySlot> slots = slotRepository.findByDoctor(doctor);
        if (!slots.isEmpty()) {
            boolean withinSlot = slots.stream().anyMatch(s -> s.isActive() &&
                    !s.getStartDateTime().isAfter(start) && !s.getEndDateTime().isBefore(end));
            if (!withinSlot) return false;
        }

        // Check existing appointments for overlaps (exclude cancelled)
        LocalDateTime dayStart = start.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        List<Appointment> existing = appointmentRepository.findByDoctorAndAppointmentTimeBetween(doctor, dayStart, dayEnd);
        for (Appointment a : existing) {
            if (a.getStatus() != null && a.getStatus().name().equals("CANCELLED")) continue;
            LocalDateTime aStart = a.getAppointmentTime();
            LocalDateTime aEnd = aStart.plusMinutes(a.getDurationMinutes() != null ? a.getDurationMinutes() : 30);
            if (start.isBefore(aEnd) && end.isAfter(aStart)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<LocalDateTime> getAvailableSlotsForDoctorOnDate(Long doctorId, LocalDate date, int slotMinutes) {
        if (slotMinutes <= 0) slotMinutes = 15;
        User doctor = userRepository.findById(doctorId).orElseThrow(() -> new RuntimeException("Doctor not found"));

        List<AvailabilitySlot> activeSlots = slotRepository.findByDoctor(doctor).stream()
                .filter(AvailabilitySlot::isActive)
                .collect(Collectors.toList());

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        List<Appointment> existing = appointmentRepository.findByDoctorAndAppointmentTimeBetween(doctor, dayStart, dayEnd);

        List<LocalDateTime> result = new ArrayList<>();
        for (AvailabilitySlot s : activeSlots) {
            LocalDateTime slotStart = s.getStartDateTime().isBefore(dayStart) ? dayStart : s.getStartDateTime();
            LocalDateTime slotEnd = s.getEndDateTime().isAfter(dayEnd) ? dayEnd : s.getEndDateTime();

            LocalDateTime candidate = slotStart;
            while (!candidate.plusMinutes(slotMinutes).isAfter(slotEnd)) {
                LocalDateTime candidateEnd = candidate.plusMinutes(slotMinutes);
                boolean overlap = false;
                for (Appointment a : existing) {
                    if (a.getStatus() != null && a.getStatus().name().equals("CANCELLED")) continue;
                    LocalDateTime aStart = a.getAppointmentTime();
                    LocalDateTime aEnd = aStart.plusMinutes(a.getDurationMinutes() != null ? a.getDurationMinutes() : 30);
                    if (candidate.isBefore(aEnd) && candidateEnd.isAfter(aStart)) {
                        overlap = true;
                        break;
                    }
                }
                if (!overlap) {
                    if (!result.contains(candidate)) result.add(candidate);
                }
                candidate = candidate.plusMinutes(slotMinutes);
            }
        }
        result.sort(Comparator.naturalOrder());
        return result;
    }

    @Override
    public List<AvailabilitySlot> getCurrentDoctorSlots() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctor = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        return slotRepository.findByDoctor(doctor);
    }

    @Override
    public AvailabilitySlot createSlotForCurrentDoctor(LocalDateTime start, LocalDateTime end) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User doctor = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        AvailabilitySlot s = AvailabilitySlot.builder()
                .doctor(doctor)
                .startDateTime(start)
                .endDateTime(end)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        return slotRepository.save(s);
    }

    @Override
    public AvailabilitySlot updateSlot(Long slotId, LocalDateTime startDateTime, LocalDateTime endDateTime, Boolean active) {
        AvailabilitySlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Availability slot not found"));
        
        if (startDateTime != null) {
            slot.setStartDateTime(startDateTime);
        }
        if (endDateTime != null) {
            slot.setEndDateTime(endDateTime);
        }
        if (active != null) {
            slot.setActive(active);
        }
        
        return slotRepository.save(slot);
    }

}
