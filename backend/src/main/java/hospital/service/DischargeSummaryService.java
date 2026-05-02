package hospital.service;

import hospital.dto.DischargeSummaryRequest;
import hospital.entity.DischargeSummary;

import java.util.List;

public interface DischargeSummaryService {
    DischargeSummary createSummary(DischargeSummaryRequest request);
    List<DischargeSummary> listForCurrentUser(Long appointmentId);
    DischargeSummary getById(Long id);
}
