package be.pxl.researchproject.integration;

import be.pxl.researchproject.model.HandshakeEvent;
import be.pxl.researchproject.model.Student;
import be.pxl.researchproject.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@SuppressWarnings({"PMD.TooManyMethods", "PMD.LawOfDemeter"})
public class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Student student;

    @BeforeEach
    public void setUp() {
        student = new Student();
        student.setEmail("test.student@student.pxl.be");
        student.setFirstname("Pieter");
        student.setLastname("Peeters");
        student.setEducation("Professionele bachelor toegepaste informatica");
        student.setPassword("password123");
        entityManager.persistAndFlush(student);
    }

    @Test
    public void shouldFindStudentByEmail() {
        Optional<Student> result = studentRepository.findByEmail(student.getEmail());

        assertThat(result).isPresent();
        assertThat(result.get().getFirstname()).isEqualTo("Pieter");
        assertThat(result.get().getLastname()).isEqualTo("Peeters");
    }

    @Test
    public void shouldReturnEmptyWhenStudentEmailDoesNotExist() {
        Optional<Student> result = studentRepository.findByEmail("missing@student.pxl.be");

        assertThat(result).isEmpty();
    }

    @Test
    public void shouldCheckEmailExists() {
        boolean exists = studentRepository.existsByEmail(student.getEmail());

        assertThat(exists).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenEmailDoesNotExist() {
        boolean exists = studentRepository.existsByEmail("missing@student.pxl.be");

        assertThat(exists).isFalse();
    }

    @Test
    public void shouldFindProfileByEmail() {
        var profile = studentRepository.findProfileByEmail(student.getEmail());

        assertThat(profile).isPresent();
        assertThat(profile.get().getFirstname()).isEqualTo("Pieter");
        assertThat(profile.get().getLastname()).isEqualTo("Peeters");
        assertThat(profile.get().getEmail()).isEqualTo(student.getEmail());
        assertThat(profile.get().getEducation()).isEqualTo("Professionele bachelor toegepaste informatica");
        assertThat(profile.get().getCvFileName()).isNull();
        assertThat(profile.get().getCvFileSize()).isNull();
    }

    @Test
    public void shouldFindAuthByEmail() {
        var auth = studentRepository.findAuthByEmail(student.getEmail());

        assertThat(auth).isPresent();
        assertThat(auth.get().getPassword()).isEqualTo("password123");
    }

    @Test
    public void shouldFindStudentsByJoinedEventId() {
        HandshakeEvent event = new HandshakeEvent(
                "Career Day",
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                LocalTime.of(15, 0),
                "Campus"
        );
        entityManager.persistAndFlush(event);

        student.setJoinedEvent(event);
        entityManager.persistAndFlush(student);

        var students = studentRepository.findByJoinedEvent_Id(event.getId());

        assertThat(students).hasSize(1);
        assertThat(students.get(0).getEmail()).isEqualTo(student.getEmail());
    }

    @Test
    public void shouldReturnEmptyWhenEventHasNoStudents() {
        HandshakeEvent event = new HandshakeEvent(
                "Empty Day",
                LocalDate.now().plusDays(10),
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                "Hall"
        );
        entityManager.persistAndFlush(event);

        var students = studentRepository.findByJoinedEvent_Id(event.getId());

        assertThat(students).isEmpty();
    }

    @Test
    public void shouldUpdateCvByEmail() {
        int updated = studentRepository.updateCvByEmail(
                student.getEmail(),
                "resume.pdf",
                "application/pdf",
                1024L,
                "%PDF-1.4".getBytes()
        );

        entityManager.getEntityManager().flush();
        entityManager.getEntityManager().clear();

        assertThat(updated).isEqualTo(1);
        Student refreshed = studentRepository.findByEmail(student.getEmail()).orElseThrow();

        assertThat(refreshed.getCvFileName()).isEqualTo("resume.pdf");
        assertThat(refreshed.getCvContentType()).isEqualTo("application/pdf");
        assertThat(refreshed.getCvFileSize()).isEqualTo(1024L);
    }

    @Test
    public void shouldFindCvByEmailAfterUpdate() {
        studentRepository.updateCvByEmail(
                student.getEmail(),
                "resume.pdf",
                "application/pdf",
                1024L,
                "%PDF-1.4".getBytes()
        );

        entityManager.getEntityManager().flush();
        entityManager.getEntityManager().clear();

        var cvProjection = studentRepository.findCvByEmail(student.getEmail());

        assertThat(cvProjection).isPresent();
        assertThat(cvProjection.get().getCvFileName()).isEqualTo("resume.pdf");
        assertThat(cvProjection.get().getCvContentType()).isEqualTo("application/pdf");
        assertThat(cvProjection.get().getCvFileContent()).isEqualTo("%PDF-1.4".getBytes());
    }

    @Test
    public void shouldDeleteCvByEmail() {
        studentRepository.updateCvByEmail(
                student.getEmail(),
                "resume.pdf",
                "application/pdf",
                1024L,
                "%PDF-1.4".getBytes()
        );

        entityManager.getEntityManager().flush();
        entityManager.getEntityManager().clear();

        int deleted = studentRepository.deleteCvByEmail(student.getEmail());

        entityManager.getEntityManager().flush();
        entityManager.getEntityManager().clear();

        assertThat(deleted).isEqualTo(1);
        var cvProjection = studentRepository.findCvByEmail(student.getEmail());
        assertThat(cvProjection).isPresent();
        assertThat(cvProjection.get().getCvFileName()).isNull();
        assertThat(cvProjection.get().getCvContentType()).isNull();
        assertThat(cvProjection.get().getCvFileContent()).isNull();
    }

    @Test
    public void shouldNotUpdateCvWhenEmailDoesNotExist() {
        int updated = studentRepository.updateCvByEmail(
                "missing@student.pxl.be",
                "file.pdf",
                "application/pdf",
                512L,
                "content".getBytes()
        );

        assertThat(updated).isZero();
    }

    @Test
    public void shouldNotDeleteCvWhenEmailDoesNotExist() {
        int deleted = studentRepository.deleteCvByEmail("missing@student.pxl.be");

        assertThat(deleted).isZero();
    }
}
