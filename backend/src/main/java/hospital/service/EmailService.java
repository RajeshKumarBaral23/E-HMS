package hospital.service;

import hospital.entity.Appointment;

public interface EmailService {
    void sendAppointmentCreated(Appointment appointment);
    void sendAppointmentCancelled(Appointment appointment);
    void sendAppointmentRescheduled(Appointment oldAppointment, Appointment newAppointment);
}
