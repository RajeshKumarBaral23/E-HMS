package hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pharmacy_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long prescriptionId;

    private Long patientId;

    private Long appointmentId;

    private Long medicineId;

    private Integer quantityDispensed;

    private LocalDateTime dispensedAt;
}
