package hospital.service;

import hospital.dto.DischargeSummaryRequest;
import hospital.entity.Appointment;
import hospital.entity.DischargeSummary;
import hospital.entity.Role;
import hospital.entity.User;
import hospital.repository.AppointmentRepository;
import hospital.repository.DischargeSummaryRepository;
import hospital.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DischargeSummaryServiceImpl implements DischargeSummaryService {

    private final DischargeSummaryRepository dischargeSummaryRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Override
    public DischargeSummary createSummary(DischargeSummaryRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() == Role.DOCTOR && !appointment.getDoctor().getId().equals(user.getId())) {
            throw new RuntimeException("Doctor may only discharge their own appointments");
        }

        LocalDate followUp = null;
        if (request.getFollowUpDate() != null && !request.getFollowUpDate().isBlank()) {
            followUp = LocalDate.parse(request.getFollowUpDate());
        }

        DischargeSummary summary = DischargeSummary.builder()
                .appointment(appointment)
                .patient(appointment.getPatient())
                .doctor(appointment.getDoctor())
                .summary(request.getSummary())
                .instructions(request.getInstructions())
                .dischargeDate(LocalDateTime.now())
                .followUpDate(followUp)
                .createdAt(LocalDateTime.now())
                .build();

        return dischargeSummaryRepository.save(summary);
    }

    @Override
    public List<DischargeSummary> listForCurrentUser(Long appointmentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            if (appointmentId != null) {
                return dischargeSummaryRepository.findByAppointment_Id(appointmentId);
            }
            return dischargeSummaryRepository.findAll();
        }

        if (appointmentId != null) {
            return dischargeSummaryRepository.findByAppointment_Id(appointmentId);
        }

        if (user.getRole() == Role.DOCTOR) {
            return dischargeSummaryRepository.findByDoctor_Id(user.getId());
        }

        return dischargeSummaryRepository.findByPatient_Id(user.getId());
    }

    @Override
    public DischargeSummary getById(Long id) {
        DischargeSummary summary = dischargeSummaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discharge summary not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.PATIENT && !summary.getPatient().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        if (user.getRole() == Role.DOCTOR && !summary.getDoctor().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        return summary;
    }
}
