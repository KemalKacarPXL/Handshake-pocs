package be.pxl.researchproject.integration;

import be.pxl.researchproject.controller.dto.*;
import be.pxl.researchproject.exception.BadRequestException;
import be.pxl.researchproject.model.Bedrijf;
import be.pxl.researchproject.model.HandshakeEvent;
import be.pxl.researchproject.model.Scan;
import be.pxl.researchproject.model.Student;
import be.pxl.researchproject.repository.BedrijfRepository;
import be.pxl.researchproject.repository.HandshakeEventRepository;
import be.pxl.researchproject.repository.ScanRepository;
import be.pxl.researchproject.repository.StudentRepository;
import be.pxl.researchproject.service.StudentService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@SuppressWarnings("PMD.TooManyMethods") //anders foutcode PMD teveel functies in 1 klasse
public class StudentServiceIntegrationTest {

    @Autowired
    StudentService studentService;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    ScanRepository scanRepository;

    @Autowired
    HandshakeEventRepository handshakeEventRepository;

    @Autowired
    BedrijfRepository bedrijfRepository;

    private Student savedStudent(){
        Student kemal = new Student();
        kemal.setFirstname("Kemal");
        kemal.setLastname("Kacar");
        kemal.setEmail("kemal@student.pxl.be");
        kemal.setPassword("password");
        kemal.setEducation("Professionele Bachelor Toegepaste Informatica");
        return studentRepository.save(kemal); // entityManager.persistAndFlush
                                                //maak TestEntityManager entityManager
    }

    private HandshakeEvent savedHandshakeEvent(){
        HandshakeEvent trixxo = new HandshakeEvent(
                "Trixxo", LocalDate.of(2026,5,1),
                LocalTime.of(16,30),LocalTime.of(19,30),
                "Hasselt"
        );
        return handshakeEventRepository.save(trixxo);
    }

    @Test
    void createStudent_shouldSaveToDatabase() {
        StudentRequest request = new StudentRequest("Kemal", "Kacar",
                "kemal@student.pxl.be", "password123",
                "Professionele Bachelor Toegepaste Informatica");
        studentService.createStudent(request);

        assertThat(studentRepository.existsByEmail("kemal@student.pxl.be")).isTrue();
    }

