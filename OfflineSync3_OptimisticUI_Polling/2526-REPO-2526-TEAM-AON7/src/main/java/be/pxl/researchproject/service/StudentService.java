package be.pxl.researchproject.service;

import be.pxl.researchproject.controller.dto.*;
import be.pxl.researchproject.model.HandshakeEvent;
import be.pxl.researchproject.repository.HandshakeEventRepository;

import be.pxl.researchproject.exception.BadRequestException;
import be.pxl.researchproject.model.Student;
import be.pxl.researchproject.repository.ScanRepository;
import be.pxl.researchproject.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class StudentService {
    private final HandshakeEventRepository handshakeEventRepository;
    private final StudentRepository studentRepository;
    private final ScanRepository scanRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository, HandshakeEventRepository handshakeEventRepository, ScanRepository scanRepository) {
        this.studentRepository = studentRepository;
        this.handshakeEventRepository = handshakeEventRepository;
        this.scanRepository = scanRepository;
    }

    @Transactional
    public JoinEventResponse joinEvent(String email, JoinEventRequest request) {
        String normalizedEmail = email.toLowerCase(Locale.ROOT).trim();
        Student student = studentRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("Student niet gevonden"));
        HandshakeEvent event = handshakeEventRepository.findById(request.id())
                .orElseThrow(() -> new BadRequestException("Event niet gevonden"));
        if (student.hasJoinedEvent()) {
            return new JoinEventResponse(false, "Je neemt al deel aan een evenement.");
        }
        student.setJoinedEvent(event);
        studentRepository.save(student);
        return new JoinEventResponse(true, "Je bent succesvol gejoined!");
    }

    public void createStudent(StudentRequest studentRequest) {
        String normalEmail = studentRequest.getEmail().toLowerCase().trim();

        if(studentRepository.existsByEmail(normalEmail)) {
            throw new BadRequestException("Email is al in gebruik");
        }
        Student student = new Student();
        student.setFirstname(studentRequest.getFirstname());
        student.setLastname(studentRequest.getLastname());
        student.setEducation(studentRequest.getEducation());
        student.setEmail(normalEmail);
        student.setPassword(studentRequest.getPassword());
        studentRepository.save(student);
    }

    public List<StudentProfileResponse> getStudentsByEvent(Long eventId) {
        List<Student> students = studentRepository.findByJoinedEvent_Id(eventId);
        List<StudentProfileResponse> result = new ArrayList<>();
        for (Student s : students) {
            String cvFileName = s.getCvFileName();
            boolean hasCv = cvFileName != null && !cvFileName.isBlank();
            result.add(new StudentProfileResponse(
                    s.getFirstname(),
                    s.getLastname(),
                    s.getEmail(),
                    s.getEducation(),
                    hasCv,
                    cvFileName,
                    s.getCvFileSize(),
                    s.hasJoinedEvent(),
                    s.getJoinedEventId()
            ));
        }
        return result;
    }

    public StudentProfileResponse getStudentProfileByEmail(String email) {
        String normalizedEmail = email.toLowerCase(Locale.ROOT).trim();
        Student student = studentRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new BadRequestException("Student niet gevonden"));

        boolean hasCv = student.getCvFileName() != null && !student.getCvFileName().isBlank();
        boolean hasJoinedEvent = student.hasJoinedEvent();
        Long joinedEventId = student.getJoinedEventId();

        return new StudentProfileResponse(
            student.getFirstname(),
            student.getLastname(),
            student.getEmail(),
            student.getEducation(),
            hasCv,
            student.getCvFileName(),
            student.getCvFileSize(),
            hasJoinedEvent,
            joinedEventId
        );
    }

    @Transactional
    public StudentProfileResponse updateStudentProfile(String email, StudentProfileUpdateRequest request) {
        String normalizedEmail = email.toLowerCase(Locale.ROOT).trim();
        Student student = studentRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("Student niet gevonden"));

        String updatedEmail = request.email().toLowerCase(Locale.ROOT).trim();
        if (!updatedEmail.equals(student.getEmail()) && studentRepository.existsByEmail(updatedEmail)) {
            throw new BadRequestException("Email is al in gebruik");
        }

        student.setFirstname(request.firstname().trim());
        student.setLastname(request.lastname().trim());
        student.setEmail(updatedEmail);
        student.setEducation(request.education().trim());
        studentRepository.save(student);

        boolean hasCv = student.getCvFileName() != null && !student.getCvFileName().isBlank();
        boolean hasJoinedEvent = student.hasJoinedEvent();
        Long joinedEventId = student.getJoinedEventId();

        return new StudentProfileResponse(
                student.getFirstname(),
                student.getLastname(),
                student.getEmail(),
                student.getEducation(),
                hasCv,
                student.getCvFileName(),
                student.getCvFileSize(),
                hasJoinedEvent,
                joinedEventId
        );
    }

    public List<StudentScanResponseDTO> getScansForStudent(String email) {
        String normalizedEmail = email.toLowerCase(Locale.ROOT).trim();
        return scanRepository.findByStudentEmail(normalizedEmail).stream()
                .map(s -> new StudentScanResponseDTO(
                        s.getBedrijf().getEmail().split("@")[1].split("\\.")[0],
                        s.getBedrijf().getEmail(),
                        s.getBedrijf().getSector(),
                        s.getScannedAt()
                ))
                .toList();
    }
}
