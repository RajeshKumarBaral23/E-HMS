package hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DischargeSummaryRequest {
    @NotNull
    private Long appointmentId;

    @NotBlank
    private String summary;

    private String instructions;
    private String followUpDate;
}
