package org.hse.model;

public class UserDto{

    private long id;
    private String ppsn;
    private String firstName;
    private String surname;
    private String dob;
    private String address;
    private String phoneNumber;
    private String email;
    private String nationality;
    private String authority;
    private String male;
    private String password;

    public UserDto(){
        super();
    }

    public UserDto(String authority) {
        this.authority = authority;
    }

    public UserDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public UserDto(String firstName,String surname,String dob, String ppsn, String address,String phoneNumber,String email, String nationality, String password, String authority, String male){
        this.firstName = firstName;
        this.surname = surname;
        this.dob = dob;
        this.ppsn = ppsn;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.nationality = nationality;
        this.password = password;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUsername() {
        return email;
    }

    public void setUsername(String username) {
        this.email = username;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) { this.password = password;}

    public String getAuthority() {
        return  authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getMale() {
        return male;
    }

    public void setMale(String male) {
        this.male = male;
    }
}
