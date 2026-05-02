package hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import hospital.entity.PaymentStatus;

@Entity
@Table(name = "billings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Billing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long appointmentId;
    private Long patientId;
    private Long admissionId;

    private Double consultationFee;
    private Double medicineCost;
    private Double labCharges;
    private Double roomCharges;
    private Double totalAmount;

    private String status; // PAID / UNPAID

    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
}
