package com.financetracker.app.mailservice;

import com.financetracker.app.mailservice.config.ITConfig;
import com.financetracker.app.mailservice.mail.MailDeliverer;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;


public class GreenMailTest extends ITConfig {

    @Autowired
    MailDeliverer mailDeliverer;

    @Autowired
    JavaMailSender javaMailSender;


    static GreenMail greenMail = new GreenMail(ServerSetupTest.SMTP)
        .withConfiguration(GreenMailConfiguration.aConfig().withUser("fakemail", "password"));

    @BeforeAll
    public static void setup() {
        greenMail.start();
    }

    @Test
    public void should_send_mail() throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false);
        helper.setFrom("example@smthing.com");
        helper.setTo("fakemail");
        helper.setSubject("Example message");
        helper.setText("Hello World");
        mailDeliverer.sendMail(helper.getMimeMessage());

        MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
        Assertions.assertThat(GreenMailUtil.getBody(receivedMessage)).isEqualTo("Hello World");
    }
}
