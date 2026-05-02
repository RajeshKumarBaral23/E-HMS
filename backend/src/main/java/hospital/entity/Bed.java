package hospital.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Bed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String bedNumber;
    private String ward;
    private boolean occupied;
}

