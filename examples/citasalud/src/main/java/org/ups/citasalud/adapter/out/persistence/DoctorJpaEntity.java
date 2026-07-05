package org.ups.citasalud.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "doctor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String specialty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private LocationJpaEntity location;
}
