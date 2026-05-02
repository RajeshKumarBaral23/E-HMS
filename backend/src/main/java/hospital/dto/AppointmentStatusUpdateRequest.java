package hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStatusUpdateRequest {
    @NotBlank
    private String status;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "followUpDate must be yyyy-MM-dd")
    private String followUpDate; // ISO date yyyy-MM-dd, optional
}
