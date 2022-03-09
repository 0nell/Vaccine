package org.hse.model;

import javax.validation.constraints.NotBlank;
import javax.persistence.*;
import org.hse.model.User;
import org.hse.model.VaccineType;
import java.util.Date;

@Entity
@Table(name = "appointment_table")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    //@ManyToOne                //if we have a centres table /////not needed imo
    @NotBlank
    private int centreNum;

    @Column
    @NotBlank
    private Date appointmentDateTime;

    @Column
    @NotBlank
    private boolean received;

    @Column
    @NotBlank
    private boolean firstDose;

    @Column
    @NotBlank
    private VaccineType vaccineType;

    @NotBlank
    @ManyToOne
    private User user;

    public Appointment() {
        super();
    }

    public Appointment(@NotBlank int centreNum, @NotBlank Date appointmentDateTime, @NotBlank boolean firstDose, @NotBlank User user, @NotBlank VaccineType vaccineType) {
        this.centreNum = centreNum;
        this.appointmentDateTime = appointmentDateTime;
        this.firstDose = firstDose;
        this.user = user;
        this.vaccineType = vaccineType;
        this.received = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCentreNum() {
        return centreNum;
    }

    public void setCentreNum(int centreNum) {
        this.centreNum = centreNum;
    }

    public Date getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(Date appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

    public boolean isFirstDose() {
        return firstDose;
    }

    public void setFirstDose(boolean firstDose) {
        this.firstDose = firstDose;
    }

    public VaccineType getVaccineType() {
        return vaccineType;
    }

    public void setVaccineType(VaccineType vaccineType) {
        this.vaccineType = vaccineType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
