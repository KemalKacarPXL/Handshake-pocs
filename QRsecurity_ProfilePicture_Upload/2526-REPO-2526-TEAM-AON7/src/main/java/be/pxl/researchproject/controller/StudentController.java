package be.pxl.researchproject.controller;

import be.pxl.researchproject.controller.dto.*;
import be.pxl.researchproject.model.Student;
import be.pxl.researchproject.service.BedrijfService;
import org.springframework.web.bind.annotation.RequestHeader;

import be.pxl.researchproject.service.StudentCvService;
import be.pxl.researchproject.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    private final StudentService studentService;
    private final StudentCvService studentCvService;
    private final BedrijfService bedrijfService;

    @Autowired
    public StudentController(StudentService studentService, StudentCvService studentCvService, BedrijfService bedrijfService) {
        this.studentService = studentService;
        this.studentCvService = studentCvService;
        this.bedrijfService = bedrijfService;
    }

    @PostMapping
    public ResponseEntity<Void> createStudent(@Valid @RequestBody StudentRequest studentRequest) {
        studentService.createStudent(studentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/by-event/{eventId}")
    public ResponseEntity<List<StudentProfileResponse>> getStudentsByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(studentService.getStudentsByEvent(eventId));
    }

    @GetMapping("/profile")
    public ResponseEntity<StudentProfileResponse> getStudentProfile(@RequestParam String email) {
        StudentProfileResponse profile = studentService.getStudentProfileByEmail(email);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<StudentProfileResponse> updateStudentProfile(
            @RequestParam String email,
            @Valid @RequestBody StudentProfileUpdateRequest request
    ) {
        StudentProfileResponse updatedProfile = studentService.updateStudentProfile(email, request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/profile/cv")
    public ResponseEntity<Void> uploadCv(@RequestParam String email, @RequestParam("file") MultipartFile file) {
        studentCvService.uploadCv(email, file);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile/cv/open")
    public ResponseEntity<byte[]> openCv(@RequestParam String email) {
        return buildCvResponse(email, false);
    }

    @GetMapping("/profile/cv/download")
    public ResponseEntity<byte[]> downloadCv(@RequestParam String email) {
        return buildCvResponse(email, true);
    }

    private ResponseEntity<byte[]> buildCvResponse(String email, boolean asAttachment) {
        StudentCvDownloadResponse cv = studentCvService.downloadCv(email);
        MediaType mediaType = asAttachment
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(cv.contentType());
        String contentDisposition = asAttachment
                ? ContentDisposition.attachment().filename(cv.fileName()).build().toString()
                : ContentDisposition.inline().filename(cv.fileName()).build().toString();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header("X-Content-Type-Options", "nosniff")
                .contentType(mediaType)
                .contentLength(cv.fileContent().length)
                .body(cv.fileContent());
    }

    // --- PROFIELFOTO ENDPOINTS ---

    // 1. Uploaden van de profielfoto
    @PostMapping(value = "/profile/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadProfilePicture(
            @RequestParam String email,
            @RequestParam("file") MultipartFile file) {
        
        // Let op: je moet deze methode nog aanmaken in je StudentService!
        studentService.uploadProfilePicture(email, file);
        return ResponseEntity.ok().build();
    }

    // 2. Verwijderen van de profielfoto
    @DeleteMapping("/profile/picture")
    public ResponseEntity<Void> deleteProfilePicture(@RequestParam String email) {
        
        studentService.deleteProfilePicture(email);
        return ResponseEntity.ok().build();
    }

    // 3. Ophalen van de profielfoto (handig om in de frontend te tonen)
    @GetMapping(value = "/profile/picture")
    public ResponseEntity<byte[]> getProfilePicture(@RequestParam String email) {
        
        // Optioneel: Haal content type en bytes op via de service
        Student student = studentService.getStudentEntityByEmail(email); 
        if (student.getProfilePictureContent() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(student.getProfilePictureContentType()))
                .body(student.getProfilePictureContent());
    }

    @PostMapping("/join-event")
    public ResponseEntity<JoinEventResponse> joinEvent(@RequestHeader("student-email") String email,
            @RequestBody JoinEventRequest request) {
        JoinEventResponse response = studentService.joinEvent(email, request);
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    @DeleteMapping("/profile/cv")
    public ResponseEntity<Void> deleteCv(@RequestParam String email) {
        studentCvService.deleteCv(email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile/scan")
    public ResponseEntity<Void> scan(
            @RequestParam String email,
            @RequestParam(required = false) String bedrijfEmail) {
        if (bedrijfEmail != null && !bedrijfEmail.isBlank()) {
            bedrijfService.registerScan(email, bedrijfEmail);
        }
        return ResponseEntity.ok().build();
    }
    @GetMapping("/scans")
    public ResponseEntity<List<StudentScanResponseDTO>> getStudentScans(@RequestParam String email) {
        return ResponseEntity.ok(studentService.getScansForStudent(email));
    }
}
