package hospital.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AvailabilitySlotRequest {
    private Long doctorId; // Optional - uses current authenticated user if not provided

    @NotNull
    private String startDateTime; // ISO local date-time

    @NotNull
    private String endDateTime; // ISO local date-time

    private Boolean active;
}
