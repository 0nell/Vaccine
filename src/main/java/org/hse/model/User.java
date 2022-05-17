package org.hse.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.concurrent.TimeUnit;
import java.util.*;
import java.text.SimpleDateFormat;

@Entity
@Table(name = "users")
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
    private String username;

    @Column
    private String nationality;

    @Column
    private String authority;

    @Column
    private String male;


    private int enabled;

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

    public User(String authority) {
        this.authority = authority;
        this.enabled = 1;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.enabled = 1;
    }

    public User(String firstName,String surname,String dob, String ppsn, String address,String phoneNumber,String username, String nationality, String password, String authority, String male){
        this.firstName = firstName;
        this.surname = surname;
        this.dob = dob;
        this.ppsn = ppsn;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.username = username;
        this.nationality = nationality;
        this.password = password;
        this.enabled = 1;
        this.authority = authority;
        this.male = male;
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
    public int getEnabled() {
        return enabled;
    }

    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getAuthority() {
        return  authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public boolean vaccinated(){
        if(appointments.size() == 2){
            return appointments.get(0).isReceived() && appointments.get(1).isReceived();
        }
        return false;
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
    public String getMale() {
        return male;
    }

    public int getAge() {
        Date d = new Date();
        try {
            d = new SimpleDateFormat("yyyy-MM-dd").parse(this.dob);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Date today = Calendar.getInstance().getTime();
        long d1 = TimeUnit.DAYS.convert(today.getTime(), TimeUnit.MILLISECONDS);
        long d2 = TimeUnit.DAYS.convert(d.getTime(), TimeUnit.MILLISECONDS);
        long diff = d1 - d2;
        return (int)diff/360;
    }

    public boolean isMale()
    {
        return male.equals("true");
    }

    public void setMale(String male) {
        this.male = male;
    }

    public boolean isEnabled() {
        return enabled == 1;
    }
}