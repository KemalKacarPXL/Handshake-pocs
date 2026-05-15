package be.pxl.researchproject.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class StudentRequest {

    @NotBlank(message = "Voornaam mag niet leeg zijn")
    private String firstname;

    @NotBlank(message = "Achternaam mag niet leeg zijn")
    private String lastname;

    @NotBlank(message = "Opleiding mag niet leeg zijn")
    private String education;

    @NotBlank(message = "Email mag niet leeg zijn")
    @Email(message = "Ongeldig emailadres")
    private String email;

    @NotBlank(message = "Wachtwoord mag niet leeg zijn")
    @Size(min = 8, message = "Wachtwoord moet minstens 8 tekens bevatten")
    private String password;

    public StudentRequest(String firstname, String lastname, String email, String password, String education) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.education = education;
    }

    public StudentRequest() {}

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

    public String getEducation() {return education;}

    public void setEducation(String education) {this.education = education;}
}