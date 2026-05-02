package hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabResultResponse {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private Long appointmentId;
    private String testType;
    private String result;
    private LocalDateTime createdAt;
}
