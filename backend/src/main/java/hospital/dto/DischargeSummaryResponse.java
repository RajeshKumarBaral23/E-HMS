package hospital.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class DischargeSummaryResponse {
    private Long id;
    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private String summary;
    private String instructions;
    private LocalDate dischargeDate;
    private LocalDate followUpDate;
    private LocalDateTime createdAt;
}
