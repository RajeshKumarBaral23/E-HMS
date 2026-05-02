package hospital.service;

import hospital.dto.LabResultRequest;
import hospital.entity.LabResult;

import java.util.List;

public interface LabResultService {
    LabResult create(LabResultRequest request);
    List<LabResult> getByPatientId(Long patientId);
    List<LabResult> getAll();
    LabResult getById(Long id);
}
