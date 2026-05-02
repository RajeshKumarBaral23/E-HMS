package hospital.service;

import hospital.entity.AvailabilitySlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AvailabilitySlotService {
    AvailabilitySlot createSlot(Long doctorId, LocalDateTime start, LocalDateTime end);
    List<AvailabilitySlot> listForDoctor(Long doctorId);
    boolean isDoctorAvailable(Long doctorId, LocalDateTime start, LocalDateTime end);

    // Returns discrete available start times (ISO LocalDateTime) for the given date
    List<LocalDateTime> getAvailableSlotsForDoctorOnDate(Long doctorId, LocalDate date, int slotMinutes);

    // Get current authenticated doctor's availability slots
    List<AvailabilitySlot> getCurrentDoctorSlots();

    // Create availability slot for current authenticated doctor
    AvailabilitySlot createSlotForCurrentDoctor(LocalDateTime start, LocalDateTime end);

    // Update an availability slot (for editing times or deactivating)
    AvailabilitySlot updateSlot(Long slotId, LocalDateTime startDateTime, LocalDateTime endDateTime, Boolean active);
}
