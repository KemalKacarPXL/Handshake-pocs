package be.pxl.researchproject.model;

import jakarta.persistence.*;

@Entity
public class Bedrijf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstname;
    private String lastname;

    @Column(unique = true, nullable = false)
    private String email;
    private String password;
    private String companyName;
    private String description;
    private String sector;
    private String website;
    private String phoneNumber;
    private String location;

    @ManyToOne
    private HandshakeEvent joinedEvent;

    public Bedrijf() {}
    public HandshakeEvent getJoinedEvent() {
        return joinedEvent;
    }

    public void setJoinedEvent(HandshakeEvent event) {
        this.joinedEvent = event;
    }

    public Long getJoinedEventId() {
        return joinedEvent != null ? joinedEvent.getId() : null;
    }

    public boolean hasJoinedEvent() {
        return joinedEvent != null;
    }

    public Bedrijf(String firstname, String lastname, String email, String password) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
    }

    public Bedrijf(String firstname, String lastname, String email, String password,
                   String description, String sector, String website, String phoneNumber, String location) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.description = description;
        this.sector = sector;
        this.website = website;
        this.phoneNumber = phoneNumber;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastname() {
        return lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}