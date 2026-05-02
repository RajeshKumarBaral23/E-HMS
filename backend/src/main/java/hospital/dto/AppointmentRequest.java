package hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AppointmentRequest {
    @NotNull
    private Long doctorId;

    @NotBlank
    private String appointmentTime; // ISO-8601 string

    @NotNull
    @Positive
    private Integer durationMinutes;
    private String reason;
}
