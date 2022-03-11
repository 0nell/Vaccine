package org.hse.model;

import javax.persistence.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "user_table")
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String ppsn;

    @Column
    private String firstName;

    @Column
    private String surname;

    @Column
    private String dob;

    @Column
    private String address;

    @Column
    private String phoneNumber;

    @Column
    private String email;

    @Column
    private String nationality;

    @Column
    private UserType userType;

    @Column
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Appointment> appointments = new ArrayList<>();

    public User(){
        super();
    }

    /*public User(@NotBlank String firstName, @NotBlank String surname, @NotBlank Date DOB,
                @NotBlank String ppsn, @NotBlank String address, @NotBlank String phoneNumber, @NotBlank String email,
                @NotBlank String nationality)*/

    public User(UserType userType) {
        this.userType = userType;
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(String firstName,String surname,String dob, String ppsn, String address,String phoneNumber,String email, String nationality, String password, UserType userType){
        this.firstName = firstName;
        this.surname = surname;
        this.dob = dob;
        this.ppsn = ppsn;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.nationality = nationality;
        this.password = password;
        this.userType = userType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public boolean vaccinated(){
        return appointments.size() == 2 & appointments.get(0).isReceived() && appointments.get(1).isReceived();
    }

    public boolean firstDose(){
        if(!firstBooked())
            return false;
        else
            return appointments.get(0).isReceived();
    }

    public boolean firstBooked(){
        return !appointments.isEmpty();
    }

    public boolean secondBooked(){
        return appointments.size() == 2 & appointments.get(0).isReceived() && !appointments.get(1).isReceived();
    }

    public boolean canBook(){
        return (!firstBooked() || (firstBooked() && firstDose() && !secondBooked() && !vaccinated()));
    }
}