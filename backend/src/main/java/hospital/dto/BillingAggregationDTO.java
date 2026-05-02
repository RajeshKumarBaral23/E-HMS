package hospital.dto;

import lombok.Data;

@Data
public class BillingAggregationDTO {
    private Double consultationFee;
    private Double medicineCost;
    private Double labTestCost;
    private Double roomCharges;
    private Double totalAmount;
    private String status;
}
