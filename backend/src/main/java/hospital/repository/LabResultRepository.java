package hospital.repository;

import hospital.entity.LabResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LabResultRepository extends JpaRepository<LabResult, Long> {
    List<LabResult> findByPatient_Id(Long patientId);
    List<LabResult> findByDoctor_Id(Long doctorId);

    @Query("SELECT COUNT(l) FROM LabResult l WHERE l.patient.id = :patientId")
    Long countByPatientId(@Param("patientId") Long patientId);
}
