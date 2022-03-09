package org.hse.model;

import javax.validation.constraints.NotBlank;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "user_table")
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @NotBlank
    private String ppsn;

    @Column
    @NotBlank
    private String firstName;

    @Column
    @NotBlank
    private String surname;

    @Column
    @NotBlank
    private Date DOB;

    @Column
    @NotBlank
    private String address;

    @Column
    @NotBlank
    private String phoneNumber;

    @Column
    @NotBlank
    private String email;

    @Column
    @NotBlank
    private String nationality;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Appointment> appointments = new ArrayList<>();

    public User(){
        super();
    }

    public User(@NotBlank String firstName, @NotBlank String surname, @NotBlank Date DOB,
                @NotBlank String ppsn, @NotBlank String address, @NotBlank String phoneNumber, @NotBlank String email,
                @NotBlank String nationality)
    {
        this.firstName = firstName;
        this.surname = surname;
        this.DOB = DOB;
        this.ppsn = ppsn;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.nationality = nationality;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPpsn() {
        return ppsn;
    }

    public void setPpsn(String ppsn) {
        this.ppsn = ppsn;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Date getDOB() {
        return DOB;
    }

    public void setDOB(Date DOB) {
        this.DOB = DOB;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }
}