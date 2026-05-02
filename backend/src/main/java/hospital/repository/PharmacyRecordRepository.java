package hospital.repository;

import hospital.entity.PharmacyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PharmacyRecordRepository extends JpaRepository<PharmacyRecord, Long> {
    List<PharmacyRecord> findByPrescriptionId(Long prescriptionId);
    List<PharmacyRecord> findByAppointmentId(Long appointmentId);
}
