package hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MedicalRecordRequest {
    @NotNull
    private Long patientId;

    @NotNull
    private Long doctorId;

    @NotBlank
    private String diagnosis;

    private String treatment;

    private String notes;

    @NotBlank
    private String visitDate; // ISO datetime string

    private Long appointmentId; // optional link
}
