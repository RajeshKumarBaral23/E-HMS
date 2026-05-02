package hospital.repository;

import hospital.entity.Billing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BillingRepository extends JpaRepository<Billing, Long> {
    List<Billing> findByAppointmentId(Long appointmentId);
    List<Billing> findByAppointmentIdInOrderByCreatedAtDesc(List<Long> appointmentIds);
    List<Billing> findByPatientId(Long patientId);
    Optional<Billing> findByAppointmentIdAndPatientId(Long appointmentId, Long patientId);

    @Query("SELECT SUM(b.totalAmount) FROM Billing b WHERE b.status = 'PAID'")
    Double sumTotalPaid();
}
