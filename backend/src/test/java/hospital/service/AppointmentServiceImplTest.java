package hospital.service;

import hospital.entity.Appointment;
import hospital.entity.AppointmentStatus;
import hospital.entity.Role;
import hospital.entity.User;
import hospital.repository.AppointmentRepository;
import hospital.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentServiceImplTest {

    private AppointmentRepository appointmentRepository;
    private UserRepository userRepository;
    private hospital.service.AvailabilitySlotService availabilitySlotService;
    private hospital.service.EmailService emailService;
    private AppointmentServiceImpl service;

    @BeforeEach
    void setup() {
        appointmentRepository = mock(AppointmentRepository.class);
        userRepository = mock(UserRepository.class);
        availabilitySlotService = mock(hospital.service.AvailabilitySlotService.class);
        emailService = mock(hospital.service.EmailService.class);
        service = new AppointmentServiceImpl(appointmentRepository, userRepository, availabilitySlotService, emailService);
        // Default save behavior for mocks: return the saved entity
        when(appointmentRepository.save(ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testValidTransitionPendingToConfirmed() {
        User admin = new User(); admin.setId(1L); admin.setEmail("admin@test"); admin.setRole(Role.ADMIN);
        Appointment appt = Appointment.builder().id(10L).status(AppointmentStatus.PENDING).build();
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appt));

        // set security context as admin
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(admin.getEmail(), null, null));
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));

        Appointment res = service.updateAppointmentStatus(10L, "CONFIRMED", null);
        assertEquals(AppointmentStatus.CONFIRMED, res.getStatus());
        verify(appointmentRepository, times(1)).save(ArgumentMatchers.any());
    }

    @Test
    void testInvalidTransitionCompletedToInProgress() {
        User admin = new User(); admin.setId(1L); admin.setEmail("admin@test"); admin.setRole(Role.ADMIN);
        Appointment appt = Appointment.builder().id(11L).status(AppointmentStatus.COMPLETED).build();
        when(appointmentRepository.findById(11L)).thenReturn(Optional.of(appt));

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(admin.getEmail(), null, null));
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.updateAppointmentStatus(11L, "IN_PROGRESS", null));
        assertTrue(ex.getMessage().contains("Invalid status transition"));
    }

    @Test
    void testPatientCheckInAllowed() {
        User patient = new User(); patient.setId(2L); patient.setEmail("pat@test"); patient.setRole(Role.PATIENT);
        Appointment appt = Appointment.builder().id(12L).status(AppointmentStatus.CONFIRMED).patient(patient).build();
        when(appointmentRepository.findById(12L)).thenReturn(Optional.of(appt));

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(patient.getEmail(), null, null));
        when(userRepository.findByEmail(patient.getEmail())).thenReturn(Optional.of(patient));

        Appointment res = service.updateAppointmentStatus(12L, "CHECKED_IN", null);
        assertEquals(AppointmentStatus.CHECKED_IN, res.getStatus());
    }
}
