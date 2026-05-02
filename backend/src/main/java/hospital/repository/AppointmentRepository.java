package hospital.repository;

import hospital.entity.Appointment;
import hospital.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctor(User doctor);
    List<Appointment> findByPatient(User patient);
    List<Appointment> findByDoctorAndAppointmentTimeBetween(User doctor, LocalDateTime start, LocalDateTime end);

    @org.springframework.data.jpa.repository.Query(value = "SELECT DATE(appointment_time) as day, COUNT(*) as cnt FROM appointments WHERE appointment_time >= :start GROUP BY DATE(appointment_time) ORDER BY day", nativeQuery = true)
    java.util.List<Object[]> countAppointmentsSince(@org.springframework.data.repository.query.Param("start") LocalDateTime start);
}
