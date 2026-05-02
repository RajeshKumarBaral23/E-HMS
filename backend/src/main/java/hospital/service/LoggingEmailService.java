package hospital.service;

import hospital.entity.Appointment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LoggingEmailService implements EmailService {

    @Override
    @Async
    public void sendAppointmentCreated(Appointment appointment) {
        // Non-blocking: log the notification; real mailer can replace this bean later
        log.info("[Email] appointment created: id={} patient={} doctor={} time={}",
                appointment.getId(),
                appointment.getPatient() != null ? appointment.getPatient().getEmail() : null,
                appointment.getDoctor() != null ? appointment.getDoctor().getEmail() : null,
                appointment.getAppointmentTime());
    }

    @Override
    @Async
    public void sendAppointmentCancelled(Appointment appointment) {
        log.info("[Email] appointment cancelled: id={} patient={} doctor={} time={}",
                appointment.getId(),
                appointment.getPatient() != null ? appointment.getPatient().getEmail() : null,
                appointment.getDoctor() != null ? appointment.getDoctor().getEmail() : null,
                appointment.getAppointmentTime());
    }

    @Override
    @Async
    public void sendAppointmentRescheduled(Appointment oldAppointment, Appointment newAppointment) {
        log.info("[Email] appointment rescheduled: oldId={} newId={} patient={} doctor={} oldTime={} newTime={}",
                oldAppointment != null ? oldAppointment.getId() : null,
                newAppointment != null ? newAppointment.getId() : null,
                newAppointment != null && newAppointment.getPatient() != null ? newAppointment.getPatient().getEmail() : null,
                newAppointment != null && newAppointment.getDoctor() != null ? newAppointment.getDoctor().getEmail() : null,
                oldAppointment != null ? oldAppointment.getAppointmentTime() : null,
                newAppointment != null ? newAppointment.getAppointmentTime() : null);
    }
}
