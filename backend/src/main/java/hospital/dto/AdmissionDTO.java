package hospital.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AdmissionDTO {
    private Long id;
    private Long patientId;
    private Long bedId;
    private LocalDate admissionDate;
    private LocalDate dischargeDate;
    private Double roomChargePerDay;
}

