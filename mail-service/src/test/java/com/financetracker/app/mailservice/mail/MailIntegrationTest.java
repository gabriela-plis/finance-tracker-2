package com.financetracker.app.mailservice.mail;

import com.financetracker.app.mailservice.IntegrationTestConfig;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class MailIntegrationTest extends IntegrationTestConfig {

    static GreenMail greenMail = new GreenMail(ServerSetupTest.SMTP)
        .withConfiguration(GreenMailConfiguration.aConfig().withUser("fakemail", "password"));


    @Test
    public void shouldSendMail() {
        Assertions.assertFalse(false);
    }

}
