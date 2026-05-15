package be.pxl.researchproject.integration;

import be.pxl.researchproject.controller.dto.SendCompanyInviteEmailRequest;
import be.pxl.researchproject.model.CompanyInvite;
import be.pxl.researchproject.model.Coordinator;
import be.pxl.researchproject.repository.CompanyInviteRepository;
import be.pxl.researchproject.repository.CoordinatorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.mail.smtp-enabled=true",
        "app.mail.smtp-user=mail.owner@pxl.be"
})
@AutoConfigureMockMvc
@Transactional
class CompanyInviteSmtpCoordinatorIntegrationTest {

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

    @BeforeEach
    void setUp() {
        companyInviteRepository.deleteAll();
        coordinatorRepository.deleteAll();

        coordinatorRepository.save(new Coordinator("coordinator@pxl.be", "password"));
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
    }

    @Test
    void shouldFailWhenCoordinatorEmailDoesNotMatchSmtpUser() throws Exception {
        CompanyInvite invite = new CompanyInvite("Test", "test@test.com", "token-smtp-mismatch", LocalDateTime.now().plusDays(1));
        companyInviteRepository.save(invite);

        SendCompanyInviteEmailRequest request = new SendCompanyInviteEmailRequest(
                "coordinator@pxl.be",
                "token-smtp-mismatch",
                "http://localhost/invite/token-smtp-mismatch"
        );

        mockMvc.perform(post("/api/company-invites/send-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "Log in als coördinator met hetzelfde adres als MAIL_SMTP_USER (nodig voor Gmail en de meeste SMTP-servers)."));

        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