    @Test
    void createStudent_shouldThrow_whenEmailDuplicate() {
        savedStudent();

        StudentRequest request = new StudentRequest("Salih", "Kacar",
                "kemal@student.pxl.be", "password123",
                "Professionele Bachelor Toegepaste Informatica");

        assertThatThrownBy(() -> studentService.createStudent(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email is al in gebruik");
    }

    @Test
    void createStudent_EmailSavedAsLowercase() {
        StudentRequest request = new StudentRequest("Kemal", "Kacar",
                "KEMAL@student.pxl.be", "password123",
                "Professionele Bachelor Toegepaste Informatica");

        studentService.createStudent(request);

        assertThat(studentRepository.existsByEmail("kemal@student.pxl.be")).isTrue();
        assertThat(studentRepository.existsByEmail("KEMAL@student.pxl.be")).isFalse();
    }

    @Test
    void getStudentProfileByEmail_Success() {
        savedStudent();

        StudentProfileResponse result = studentService.getStudentProfileByEmail("kemal@student.pxl.be");

        assertThat(result.firstname()).isEqualTo("Kemal");
        assertThat(result.email()).isEqualTo("kemal@student.pxl.be");
        assertThat(result.hasCv()).isFalse();
    }

    @Test
    void getStudentProfileByEmail_EmailNormalization() {
        savedStudent();

        StudentProfileResponse result = studentService.getStudentProfileByEmail("  KEMAL@student.pxl.be  ");

        assertThat(result.firstname()).isEqualTo("Kemal");
    }

    @Test
    void getStudentProfileByEmail_StudentNotFound() {
        assertThatThrownBy(() ->
                studentService.getStudentProfileByEmail("onbekend@student.pxl.be")
        ).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Student niet gevonden");
    }

    @Test
    void joinEvent_Success() {
        HandshakeEvent event = savedHandshakeEvent();
        savedStudent();

        JoinEventResponse response = studentService.joinEvent(
                "kemal@student.pxl.be",
                new JoinEventRequest(event.getId())
        );

        assertThat(response.success()).isTrue();
        assertThat(response.message()).isEqualTo("Je bent succesvol gejoined!");
    }

    @Test
    void joinEvent_AlreadyJoined() {
        HandshakeEvent event = savedHandshakeEvent();
        Student student = savedStudent();
        student.setJoinedEvent(event);
        studentRepository.save(student);

        JoinEventResponse response = studentService.joinEvent(
                "kemal@student.pxl.be",
                new JoinEventRequest(event.getId())
        );

        assertThat(response.success()).isFalse();
    }

    @Test
    void joinEvent_StudentNotFound() {
        HandshakeEvent event = savedHandshakeEvent();

        assertThatThrownBy(() ->
                studentService.joinEvent("onbekend@pxl.be", new JoinEventRequest(event.getId()))
        ).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Student niet gevonden");
    }
    @Test
    void getStudentsByEvent_WithCv() {
        HandshakeEvent event = savedHandshakeEvent();

        Student student = new Student();
        student.setFirstname("Kemal");
        student.setLastname("Kacar");
        student.setEmail("kemal@student.pxl.be");
        student.setPassword("password");
        student.setJoinedEvent(event);
        student.setCvFileName("cv.pdf");
        student.setCvContentType("application/pdf");
        student.setCvFileSize(100L);
        student.setCvFileContent(new byte[]{1, 2, 3});

        studentRepository.save(student);

        List<StudentProfileResponse> result = studentService.getStudentsByEvent(event.getId());

        assertThat(result.getFirst().hasCv()).isTrue();
        assertThat(result.getFirst().cvFileName()).isEqualTo("cv.pdf");
    }

    @Test
    void getStudentsByEvent_NonExistentEvent() {
        List<StudentProfileResponse> result = studentService.getStudentsByEvent(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void getStudentsByEvent_OnlyReturnsStudentsFromThatEvent() {
        HandshakeEvent event1 = savedHandshakeEvent();
        HandshakeEvent event2 = new HandshakeEvent(
                "Ander Event", LocalDate.of(2026, 6, 1),
                LocalTime.of(10, 0), LocalTime.of(12, 0), "Brussel"
        );
        handshakeEventRepository.save(event2);

        Student studentEvent1 = new Student();
        studentEvent1.setFirstname("Kemal");
        studentEvent1.setLastname("Kacar");
        studentEvent1.setEmail("kemal@student.pxl.be");
        studentEvent1.setPassword("password");
        studentEvent1.setJoinedEvent(event1);
        studentRepository.save(studentEvent1);

        Student studentEvent2 = new Student();
        studentEvent2.setFirstname("Jan");
        studentEvent2.setLastname("Janssen");
        studentEvent2.setEmail("jan@student.pxl.be");
        studentEvent2.setPassword("password");
        studentEvent2.setJoinedEvent(event2);
        studentRepository.save(studentEvent2);

        List<StudentProfileResponse> result = studentService.getStudentsByEvent(event1.getId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().email()).isEqualTo("kemal@student.pxl.be");
    }

    @Test
    void getScansForStudent_Success() {
        Student student = savedStudent();
        Bedrijf bedrijf = new Bedrijf();
        bedrijf.setFirstname("Jan");
        bedrijf.setLastname("Desmedt");
        bedrijf.setEmail("jandesmedt@cegeka.be");
        bedrijf.setSector("IT");
        bedrijf.setPassword("password");
        bedrijfRepository.save(bedrijf);

        Scan scan = new Scan();
        scan.setStudentEmail(student.getEmail());
        scan.setStudentFirstname(student.getFirstname());
        scan.setStudentLastname(student.getLastname());
        scan.setStudentEducation(student.getEducation());
        scan.setScannedAt(LocalDateTime.now());
        scan.setBedrijf(bedrijf);
        scanRepository.save(scan);

        List<StudentScanResponseDTO> result = studentService.getScansForStudent("kemal@student.pxl.be");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().bedrijfEmail()).isEqualTo("jandesmedt@cegeka.be");
        assertThat(result.getFirst().bedrijfSector()).isEqualTo("IT");
        assertThat(result.getFirst().bedrijfName()).isEqualTo("cegeka");
    }

    @Test
    void getScansForStudent_NoScans() {
        savedStudent();

        List<StudentScanResponseDTO> result = studentService.getScansForStudent("kemal@student.pxl.be");

        assertThat(result).isEmpty();
    }
}
