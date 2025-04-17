package com.university.utms.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.List;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bus_id", nullable = false)
    @JsonIgnoreProperties({"schedules"}) // to avoid loop
    private Bus bus;

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    @JsonIgnoreProperties({"schedules"}) // if Route has schedules
    private Route route;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Booking> bookings;


    @Column(nullable = false)
    private LocalTime departureTime;

    @Column(nullable = false)
    private LocalTime arrivalTime;


    public Schedule(Bus bus, Route route, LocalTime departure, LocalTime arrival) {
        this.bus = bus;
        this.route = route;
        this.departureTime = departure;
        this.arrivalTime = arrival;
    }

}
