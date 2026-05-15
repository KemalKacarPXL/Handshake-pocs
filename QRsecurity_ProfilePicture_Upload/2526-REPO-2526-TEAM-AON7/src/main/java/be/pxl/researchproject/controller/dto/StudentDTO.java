package be.pxl.researchproject.controller.dto;

import be.pxl.researchproject.model.Student;

public class StudentDTO {
    private Long id;
    private String firstname;
    private String lastname;
    private String education;
    private String email;
    private String password;

    public StudentDTO(Student student) {
        this.id = student.getId();
        this.firstname = student.getFirstname();
        this.lastname = student.getLastname();
        this.education = student.getEducation();
        this.email = student.getEmail();
        this.password = student.getPassword();
    }

    public String getFirstname() {
        return firstname;
    }

    public Long getId() {
        return id;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getEducation() {return education;}
}
