package com.financetracker.app.mailservice.mail;

import com.financetracker.api.mail.MailDTO;
import com.financetracker.api.mail.Template;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MailCreatorTest {

    private final JavaMailSender javaMailSender = mock();
    private final TemplateEngine templateEngine = mock();
    private final String sender = "sender@gmail.com";

    private final MailCreator mailCreator = new MailCreator(javaMailSender, templateEngine, sender);

    @Test
    public void should_create_mail_to_send() throws MessagingException {
//        given
        MailDTO mail = getMail();
        MimeMessage mimeMessage = getMessage();
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("greeting-template"), any(Context.class))).thenReturn("Template body");

//        when
        MimeMessage result = mailCreator.createMailToSend(mail);

//        then
        assertThat(result.getAllRecipients()).hasSize(1);
        assertThat(result.getAllRecipients()[0].toString()).isEqualTo("anne@gmail.com");
        assertThat(result.getFrom()[0].toString()).isEqualTo("sender@gmail.com");
        assertThat(result.getSubject()).isEqualTo("Message subject");
    }

    private MailDTO getMail() {
        return new MailDTO(Template.GREETING, "anne@gmail.com", "Message subject", Map.of());
    }

    private MimeMessage getMessage() {
        Session session = Session.getDefaultInstance(new Properties());
        return new MimeMessage(session);
    }
}
