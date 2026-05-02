package hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentRescheduleRequest {
    @NotBlank
    private String newAppointmentTime; // ISO local date-time

    private Integer durationMinutes; // optional
}
