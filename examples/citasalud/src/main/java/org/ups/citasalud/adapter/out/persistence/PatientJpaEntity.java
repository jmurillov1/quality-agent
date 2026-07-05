package org.ups.citasalud.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 20, unique = true)
    private String phone;

    @Column(length = 255, unique = true)
    private String email;

    @Column(name = "registration_date", nullable = false)
    private Instant registrationDate;
}
