package hospital.service;

import hospital.dto.MedicalRecordRequest;
import hospital.entity.MedicalRecord;

import java.util.List;

public interface MedicalRecordService {
    MedicalRecord create(MedicalRecordRequest req);
    List<MedicalRecord> listForPatient(Long patientId);
    List<MedicalRecord> listForDoctor(Long doctorId);
    MedicalRecord getById(Long id);
    List<MedicalRecord> listAll();
}
