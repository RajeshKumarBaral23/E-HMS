package hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrescriptionRequest {
    @NotNull
    private Long appointmentId;

    @NotBlank
    private String medications;
    private String notes;
}
