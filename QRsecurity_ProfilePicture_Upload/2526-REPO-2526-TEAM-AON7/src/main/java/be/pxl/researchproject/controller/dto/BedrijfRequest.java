package be.pxl.researchproject.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BedrijfRequest {

    @NotBlank(message = "Voornaam mag niet leeg zijn")
    private String firstname;

    @NotBlank(message = "Achternaam mag niet leeg zijn")
    private String lastname;

    @NotBlank(message = "Email mag niet leeg zijn")
    @Email(message = "Ongeldig emailadres")
    private String email;

    @NotBlank(message = "Wachtwoord mag niet leeg zijn")
    @Size(min = 8, message = "Wachtwoord moet minstens 8 tekens bevatten")
    private String password;

    private String companyName;

    public BedrijfRequest() {
    }

    public BedrijfRequest(String firstname, String lastname, String email, String password) {
        this(firstname, lastname, email, password, null);
    }

    public BedrijfRequest(String firstname, String lastname, String email, String password, String companyName) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.companyName = companyName;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}