package be.pxl.researchproject.service;

import be.pxl.researchproject.controller.dto.*;
import be.pxl.researchproject.model.Bedrijf;
import be.pxl.researchproject.model.HandshakeEvent;

import be.pxl.researchproject.controller.dto.StudentProfileResponse;
import be.pxl.researchproject.controller.dto.StudentProfileUpdateRequest;
import be.pxl.researchproject.controller.dto.StudentRequest;
import be.pxl.researchproject.exception.BadRequestException;
import be.pxl.researchproject.model.Scan;
import be.pxl.researchproject.model.Student;
import be.pxl.researchproject.repository.HandshakeEventRepository;
import be.pxl.researchproject.repository.ScanRepository;
import be.pxl.researchproject.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.TooManyMethods") //anders foutcode PMD teveel functies in 1 klasse
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private  HandshakeEventRepository handshakeEventRepository;

    @Mock
    private ScanRepository scanRepository;

    @InjectMocks
    private StudentService studentService;

    private StudentRequest studentRequest;

    @BeforeEach
    void setUp() {
        studentRequest = new StudentRequest("John", "Doe", "john@student.pxl.be", "password123","Professionele Bachelor Toegepaste Informatica");
    }

    @Test
    void createStudent_Success() {
        when(studentRepository.existsByEmail("john@student.pxl.be")).thenReturn(false);

        studentService.createStudent(studentRequest);

        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void createStudent_AlreadyExists_ThrowsException() {
        when(studentRepository.existsByEmail("john@student.pxl.be")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, 
                () -> studentService.createStudent(studentRequest));

        assertThat(ex.getMessage()).isEqualTo("Email is al in gebruik");
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void getStudentProfileByEmail_Success() {
        Student student = new Student("John", "Doe", "john@student.pxl.be", "password123","Professionele Bachelor Toegepaste Informatica");
        student.setCvFileName("resume.pdf");
        student.setCvFileSize(1024L);
        // Mock joinedEvent
        HandshakeEvent event = new HandshakeEvent();
        event.setId(42L);
        student.setJoinedEvent(event);

        when(studentRepository.findByEmail("john@student.pxl.be")).thenReturn(Optional.of(student));

        StudentProfileResponse response = studentService.getStudentProfileByEmail("john@student.pxl.be");

        assertThat(response.firstname()).isEqualTo("John");
        assertThat(response.email()).isEqualTo("john@student.pxl.be");
        assertThat(response.hasCv()).isTrue();
        assertThat(response.hasJoinedEvent()).isTrue();
        assertThat(response.education()).isEqualTo("Professionele Bachelor Toegepaste Informatica");
        assertThat(response.joinedEventId()).isEqualTo(42L);
    }

    @Test
    void getStudentProfileByEmail_NotFound_ThrowsException() {
        when(studentRepository.findByEmail("unknown@pxl.be")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class, 
                () -> studentService.getStudentProfileByEmail("unknown@pxl.be"));

        assertThat(ex.getMessage()).isEqualTo("Student niet gevonden");
    }

    @Test
    void joinEvent_Success() {
        HandshakeEvent event = new HandshakeEvent("Trixxo Event", LocalDate.of(2026,05,1), LocalTime.of(16,30),LocalTime.of(19,30),"Hasselt");
        event.setId(1L);

        Student student = new Student("John", "Doe", "john@student.pxl.be", "password123","Professionele Bachelor Toegepaste Informatica");

        when(studentRepository.findByEmail("john@student.pxl.be"))
                .thenReturn(Optional.of(student));
        when(handshakeEventRepository.findById(1L))
                .thenReturn(Optional.of(event));

        JoinEventResponse response = studentService.joinEvent(
                "john@student.pxl.be",
                new JoinEventRequest(1L)
        );

        assertThat(response.success()).isTrue();
        assertThat(response.message()).isEqualTo("Je bent succesvol gejoined!");
        verify(studentRepository, times(1)).save(student);
        assertThat(student.getJoinedEvent()).isEqualTo(event);
    }

    @Test
    void joinEvent_AlreadyJoined(){
        HandshakeEvent event = new HandshakeEvent("Trixxo Event", LocalDate.of(2026,05,1), LocalTime.of(16,30),LocalTime.of(19,30),"Hasselt");
        event.setId(1L);

        Student student = new Student("John", "Doe", "john@student.pxl.be", "password123","Professionele Bachelor Toegepaste Informatica");
        student.setJoinedEvent(event);

        when(studentRepository.findByEmail("john@student.pxl.be"))
                .thenReturn(Optional.of(student));
        when(handshakeEventRepository.findById(1L))
                .thenReturn(Optional.of(event));

        JoinEventResponse response = studentService.joinEvent(
                "john@student.pxl.be",
                new JoinEventRequest(1L)
        );

        assertThat(response.success()).isFalse();
        assertThat(response.message()).isEqualTo("Je neemt al deel aan een evenement.");
        verify(studentRepository, never()).save(any());
    }

    @Test
    void joinEvent_EventNotFound_ThrowsException() {
        Student student = new Student("John", "Doe", "john@student.pxl.be", "password123","Professionele Bachelor Toegepaste Informatica");

        when(studentRepository.findByEmail("john@student.pxl.be"))
                .thenReturn(Optional.of(student));
        when(handshakeEventRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.joinEvent("john@student.pxl.be", new JoinEventRequest(1L)))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("Event niet gevonden");
    }

    @Test
    void joinEvent_StudentNotFound_ThrowsException() {
        when(studentRepository.findByEmail(any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.joinEvent("john@student.pxl.be", new JoinEventRequest(1L)))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("Student niet gevonden");
    }

    @Test
    void getStudentsByEvent_Success() {
        HandshakeEvent event = new HandshakeEvent();
        event.setId(67L);

        Student student1 = new Student();
        Student student2 = new Student();
        Student student3 = new Student();
        Student student4 = new Student();
        Student student5 = new Student();
        Student student6 = new Student();

        student1.setId(67L);
        student2.setId(67L);
        student3.setId(67L);
        student4.setId(67L);
        student5.setId(67L);
        student6.setId(67L);

        List<Student> students = List.of(student1, student2, student3, student4, student5, student6);

        when(studentRepository.findByJoinedEvent_Id(67L))
                .thenReturn(students);

        List<StudentProfileResponse> result = studentService.getStudentsByEvent(67L);


        assertThat(result).hasSize(6); // beter want je kan fouten zien
        verify(studentRepository, times(1)).findByJoinedEvent_Id(67L);
    }

    @Test void getScansForStudent_Success() {
        Bedrijf bedrijf1 = new Bedrijf();
        bedrijf1.setEmail("jan.desmedt@cegeka.be");
        bedrijf1.setSector("IT");

        Bedrijf bedrijf2 = new Bedrijf();
        bedrijf2.setEmail("sophie.vandamme@mobly.be");
        bedrijf2.setSector("Software Engineering");

        Scan scan1 = new Scan();
        scan1.setStudentEmail("kemal@student.pxl.be");
        scan1.setScannedAt(LocalDateTime.now());
        scan1.setBedrijf(bedrijf1);

        Scan scan2 = new Scan();
        scan2.setStudentEmail("kemal@student.pxl.be");
        scan2.setScannedAt(LocalDateTime.now());
        scan2.setBedrijf(bedrijf2);

        when(scanRepository.findByStudentEmail("kemal@student.pxl.be"))
                .thenReturn(List.of(scan1, scan2));

        List<StudentScanResponseDTO> result = studentService.getScansForStudent("kemal@student.pxl.be");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).bedrijfSector()).isEqualTo(bedrijf1.getSector());
        assertThat(result.get(0).bedrijfName()).isEqualTo("cegeka");
        assertThat(result.get(1).bedrijfSector()).isEqualTo(bedrijf2.getSector());
        assertThat(result.get(1).bedrijfName()).isEqualTo("mobly");
    }
    void updateStudentProfile_Success() {
        Student student = new Student("John", "Doe", "john@student.pxl.be", "password123", "Oude opleiding");
        when(studentRepository.findByEmail("john@student.pxl.be")).thenReturn(Optional.of(student));
        when(studentRepository.existsByEmail("jane@student.pxl.be")).thenReturn(false);

        StudentProfileUpdateRequest request = new StudentProfileUpdateRequest(
                "  Jane  ",
                "  Smith  ",
                "  Jane@student.pxl.be  ",
                "  Nieuwe opleiding  "
        );

        StudentProfileResponse response = studentService.updateStudentProfile("john@student.pxl.be", request);

        assertThat(response.firstname()).isEqualTo("Jane");
        assertThat(response.lastname()).isEqualTo("Smith");
        assertThat(response.email()).isEqualTo("jane@student.pxl.be");
        assertThat(response.education()).isEqualTo("Nieuwe opleiding");
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void updateStudentProfile_NotFound_ThrowsException() {
        when(studentRepository.findByEmail("unknown@pxl.be")).thenReturn(Optional.empty());

        StudentProfileUpdateRequest request = new StudentProfileUpdateRequest(
            "Jane",
            "Smith",
            "jane@student.pxl.be",
            "TI"
        );

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> studentService.updateStudentProfile("unknown@pxl.be", request));

        assertThat(ex.getMessage()).isEqualTo("Student niet gevonden");
        verify(studentRepository, never()).save(any(Student.class));
    }

        @Test
        void updateStudentProfile_DuplicateEmail_ThrowsException() {
        Student student = new Student("John", "Doe", "john@student.pxl.be", "password123", "Oude opleiding");
        when(studentRepository.findByEmail("john@student.pxl.be")).thenReturn(Optional.of(student));
        when(studentRepository.existsByEmail("jane@student.pxl.be")).thenReturn(true);

        StudentProfileUpdateRequest request = new StudentProfileUpdateRequest(
            "Jane",
            "Smith",
            "jane@student.pxl.be",
            "TI"
        );

        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> studentService.updateStudentProfile("john@student.pxl.be", request));

        assertThat(ex.getMessage()).isEqualTo("Email is al in gebruik");
        verify(studentRepository, never()).save(any(Student.class));
        }
}
