package com.university.utms.repository;

import com.university.utms.model.Booking;
import com.university.utms.model.Schedule;
import com.university.utms.model.Bus;
import com.university.utms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByUser(User user);
    List<Booking> findAllBySchedule(Schedule schedule);
    long countByScheduleIdAndStandingTrue(Long scheduleId);


}