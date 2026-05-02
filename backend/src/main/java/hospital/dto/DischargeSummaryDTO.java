package hospital.dto;

import lombok.Data;
import java.util.List;

@Data
public class DischargeSummaryDTO {
    private String patientName;
    private String diagnosis;
    private List<String> medicines;
    private List<String> labTests;
    private Double totalBill;
    private String dischargeDate;
}

