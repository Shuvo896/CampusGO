package com.university.utms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
    private BusType type;  // STUDENT, FACULTY, MIXED

    @Column(nullable = false)
    private int totalSeats;  // total seats count

    @Column(nullable = false)
    private int standingCapacity;

    @Column(nullable = false)
    private int availableSeats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status currentStatus;  // AVAILABLE, IN_TRANSIT, NOT_AVAILABLE

    @Column(length = 100)
    private String driverName;

    @OneToOne
    @JoinColumn(name = "route_id")
    private Route route;
    
    @Column(length = 255)
    private String qrCode;  // can be updated when needed

    @OneToMany(mappedBy = "bus", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // prevent infinite recursion
    private List<Schedule> schedules = new ArrayList<>();


    public enum BusType {
        student, faculty, mixed
    }

    public enum Status {
        available, in_transit, not_available
    }
}
