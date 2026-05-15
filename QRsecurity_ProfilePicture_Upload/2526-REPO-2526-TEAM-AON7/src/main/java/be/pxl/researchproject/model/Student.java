package be.pxl.researchproject.model;

import jakarta.persistence.*;

@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstname;
    private String lastname;
    private String education;

    @Column(unique = true, nullable = false)
    private String email;
    private String password;

    private String cvFileName;
    private String cvContentType;
    private Long cvFileSize;

    private String profilePictureFileName;
    private String profilePictureContentType;

    @Basic(fetch = FetchType.LAZY)
    private byte[] profilePictureContent;

    @Basic(fetch = FetchType.LAZY)
    private byte[] cvFileContent;

    @ManyToOne
    private HandshakeEvent joinedEvent;

    public Student() {}
    public HandshakeEvent getJoinedEvent() {
        return joinedEvent;
    }

    public void setJoinedEvent(HandshakeEvent event) {
        this.joinedEvent = event;
    }



    public Student(String firstname, String lastname, String email, String password, String education) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.education = education;
    }
    public String getProfilePictureFileName() { return profilePictureFileName; }
    public void setProfilePictureFileName(String profilePictureFileName) { this.profilePictureFileName = profilePictureFileName; } 
    public String getProfilePictureContentType() { return profilePictureContentType; }
    public void setProfilePictureContentType(String profilePictureContentType) { this.profilePictureContentType = profilePictureContentType; }

    public byte[] getProfilePictureContent() {
        return profilePictureContent;
    }

    public void setProfilePictureContent(byte[] profilePictureContent) {
        this.profilePictureContent = profilePictureContent;
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

    public String getCvFileName() {
        return cvFileName;
    }

    public void setCvFileName(String cvFileName) {
        this.cvFileName = cvFileName;
    }

    public String getCvContentType() {
        return cvContentType;
    }

    public void setCvContentType(String cvContentType) {
        this.cvContentType = cvContentType;
    }

    public Long getCvFileSize() {
        return cvFileSize;
    }

    public void setCvFileSize(Long cvFileSize) {
        this.cvFileSize = cvFileSize;
    }

    public byte[] getCvFileContent() {
        return cvFileContent;
    }

    public void setCvFileContent(byte[] cvFileContent) {
        this.cvFileContent = cvFileContent;
    }

    public String getEducation() {return education;}

    public void setEducation(String education) {this.education = education;}

    public Long getJoinedEventId() {
        return joinedEvent != null ? joinedEvent.getId() : null;
    }

    public boolean hasJoinedEvent() {
        return joinedEvent != null;
    }
}
