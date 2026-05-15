package be.pxl.researchproject.controller.dto;

public class BedrijfProfileUpdateRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String description;
    private String sector;
    private String website;
    private String phoneNumber;
    private String location;
    private String oldEmail;

    public BedrijfProfileUpdateRequest() {}

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }
    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getOldEmail() { return oldEmail; }
    public void setOldEmail(String oldEmail) { this.oldEmail = oldEmail; }
}