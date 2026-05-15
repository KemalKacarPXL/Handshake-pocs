package be.pxl.researchproject.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

@Entity
public class Scan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentFirstname;
    private String studentLastname;
    private String studentEmail;
    private String studentEducation;
    private LocalDateTime scannedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bedrijf_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Bedrijf bedrijf;

    public Scan() {}

    public Long getId() { return id; }
    public String getStudentFirstname() { return studentFirstname; }
    public void setStudentFirstname(String studentFirstname) { this.studentFirstname = studentFirstname; }
    public String getStudentLastname() { return studentLastname; }
    public void setStudentLastname(String studentLastname) { this.studentLastname = studentLastname; }
    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
    public String getStudentEducation() { return studentEducation; }
    public void setStudentEducation(String studentEducation) { this.studentEducation = studentEducation; }
    public LocalDateTime getScannedAt() { return scannedAt; }
    public void setScannedAt(LocalDateTime scannedAt) { this.scannedAt = scannedAt; }
    public Bedrijf getBedrijf() { return bedrijf; }
    public void setBedrijf(Bedrijf bedrijf) { this.bedrijf = bedrijf; }
}