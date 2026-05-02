package hospital.service;

import hospital.dto.AdmissionDTO;
import java.util.List;

public interface AdmissionService {
    AdmissionDTO admitPatient(AdmissionDTO dto);
    AdmissionDTO dischargePatient(Long admissionId, java.time.LocalDate dischargeDate);
    List<AdmissionDTO> getAdmissionsByPatient(Long patientId);
}

