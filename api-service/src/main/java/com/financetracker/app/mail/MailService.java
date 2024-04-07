package com.financetracker.app.mail;

import com.financetracker.api.mail.MailDTO;
import com.financetracker.api.mail.Template;
import com.financetracker.app.rabbitmq.QueueSender;
import com.financetracker.app.user.User;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Getter
@Setter
public class MailService {

    private final QueueSender queueSender;

    public MailDTO createGreetingMail(@NotNull User user) {
        if (user == null) {
            throw new IllegalArgumentException();
        }

        Template template = Template.GREETING;

        Map<String, Object> properties = Map.of(
            "username", user.getUsername()
        );

        return MailDTO.builder()
            .template(template)
            .recipient(user.getEmail())
            .title(template.getTitle())
            .templateProperties(properties)
            .build();
    }

    public void sendMail(@NotNull MailDTO mail) {
        if (mail == null) {
            throw new IllegalArgumentException();
        }

        queueSender.send(mail);
    }

}