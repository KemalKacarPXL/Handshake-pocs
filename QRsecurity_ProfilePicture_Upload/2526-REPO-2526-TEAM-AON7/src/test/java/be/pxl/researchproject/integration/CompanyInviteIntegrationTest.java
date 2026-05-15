package be.pxl.researchproject.integration;

import be.pxl.researchproject.controller.dto.CreateCompanyInviteRequest;
import be.pxl.researchproject.controller.dto.SendCompanyInviteEmailRequest;
import be.pxl.researchproject.model.CompanyInvite;
import be.pxl.researchproject.model.Coordinator;
import be.pxl.researchproject.repository.CompanyInviteRepository;
import be.pxl.researchproject.repository.CoordinatorRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CompanyInviteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyInviteRepository companyInviteRepository;

    @Autowired
    private CoordinatorRepository coordinatorRepository;

    @MockitoBean
    private JavaMailSender mailSender;

    private Coordinator testCoordinator;

    @BeforeEach
    void setUp() {
        companyInviteRepository.deleteAll();
        coordinatorRepository.deleteAll();
        
        testCoordinator = new Coordinator("coordinator@pxl.be", "password");
        coordinatorRepository.save(testCoordinator);

        // Mock mail sender to return a MimeMessage when created
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @SuppressWarnings("PMD.LawOfDemeter")
    void shouldCreateInviteAndSendEmail() throws Exception {
        // 1. Create the invite
        CreateCompanyInviteRequest createRequest = new CreateCompanyInviteRequest("Test Company", "company@test.com");
        
        MvcResult createResult = mockMvc.perform(post("/api/company-invites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        MockHttpServletResponse response = createResult.getResponse();
        String responseJson = response.getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(responseJson, new TypeReference<>() {
        });
        String token = (String) responseMap.get("token");
        String inviteLink = (String) responseMap.get("inviteLink");

        assertThat(token).isNotEmpty();
        assertThat(inviteLink).contains(token);

        // 2. Send the email
        SendCompanyInviteEmailRequest sendRequest = new SendCompanyInviteEmailRequest(
                testCoordinator.getEmail(), 
                token, 
                inviteLink
        );

        mockMvc.perform(post("/api/company-invites/send-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("E-mail verzonden"));

        // 3. Verify in database and mail sender
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        
        CompanyInvite invite = companyInviteRepository.findByToken(token).orElseThrow();
        assertThat(invite.isInvitationEmailSent()).isTrue();
    }

    @Test
    void shouldFailToSendEmail_WhenCoordinatorNotFound() throws Exception {
        // Create an invite first
        CompanyInvite invite = new CompanyInvite("Test", "test@test.com", "token123", LocalDateTime.now().plusDays(1));
        companyInviteRepository.save(invite);

        SendCompanyInviteEmailRequest sendRequest = new SendCompanyInviteEmailRequest(
                "nonexistent@pxl.be", 
                "token123", 
                "http://localhost/invite/token123"
        );

        mockMvc.perform(post("/api/company-invites/send-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Onbekende coördinator"));

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void shouldFailToSendEmail_WhenTokenInvalid() throws Exception {
        SendCompanyInviteEmailRequest sendRequest = new SendCompanyInviteEmailRequest(
                testCoordinator.getEmail(), 
                "invalid-token", 
                "http://localhost/invite/invalid-token"
        );

        mockMvc.perform(post("/api/company-invites/send-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ongeldige uitnodiging"));

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void shouldSendEmailWhenCoordinatorEmailHasWhitespace() throws Exception {
        CompanyInvite invite = new CompanyInvite("Test", "test@test.com", "token-whitespace", LocalDateTime.now().plusDays(1));
        companyInviteRepository.save(invite);

        SendCompanyInviteEmailRequest sendRequest = new SendCompanyInviteEmailRequest(
                "  " + testCoordinator.getEmail() + "  ",
                "token-whitespace",
                "http://localhost/invite/token-whitespace"
        );

        mockMvc.perform(post("/api/company-invites/send-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("E-mail verzonden"));

        verify(mailSender, times(1)).send(any(MimeMessage.class));
        CompanyInvite updatedInvite = companyInviteRepository.findByToken("token-whitespace").orElseThrow();
        assertThat(updatedInvite.isInvitationEmailSent()).isTrue();
    }

    @Test
    void shouldFailToSendEmailWhenCoordinatorEmailIsBlank() throws Exception {
        CompanyInvite invite = new CompanyInvite("Test", "test@test.com", "token-blank-email", LocalDateTime.now().plusDays(1));
        companyInviteRepository.save(invite);

        SendCompanyInviteEmailRequest sendRequest = new SendCompanyInviteEmailRequest(
                "   ",
                "token-blank-email",
                "http://localhost/invite/token-blank-email"
        );

        mockMvc.perform(post("/api/company-invites/send-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("E-mailadres coördinator ontbreekt"));

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void shouldFailToSendEmailWhenCoordinatorEmailIsNull() throws Exception {
        CompanyInvite invite = new CompanyInvite("Test", "test@test.com", "token-null-email", LocalDateTime.now().plusDays(1));
        companyInviteRepository.save(invite);

        SendCompanyInviteEmailRequest sendRequest = new SendCompanyInviteEmailRequest(
                null,
                "token-null-email",
                "http://localhost/invite/token-null-email"
        );

        mockMvc.perform(post("/api/company-invites/send-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("E-mailadres coördinator ontbreekt"));

        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
