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

    @NotBlank       //user can have many appts
    @ManyToOne
    private User user;

    @NotBlank       //centre can have many apts
    @ManyToOne
    private Centre centre;

    public Appointment() {
        super();
    }

    public Appointment(@NotBlank Date appointmentDateTime, @NotBlank boolean firstDose, @NotBlank User user, @NotBlank VaccineType vaccineType,@NotBlank Centre centre) {
        this.appointmentDateTime = appointmentDateTime;
        this.firstDose = firstDose;
        this.user = user;
        this.vaccineType = vaccineType;
        this.received = false;              //obviously hasnt recieved dose if appointment just being made
        this.centre = centre;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public Centre getCentre() {
        return centre;
    }

    public void setCentre(Centre centre) {
        this.centre = centre;
    }
}
