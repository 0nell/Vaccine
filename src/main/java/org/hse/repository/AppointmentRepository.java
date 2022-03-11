package org.hse.repository;

import org.hse.model.Appointment;
import org.hse.model.Centre;
import org.hse.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByAppointmentDateTimeAndCentre(String date, Centre centre);
}

