package org.hse.model;

import javax.validation.constraints.NotBlank;
import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    @NotBlank
    private String appointmentDateTime;

    @Column
    private boolean received;

    @Column
    private boolean firstDose;

    @Column
    private VaccineType vaccineType;

          //user can have many appts
    @ManyToOne
    private User user;

           //centre can have many apts
    @ManyToOne
    private Centre centre;

    public Appointment() {
        super();
    }

    public Appointment(@NotBlank String appointmentDateTime, boolean firstDose, User user, Centre centre) {
        this.appointmentDateTime = appointmentDateTime;
        this.firstDose = firstDose;
        this.user = user;
        this.received = false;              //obviously hasnt recieved dose if appointment just being made
        this.centre = centre;
    }

    public Appointment(@NotBlank String appointmentDateTime, boolean firstDose, User user, VaccineType vaccineType, Centre centre) {
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

    public String getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(String appointmentDateTime) {
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

    public void setVaccineType(String type){
        if(type.equals("Pfizer"))
            setVaccineType(VaccineType.PFIZER);
        else if(type.equals("Moderna"))
            setVaccineType(VaccineType.MODERNA);
        else
            System.out.println("Something went wrong");
    }
}
