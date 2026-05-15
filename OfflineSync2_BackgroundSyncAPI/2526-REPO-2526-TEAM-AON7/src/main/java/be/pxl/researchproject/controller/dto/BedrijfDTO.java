package be.pxl.researchproject.controller.dto;

import be.pxl.researchproject.model.Bedrijf;

public class BedrijfDTO {
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String companyName;
    private String description;
    private String sector;
    private String website;
    private String phoneNumber;
    private String location;

    public BedrijfDTO(Bedrijf bedrijf) {
        this.id = bedrijf.getId();
        this.firstname = bedrijf.getFirstname();
        this.lastname = bedrijf.getLastname();
        this.email = bedrijf.getEmail();
        this.companyName = bedrijf.getCompanyName();
        this.description = bedrijf.getDescription();
        this.sector = bedrijf.getSector();
        this.website = bedrijf.getWebsite();
        this.phoneNumber = bedrijf.getPhoneNumber();
        this.location = bedrijf.getLocation();
    }

    public Long getId() { return id; }
    public String getFirstname() { return firstname; }
    public String getLastname() { return lastname; }
    public String getEmail() { return email; }
    public String getCompanyName() { return companyName; }
    public String getDescription() { return description; }
    public String getSector() { return sector; }
    public String getWebsite() { return website; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getLocation() { return location; }
}