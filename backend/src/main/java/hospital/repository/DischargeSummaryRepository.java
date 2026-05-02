package hospital.repository;

import hospital.entity.DischargeSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DischargeSummaryRepository extends JpaRepository<DischargeSummary, Long> {
    List<DischargeSummary> findByAppointment_Id(Long appointmentId);
    List<DischargeSummary> findByPatient_Id(Long patientId);
    List<DischargeSummary> findByDoctor_Id(Long doctorId);
}
