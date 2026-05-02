package hospital.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MedicalRecordResponse {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private String diagnosis;
    private String treatment;
    private String notes;
    private LocalDateTime visitDate;
    private LocalDateTime createdAt;
}
