package com.financetracker.app.mailservice.mail;

import com.financetracker.api.mail.MailDTO;
import com.financetracker.api.mail.Template;
import com.financetracker.app.mailservice.IntegrationTestConfig;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

public class MailIntegrationTest extends IntegrationTestConfig {

    private static final String GREETING_MAIL_PATH = "mails/greeting-mail.txt";
    private static final String GENERAL_MONTHLY_MAIL_PATH = "mails/general-monthly-report-mail.html";
    private static final String GENERAL_WEEKLY_MAIL_PATH = "mails/general-weekly-report-mail.html";

    @Value("${spring.rabbitmq.exchange}")
    private String EXCHANGE_NAME;

    @Value("${spring.rabbitmq.routing-key}")
    private String ROUTING_KEY;

    @Autowired
    MailCreator mailCreator;

    @Autowired
    MailDeliverer mailDeliverer;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    public void before() {
        greenMail.reset();
    }

    @Test
    public void shouldSendGreetingMail() throws IOException {
//        given
        MailDTO mail = MailDTO.builder()
                .title("Title")
                .recipient("example@mail.com")
                .template(Template.GREETING)
                .templateProperties(Map.of(
                    "username", "Anne"
                ))
                .build();
        String expectedText = Jsoup.parse(new String(MailIntegrationTest.class.getClassLoader().getResource(GREETING_MAIL_PATH).openStream().readAllBytes(), StandardCharsets.UTF_8)).wholeText();


//        when
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, mail);

//        then
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(greenMail.getReceivedMessages()).hasSize(1);
            MimeMessage message = greenMail.getReceivedMessages()[0];
            assertThat(message.getSubject()).isEqualTo("Title");
            assertThat(message.getAllRecipients()[0].toString()).isEqualTo("example@mail.com");
            InputStream inputStream = MimeUtility.decode(message.getInputStream(), "quoted-printable");
            String actualText = Jsoup.parse(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)).wholeText();
            assertThat(actualText).containsIgnoringWhitespaces(expectedText);
        });
    }

    @Test
    public void shouldSendGeneralMonthlyReportMail() throws IOException {
//        given
        MailDTO mail = MailDTO.builder()
            .title("Title")
            .recipient("example@mail.com")
            .template(Template.GENERAL_MONTHLY_REPORT)
            .templateProperties(Map.of(
                "totalExpenses", "1000.50",
                "averageDailyExpense", "100",
                "weekWithHighestAverageExpense", new DateInterval(LocalDate.of(2021, 1, 8), LocalDate.of(2021,1,15)),
                "dayWithHighestAverageExpense", "Monday",
                "totalIncomes", "2000",
                "budgetSummary", "999.50",
                "dateInterval", new DateInterval(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 31)),
                "largestExpense", new Expense(LocalDate.of(2021, 1, 20), BigDecimal.valueOf(976.99), new Category("Health")),
                "user", new User("Tom")
            ))
            .build();
        String expected = Jsoup.parse(new String(MailIntegrationTest.class.getClassLoader().getResource(GENERAL_MONTHLY_MAIL_PATH).openStream().readAllBytes(), StandardCharsets.UTF_8)).body().html();


//        when
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, mail);

//        then
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(greenMail.getReceivedMessages()).hasSize(1);
            MimeMessage message = greenMail.getReceivedMessages()[0];
            assertThat(message.getSubject()).isEqualTo("Title");
            assertThat(message.getAllRecipients()[0].toString()).isEqualTo("example@mail.com");
            InputStream inputStream = MimeUtility.decode(message.getInputStream(), "quoted-printable");
            String actual = Jsoup.parse(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)).body().html();
            assertThat(actual).contains(expected);
        });
    }

    @Test
    public void shouldSendGeneralWeeklyReportMail() throws IOException {
//        given
        MailDTO mail = MailDTO.builder()
            .title("Title")
            .recipient("example@mail.com")
            .template(Template.GENERAL_WEEKLY_REPORT)
            .templateProperties(Map.of(
                "totalExpenses", "1000.50",
                "averageDailyExpense", "100",
                "totalIncomes", "2000",
                "budgetSummary", "999.50",
                "dateInterval", new DateInterval(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 31)),
                "largestExpense", new Expense(LocalDate.of(2021, 1, 20), BigDecimal.valueOf(976.99), new Category("Health")),
                "user", new User("Tom")
            ))
            .build();
        String expected = Jsoup.parse(new String(MailIntegrationTest.class.getClassLoader().getResource(GENERAL_WEEKLY_MAIL_PATH).openStream().readAllBytes(), StandardCharsets.UTF_8)).body().html();

//        when
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, mail);

//        then
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(greenMail.getReceivedMessages()).hasSize(1);
            MimeMessage message = greenMail.getReceivedMessages()[0];
            assertThat(message.getSubject()).isEqualTo("Title");
            assertThat(message.getAllRecipients()[0].toString()).isEqualTo("example@mail.com");
            InputStream inputStream = MimeUtility.decode(message.getInputStream(), "quoted-printable");
            String actual = Jsoup.parse(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)).body().html();
            assertThat(actual).contains(expected);
        });
    }

    @RequiredArgsConstructor
    @Getter
    private class User {
        private final String username;
    }

    @RequiredArgsConstructor
    @Getter
    private class DateInterval {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private final LocalDate startDate;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private final LocalDate endDate;
    }

    @RequiredArgsConstructor
    @Getter
    private class Category {
        private final String name;
    }

    @RequiredArgsConstructor
    @Getter
    private class Expense {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private final LocalDate date;
        private final BigDecimal price;
        private final Category category;
    }
}
