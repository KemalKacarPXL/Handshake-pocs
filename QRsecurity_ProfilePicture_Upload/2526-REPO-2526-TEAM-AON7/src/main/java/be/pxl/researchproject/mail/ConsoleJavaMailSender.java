package be.pxl.researchproject.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Gebruikt wanneer er geen {@code spring.mail.host} is ingesteld: geen echte SMTP, maar wel een werkende
 * {@link JavaMailSender} zodat uitnodigingsmails in de serverlog verschijnen (handig voor lokaal testen).
 */
public class ConsoleJavaMailSender implements JavaMailSender {

    private static final Logger log = LoggerFactory.getLogger(ConsoleJavaMailSender.class);

    private static Session session() {
        return Session.getInstance(new Properties());
    }

    @Override
    public MimeMessage createMimeMessage() {
        return new MimeMessage(session());
    }

    @Override
    public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
        try {
            return new MimeMessage(session(), contentStream);
        } catch (MessagingException ex) {
            throw new MailParseException("Could not parse MIME stream", ex);
        }
    }

    @Override
    public void send(MimeMessage mimeMessage) throws MailException {
        logMail(mimeMessage);
    }

    @Override
    public void send(MimeMessage... mimeMessages) throws MailException {
        for (MimeMessage message : mimeMessages) {
            send(message);
        }
    }

    @Override
    public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
        try {
            MimeMessage message = createMimeMessage();
            mimeMessagePreparator.prepare(message);
            send(message);
        } catch (Exception ex) {
            throw new MailParseException("Could not prepare message", ex);
        }
    }

    @Override
    public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
        for (MimeMessagePreparator preparator : mimeMessagePreparators) {
            send(preparator);
        }
    }

    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
        log.info(
                "[E-mail (console, geen SMTP)] Van: {} | Aan: {} | Onderwerp: {}\n{}",
                simpleMessage.getFrom(),
                simpleMessage.getTo() != null ? String.join(", ", simpleMessage.getTo()) : "",
                simpleMessage.getSubject(),
                simpleMessage.getText());
    }

    @Override
    public void send(SimpleMailMessage... simpleMessages) throws MailException {
        for (SimpleMailMessage msg : simpleMessages) {
            send(msg);
        }
    }

    private void logMail(MimeMessage mimeMessage) {
        try {
            String to = mimeMessage.getAllRecipients() != null
                    ? Arrays.stream(mimeMessage.getAllRecipients()).map(Object::toString).collect(Collectors.joining(", "))
                    : "";
            String from = mimeMessage.getFrom() != null && mimeMessage.getFrom().length > 0
                    ? mimeMessage.getFrom()[0].toString()
                    : "?";
            Object content = mimeMessage.getContent();
            String body = content != null ? String.valueOf(content) : "";
            log.info(
                    "[E-mail (console, geen SMTP)] Van: {} | Aan: {} | Onderwerp: {}\n{}",
                    from,
                    to,
                    mimeMessage.getSubject(),
                    body);
        } catch (Exception ex) {
            log.warn("Kon uitnodigingsmail niet tonen in log: {}", ex.getMessage());
            log.debug("Details", ex);
        }
    }
}
