package be.pxl.researchproject.controller;

import be.pxl.researchproject.controller.dto.LoginRequest;
import be.pxl.researchproject.model.Bedrijf;
import be.pxl.researchproject.model.Coordinator;
import be.pxl.researchproject.repository.BedrijfRepository;
import be.pxl.researchproject.repository.CoordinatorRepository;
import be.pxl.researchproject.repository.StudentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CoordinatorRepository coordinatorRepository;
    private final StudentRepository studentRepository;
    private final BedrijfRepository bedrijfRepository;

    @Autowired
    public AuthController(CoordinatorRepository coordinatorRepository,  StudentRepository studentRepository, BedrijfRepository bedrijfRepository) {
        this.coordinatorRepository = coordinatorRepository;
        this.studentRepository = studentRepository;
        this.bedrijfRepository = bedrijfRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Optional<Coordinator> optionalCoordinator = coordinatorRepository.findByEmail(loginRequest.email());

        if (optionalCoordinator.isPresent() && optionalCoordinator.get().getPassword().equals(loginRequest.password())) {
            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "role", "COORDINATOR",
                    "email", optionalCoordinator.get().getEmail()
            ));
        }
        String normalizedEmail = loginRequest.email().toLowerCase(Locale.ROOT).trim();
        Optional<StudentRepository.StudentAuthProjection> optionalStudent = studentRepository.findAuthByEmail(normalizedEmail);
        if (optionalStudent.isPresent() && optionalStudent.get().getPassword().equals(loginRequest.password())) {
            return ResponseEntity.ok(Map.of("message", "Login successful", "role", "STUDENT"));
        }

        Optional<Bedrijf> optionalBedrijf = bedrijfRepository.findByEmail(loginRequest.email());
      
        if (optionalBedrijf.isPresent() && optionalBedrijf.get().getPassword().equals(loginRequest.password())) {
            return ResponseEntity.ok(Map.of("message", "Login successful", "role", "BEDRIJF"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
    }
}
