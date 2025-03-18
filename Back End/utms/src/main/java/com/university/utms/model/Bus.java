package com.university.utms.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "buses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String numberPlate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusType type;

    @Column(nullable = false)
    private int totalSeats;

    @Column(nullable = false)
    private int standingCapacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status currentStatus;

    public enum BusType {
        STUDENT, FACULTY, MIXED
    }

    public enum Status {
        AVAILABLE, IN_TRANSIT, NOT_AVAILABLE
    }
}