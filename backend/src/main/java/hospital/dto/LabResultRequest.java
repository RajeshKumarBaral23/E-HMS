package hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabResultRequest {
    @NotNull
    private Long patientId;

    // optional: if omitted, current authenticated doctor will be set
    private Long doctorId;

    private Long appointmentId;

    @NotNull
    private String testType;

    @NotNull
    private String result;
}
