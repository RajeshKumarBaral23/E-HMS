package hospital.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class FileAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileUrl;
    private String fileType;
    private String relatedType; // MEDICAL_RECORD / LAB / PRESCRIPTION
    private Long relatedId;
    private LocalDateTime uploadedAt;
}
