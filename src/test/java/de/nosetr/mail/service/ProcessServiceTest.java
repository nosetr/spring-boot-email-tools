package de.nosetr.mail.service;

import it.ozimov.springboot.mail.model.Email;
import it.ozimov.springboot.mail.service.EmailService;
import it.ozimov.springboot.mail.service.exception.CannotSendEmailException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
    "MAIL_HOST=smtp.test.local",
    "MAIL_PORT=587",
    "MAIL_USERNAME=test",
    "MAIL_PASSWORD=test",
    "MAIL_FROM_EMAIL=test@example.com",
    "MAIL_FROM_NAME=Test",
    "MAIL_SUBJECT=Test-Einladung"
})
class ProcessServiceIntegrationTest {

  @Autowired
  private ProcessService processService;

  @MockBean
  private EmailService emailService;

  @Test
  void testSendTemplatedEmails_sendsCorrectEmails() throws CannotSendEmailException {
    // Act
    processService.sendTemplatedEmails();

    // Assert
    ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
    ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, Object>> modelCaptor = ArgumentCaptor.forClass(Map.class);

    verify(emailService, times(3)).send(emailCaptor.capture(), templateCaptor.capture(), modelCaptor.capture());

    List<Email> sentEmails = emailCaptor.getAllValues();
    List<Map<String, Object>> models = modelCaptor.getAllValues();

    assertThat(sentEmails).hasSize(3);
    assertThat(templateCaptor.getValue()).isEqualTo("invitation_template.ftl");

    assertThat(models.get(0).get("name")).isEqualTo("Max Mustermann");
    assertThat(models.get(0).get("email")).isEqualTo("max@beispiel.de");

    assertThat(models.get(1).get("name")).isEqualTo("Erika Musterfrau");
    assertThat(models.get(1).get("email")).isEqualTo("erika@beispiel.de");

    assertThat(models.get(2).get("name")).isEqualTo("Hans Beispiel");
    assertThat(models.get(2).get("email")).isEqualTo("hans@example.com");
  }
}
