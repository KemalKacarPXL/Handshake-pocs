package be.pxl.researchproject.config;

import be.pxl.researchproject.mail.ConsoleJavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    /**
     * Echte SMTP wanneer {@code app.mail.smtp-enabled=true} (bijv. {@code MAIL_SMTP_ENABLED=true}
     * of profiel {@code mailpit}). Anders: {@link ConsoleJavaMailSender}.
     */
    @Bean
    @ConditionalOnProperty(name = "app.mail.smtp-enabled", havingValue = "true")
    public JavaMailSender smtpJavaMailSender(
            @Value("${app.mail.smtp-host}") String host,
            @Value("${app.mail.smtp-port:587}") int port,
            @Value("${app.mail.smtp-user:}") String user,
            @Value("${app.mail.smtp-password:}") String password,
            @Value("${app.mail.smtp-starttls:true}") boolean startTls,
            @Value("${app.mail.smtp-auth:true}") boolean auth) {
        if (host == null || host.isBlank()) {
            throw new IllegalStateException(
                    "SMTP staat aan maar app.mail.smtp-host is leeg. Zet MAIL_SMTP_HOST of start met "
                            + "profiel mailpit (start Mailpit met: docker compose up -d).");
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host.trim());
        sender.setPort(port);
        String trimmedUser = user != null ? user.trim() : "";
        if (!trimmedUser.isEmpty()) {
            sender.setUsername(trimmedUser);
            sender.setPassword(password != null ? password : "");
        }

        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        boolean useAuth = auth && !trimmedUser.isEmpty();
        props.put("mail.smtp.auth", Boolean.toString(useAuth));
        props.put("mail.smtp.starttls.enable", Boolean.toString(startTls));
        sender.setJavaMailProperties(props);
        return sender;
    }

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender consoleJavaMailSender() {
        return new ConsoleJavaMailSender();
    }
}
