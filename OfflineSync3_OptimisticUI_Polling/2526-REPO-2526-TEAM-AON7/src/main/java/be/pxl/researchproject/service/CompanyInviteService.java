package be.pxl.researchproject.service;

import be.pxl.researchproject.controller.dto.CompanyInviteDTO;
import be.pxl.researchproject.controller.dto.CreateCompanyInviteRequest;
import be.pxl.researchproject.controller.dto.SendCompanyInviteEmailRequest;
import be.pxl.researchproject.model.CompanyInvite;
import be.pxl.researchproject.repository.CompanyInviteRepository;
import be.pxl.researchproject.repository.CoordinatorRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class CompanyInviteService {

    private static final Logger log = LoggerFactory.getLogger(CompanyInviteService.class);
    private static final int EXPIRY_DAYS = 7;

    private final CompanyInviteRepository companyInviteRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final JavaMailSender mailSender;
    private final String frontendBaseUrl;
    private final boolean smtpEnabled;
    private final String smtpUser;

    public CompanyInviteService(
            CompanyInviteRepository companyInviteRepository,
            CoordinatorRepository coordinatorRepository,
            JavaMailSender mailSender,
            @Value("${app.frontend.base-url:http://localhost:5173}") String frontendBaseUrl,
            @Value("${app.mail.smtp-enabled:false}") boolean smtpEnabled,
            @Value("${app.mail.smtp-user:}") String smtpUser) {
        this.companyInviteRepository = companyInviteRepository;
        this.coordinatorRepository = coordinatorRepository;
        this.mailSender = mailSender;
        this.frontendBaseUrl = frontendBaseUrl;
        this.smtpEnabled = smtpEnabled;
        this.smtpUser = smtpUser != null ? smtpUser : "";
    }

    public CompanyInviteDTO createInvite(CreateCompanyInviteRequest request, String baseUrl) {
        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(EXPIRY_DAYS);

        CompanyInvite invite = new CompanyInvite(
                request.companyName(),
                request.email(),
                token,
                expiresAt
        );

        CompanyInvite saved = companyInviteRepository.save(invite);
        String inviteLink = baseUrl + "/register/invite/" + saved.getToken();

        return new CompanyInviteDTO(inviteLink, saved.getToken(), saved.getExpiresAt(), saved.isUsed());
    }

    public Optional<CompanyInvite> validateToken(String token) {
        return companyInviteRepository.findByToken(token)
                .filter(CompanyInvite::isValid);
    }

    public void markAsUsed(String token) {
        companyInviteRepository.findByToken(token).ifPresent(invite -> {
            invite.setUsed(true);
            companyInviteRepository.save(invite);
        });
    }

    public void sendInviteEmail(SendCompanyInviteEmailRequest request) {
        validateCoordinator(request.coordinatorEmail());
        String coordinatorEmail = request.coordinatorEmail().trim();

        CompanyInvite invite = getValidInvite(request.token());
        String link = getInviteLink(request, invite);

        try {
            sendEmail(coordinatorEmail, invite, link);
            markInviteAsSent(invite);
        } catch (MessagingException e) {
            throw new IllegalStateException("Kon de e-mail niet samenstellen.", e);
        } catch (MailException e) {
            throw new IllegalStateException("Verzenden van de e-mail is mislukt. Controleer SMTP-gegevens.", e);
        }
    }

    private void validateCoordinator(String coordinatorEmail) {
        if (coordinatorEmail == null || coordinatorEmail.isBlank()) {
            throw new IllegalArgumentException("E-mailadres coördinator ontbreekt");
        }
        String trimmedEmail = coordinatorEmail.trim();
        if (coordinatorRepository.findByEmail(trimmedEmail).isEmpty()) {
            throw new IllegalArgumentException("Onbekende coördinator");
        }
        if (smtpEnabled && !smtpUser.isBlank()
                && !trimmedEmail.equalsIgnoreCase(smtpUser.trim())) {
            throw new IllegalArgumentException(
                    "Log in als coördinator met hetzelfde adres als MAIL_SMTP_USER (nodig voor Gmail en de meeste SMTP-servers).");
        }
    }

    private CompanyInvite getValidInvite(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token ontbreekt");
        }
        String trimmedToken = token.trim();
        CompanyInvite invite = companyInviteRepository.findByToken(trimmedToken)
                .orElseThrow(() -> new IllegalArgumentException("Ongeldige uitnodiging"));

        if (!invite.isValid()) {
            throw new IllegalArgumentException("Deze uitnodiging is verlopen of reeds gebruikt");
        }
        if (invite.isInvitationEmailSent()) {
            throw new IllegalArgumentException("De uitnodigingsmail voor deze link is al verstuurd.");
        }
        if (companyInviteRepository.existsByEmailIgnoreCaseAndInvitationEmailSentTrueAndIdNot(
                invite.getEmail(), invite.getId())) {
            throw new IllegalArgumentException(
                    "Er is al een uitnodigingsmail verstuurd naar dit bedrijfs-e-mailadres.");
        }
        return invite;
    }

    private String getInviteLink(SendCompanyInviteEmailRequest request, CompanyInvite invite) {
        String link = request.inviteLink();
        if (link == null || link.isBlank()) {
            link = frontendBaseUrl.replaceAll("/+$", "") + "/register/invite/" + invite.getToken();
        } else {
            link = link.trim();
        }
        if (!link.contains(invite.getToken())) {
            throw new IllegalArgumentException("De link hoort niet bij deze uitnodiging");
        }
        return link;
    }

    private void sendEmail(String coordinatorEmail, CompanyInvite invite, String link) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
        helper.setFrom(coordinatorEmail);
        helper.setTo(invite.getEmail());
        helper.setReplyTo(coordinatorEmail);
        helper.setSubject("Uitnodiging registratie — " + invite.getCompanyName());
        String body = "Beste,\n\n"
                + "Hierbij uw registratielink voor " + invite.getCompanyName() + ":\n\n"
                + link + "\n\n"
                + "De link is 7 dagen geldig en eenmalig bruikbaar.\n\n"
                + "Met vriendelijke groet\n"
                + coordinatorEmail;
        helper.setText(body, false);
        mailSender.send(message);
    }

    private void markInviteAsSent(CompanyInvite invite) {
        invite.setInvitationEmailSent(true);
        companyInviteRepository.save(invite);
        if (smtpEnabled) {
            log.info("Uitnodigingsmail verzonden via SMTP naar {}", invite.getEmail());
        }
    }
}
