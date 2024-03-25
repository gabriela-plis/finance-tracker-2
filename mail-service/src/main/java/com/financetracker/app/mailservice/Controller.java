package com.financetracker.app.mailservice;

import com.financetracker.app.mailservice.mail.MailDeliverer;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class Controller {

  private final JavaMailSender javaMailSender;

  @Value("${spring.mail.username}")
  private final String sender;

  private final MailDeliverer mailService;

  @GetMapping("/send-it")
    public void send() throws MessagingException {
    MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false);
    helper.setFrom(sender);
    helper.setTo(new InternetAddress("fakemail@example.com"));
    helper.setSubject("Example message");
    helper.setText("Hello World");
    mailService.sendMail(helper.getMimeMessage());
  }
}
