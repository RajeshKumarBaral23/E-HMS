package hospital.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrescriptionMedicineRequest {
    @NotNull
    private Long medicineId;
    private String dosage;
    private Integer durationDays;
    private String instructions;
}
