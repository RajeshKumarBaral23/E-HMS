package hospital.repository;

import hospital.entity.Prescription;
import hospital.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByDoctor(User doctor);
    List<Prescription> findByPatient(User patient);
    List<Prescription> findByAppointmentId(Long appointmentId);

    @Query("SELECT p FROM Prescription p WHERE p.patient.id = :patientId")
    List<Prescription> findByPatientId(@Param("patientId") Long patientId);
}
