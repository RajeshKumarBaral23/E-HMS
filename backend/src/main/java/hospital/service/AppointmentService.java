package hospital.service;

import hospital.dto.AppointmentRequest;
import hospital.entity.Appointment;

import java.util.List;

public interface AppointmentService {
    Appointment bookAppointment(AppointmentRequest request);
    Appointment bookAppointmentForPatient(hospital.dto.AdminAppointmentRequest request);
    Appointment rescheduleAppointment(Long appointmentId, String newAppointmentTime, Integer newDurationMinutes);
    List<Appointment> getAppointmentsForCurrentUser();
    Appointment updateAppointmentStatus(Long appointmentId, String newStatus, String followUpDate);
    java.util.List<hospital.entity.Appointment> getTodaysAppointmentsForDoctor();
    hospital.entity.Appointment getNextAppointmentForDoctor();
}
