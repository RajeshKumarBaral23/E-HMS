package hospital.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    // optional on update
    private String password;

    private String specialization;
    private String phone;
    private String bio;
    @Positive(message = "departmentId must be a positive id")
    private Long departmentId;
    private String availabilityStartDateTime; // ISO local date-time, optional initial schedule start
    private String availabilityEndDateTime;   // ISO local date-time, optional initial schedule end
}
