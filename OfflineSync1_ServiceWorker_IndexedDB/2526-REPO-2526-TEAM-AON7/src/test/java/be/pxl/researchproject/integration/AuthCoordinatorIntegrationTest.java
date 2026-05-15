package be.pxl.researchproject.integration;

import be.pxl.researchproject.controller.dto.LoginRequest;
import be.pxl.researchproject.model.Bedrijf;
import be.pxl.researchproject.model.Coordinator;
import be.pxl.researchproject.model.Student;
import be.pxl.researchproject.repository.BedrijfRepository;
import be.pxl.researchproject.repository.CoordinatorRepository;
import be.pxl.researchproject.repository.StudentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthCoordinatorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CoordinatorRepository coordinatorRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private BedrijfRepository bedrijfRepository;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        bedrijfRepository.deleteAll();
        coordinatorRepository.deleteAll();
    }

    @Test
    void shouldLoginAsCoordinatorWhenCredentialsAreValid() throws Exception {
        coordinatorRepository.save(new Coordinator("coordinator@pxl.be", "secret123"));

        LoginRequest request = new LoginRequest("coordinator@pxl.be", "secret123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.role").value("COORDINATOR"))
                .andExpect(jsonPath("$.email").value("coordinator@pxl.be"));
    }

    @Test
    void shouldReturnUnauthorizedWhenCoordinatorPasswordIsWrong() throws Exception {
        coordinatorRepository.save(new Coordinator("coordinator@pxl.be", "correct-password"));

        LoginRequest request = new LoginRequest("coordinator@pxl.be", "wrong-password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void shouldPreferCoordinatorRoleWhenSameEmailExistsInOtherTables() throws Exception {
        String sharedEmail = "shared@pxl.be";
        coordinatorRepository.save(new Coordinator(sharedEmail, "same-pass"));
        studentRepository.save(new Student("Jan", "Janssen", sharedEmail, "same-pass", "TI"));
        bedrijfRepository.save(new Bedrijf("Piet", "Peeters", sharedEmail, "same-pass"));

        LoginRequest request = new LoginRequest(sharedEmail, "same-pass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("COORDINATOR"))
                .andExpect(jsonPath("$.email").value(sharedEmail));
    }

    @Test
    void shouldReturnUnauthorizedWhenCoordinatorEmailContainsWhitespacesOrDifferentCase() throws Exception {
        coordinatorRepository.save(new Coordinator("coordinator@pxl.be", "secret123"));

        LoginRequest request = new LoginRequest("  COORDINATOR@PXL.BE  ", "secret123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }
}
