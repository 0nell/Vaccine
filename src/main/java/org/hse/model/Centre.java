package org.hse.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "centres")
public class Centre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String name;

    @Column
    private String address;

    @Column
    private String number;

    @OneToMany(mappedBy = "centre", cascade = CascadeType.ALL)  //remove centre means appointment removed
    private List<Appointment> appointments = new ArrayList<>();

    public Centre() { }

    public Centre(String name) {
        this.name = name;
    }
    public Centre(String name, String address) {
        this.name = name;
        this.address = address;
    }
    public Centre(String name, String address, String number) {
        this.name = name;
        this.address = address;
        this.number = number;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    public List<String> getBookedDates(){
        List<String> dates = new ArrayList<>();
        for(Appointment app: appointments){
            dates.add(app.getAppointmentDateTime());
        }
        return dates;
    }
}
