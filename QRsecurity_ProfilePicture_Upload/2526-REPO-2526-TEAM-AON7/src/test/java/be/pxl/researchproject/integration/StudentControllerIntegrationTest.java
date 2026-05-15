package be.pxl.researchproject.integration;

import be.pxl.researchproject.controller.dto.JoinEventRequest;
import be.pxl.researchproject.controller.dto.StudentRequest;
import be.pxl.researchproject.model.Bedrijf;
import be.pxl.researchproject.model.HandshakeEvent;
import be.pxl.researchproject.model.Scan;
import be.pxl.researchproject.model.Student;
import be.pxl.researchproject.repository.BedrijfRepository;
import be.pxl.researchproject.repository.HandshakeEventRepository;
import be.pxl.researchproject.repository.ScanRepository;
import be.pxl.researchproject.repository.StudentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@SuppressWarnings("PMD.TooManyMethods") //anders foutcode PMD teveel functies in 1 klasse
public class StudentControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private HandshakeEventRepository handshakeEventRepository;

    @Autowired
    private BedrijfRepository bedrijfRepository;

    @Autowired
    private ScanRepository scanRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Student savedStudent() {
        Student s = new Student();
        s.setFirstname("Kemal");
        s.setLastname("Kacar");
        s.setEmail("kemal@student.pxl.be");
        s.setPassword("password");
        s.setEducation("Professionele Bachelor Toegepaste Informatica");
        return studentRepository.save(s);
    }

    private HandshakeEvent savedEvent() {
        HandshakeEvent e = new HandshakeEvent(
                "Trixxo Event",
                LocalDate.of(2026, 5, 1),
                LocalTime.of(16, 30),
                LocalTime.of(19, 30),
                "Hasselt"
        );
        return handshakeEventRepository.save(e);
    }

    private Bedrijf savedBedrijf() {
        Bedrijf b = new Bedrijf();
        b.setFirstname("Jan");
        b.setLastname("Janssen");
        b.setEmail("jan@bedrijf.be");
        b.setPassword("password");
        b.setSector("IT");
        return bedrijfRepository.save(b);
    }

    @Test
    void createStudent_shouldReturn201() throws Exception {
        StudentRequest request = new StudentRequest("Kemal", "Kacar", "kemal@student.pxl.be",
                "password123", "Professionele Bachelor Toegepaste Informatica");

        mockMvc.perform(post("/api/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertThat(studentRepository.existsByEmail("kemal@student.pxl.be")).isTrue();
    }

    @Test
    void createStudent_shouldReturn400_whenEmailDuplicate() throws Exception {
        savedStudent();

        StudentRequest request = new StudentRequest("Andere", "Student", "kemal@student.pxl.be",
                "password123", "Professionele Bachelor Toegepaste Informatica");

        mockMvc.perform(post("/api/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createStudent_shouldReturn400_whenFieldsMissing() throws Exception {
        StudentRequest request = new StudentRequest();

        mockMvc.perform(post("/api/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStudentProfile_shouldReturn200() throws Exception {
        savedStudent();

        mockMvc.perform(get("/api/student/profile")
                        .param("email", "kemal@student.pxl.be"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("Kemal"))
                .andExpect(jsonPath("$.lastname").value("Kacar"))
                .andExpect(jsonPath("$.email").value("kemal@student.pxl.be"))
                .andExpect(jsonPath("$.hasCv").value(false));
    }

    @Test
    void getStudentProfile_shouldReturn400_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/student/profile")
                        .param("email", "onbekend@student.pxl.be"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStudentProfile_shouldNormalizeEmail() throws Exception {
        savedStudent();

        mockMvc.perform(get("/api/student/profile")
                        .param("email", "KEMAL@student.pxl.be"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("kemal@student.pxl.be"));
    }

    @Test
    void getStudentsByEvent_shouldReturn200_withStudents() throws Exception {
        HandshakeEvent event = savedEvent();
        Student student = savedStudent();
        student.setJoinedEvent(event);
        studentRepository.save(student);

        mockMvc.perform(get("/api/student/by-event/{eventId}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("kemal@student.pxl.be"))
                .andExpect(jsonPath("$[0].firstname").value("Kemal"));
    }

    @Test
    void getStudentsByEvent_shouldReturnEmptyList_whenNoStudents() throws Exception {
        HandshakeEvent event = savedEvent();

        mockMvc.perform(get("/api/student/by-event/{eventId}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void joinEvent_shouldReturn200() throws Exception {
        HandshakeEvent event = savedEvent();
        savedStudent();

        mockMvc.perform(post("/api/student/join-event")
                        .header("student-email", "kemal@student.pxl.be")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new JoinEventRequest(event.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void joinEvent_shouldReturn409_whenAlreadyJoined() throws Exception {
        HandshakeEvent event = savedEvent();
        Student student = savedStudent();
        student.setJoinedEvent(event);
        studentRepository.save(student);

        mockMvc.perform(post("/api/student/join-event")
                        .header("student-email", "kemal@student.pxl.be")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new JoinEventRequest(event.getId()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void uploadCv_shouldReturn200() throws Exception {
        savedStudent();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                "application/pdf",
                "%PDF-1.4 test content".getBytes()
        );

        mockMvc.perform(multipart("/api/student/profile/cv")
                        .file(file)
                        .param("email", "kemal@student.pxl.be"))
                .andExpect(status().isOk());
    }

    @Test
    void uploadCv_shouldReturn400_whenNotPdf() throws Exception {
        savedStudent();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.txt",
                "text/plain",
                "dit is geen pdf".getBytes()
        );

        mockMvc.perform(multipart("/api/student/profile/cv")
                        .file(file)
                        .param("email", "kemal@student.pxl.be"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCv_shouldReturn204() throws Exception {
        Student student = savedStudent();
        student.setCvFileName("cv.pdf");
        student.setCvContentType("application/pdf");
        student.setCvFileSize(100L);
        student.setCvFileContent(new byte[]{1, 2, 3});
        studentRepository.save(student);

        mockMvc.perform(delete("/api/student/profile/cv")
                        .param("email", "kemal@student.pxl.be"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCv_shouldReturn400_whenStudentNotFound() throws Exception {
        mockMvc.perform(delete("/api/student/profile/cv")
                        .param("email", "onbekend@student.pxl.be"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStudentScans_shouldReturn200_withScans() throws Exception {
        Student student = savedStudent();
        Bedrijf bedrijf = savedBedrijf();

        Scan scan = new Scan();
        scan.setStudentEmail(student.getEmail());
        scan.setStudentFirstname(student.getFirstname());
        scan.setStudentLastname(student.getLastname());
        scan.setStudentEducation(student.getEducation());
        scan.setScannedAt(LocalDateTime.now());
        scan.setBedrijf(bedrijf);
        scanRepository.save(scan);

        mockMvc.perform(get("/api/student/scans")
                        .param("email", "kemal@student.pxl.be"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bedrijfEmail").value("jan@bedrijf.be"))
                .andExpect(jsonPath("$[0].bedrijfSector").value("IT"));
    }

    @Test
    void getStudentScans_shouldReturnEmptyList_whenNoScans() throws Exception {
        savedStudent();

        mockMvc.perform(get("/api/student/scans")
                        .param("email", "kemal@student.pxl.be"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void openCv_shouldReturn200_withPdfContent() throws Exception {
        Student student = savedStudent();
        byte[] pdfBytes = "%PDF-1.4 test".getBytes();
        student.setCvFileName("cv.pdf");
        student.setCvContentType("application/pdf");
        student.setCvFileSize((long) pdfBytes.length);
        student.setCvFileContent(pdfBytes);
        studentRepository.save(student);

        mockMvc.perform(get("/api/student/profile/cv/open")
                        .param("email", "kemal@student.pxl.be"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void openCv_shouldReturn400_whenNoCv() throws Exception {
        savedStudent();

        mockMvc.perform(get("/api/student/profile/cv/open")
                        .param("email", "kemal@student.pxl.be"))
                .andExpect(status().isBadRequest());
    }
}
