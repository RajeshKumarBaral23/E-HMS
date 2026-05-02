package hospital.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "discharge_summaries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DischargeSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private User patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private User doctor;

    @Lob
    private String summary;

    @Lob
    private String instructions;

    private LocalDateTime dischargeDate;

    private LocalDate followUpDate;

    private LocalDateTime createdAt;
}
