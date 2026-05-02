package hospital.service;

import hospital.dto.DischargeSummaryDTO;

public interface DischargeService {
    DischargeSummaryDTO generateSummary(Long patientId, Long admissionId);
}

