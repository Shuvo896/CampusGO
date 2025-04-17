package com.university.utms.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleRequest {
    private Long busId;
    private Long routeId;
    private String departureTime;  // e.g., "10:30"
    private String arrivalTime;    // e.g., "11:15"
}
