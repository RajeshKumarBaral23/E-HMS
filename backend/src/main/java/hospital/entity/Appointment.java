package hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private User doctor;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private User patient;

    private LocalDateTime appointmentTime;

    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    private String reason;

    private LocalDateTime checkInTime;

    private LocalDateTime consultationStartTime;

    private LocalDateTime consultationEndTime;

    private LocalDate followUpDate;

    private Integer queueNumber;
}
