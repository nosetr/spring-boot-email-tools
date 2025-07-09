package de.nosetr.mail.service;

import de.nosetr.mail.config.MailFromProperties;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;

import jakarta.mail.Address;
import jakarta.mail.internet.InternetAddress;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
    "MAIL_HOST=smtp.test.local",
    "MAIL_PORT=587",
    "MAIL_USERNAME=test",
    "MAIL_PASSWORD=test",
    "MAIL_FROM_EMAIL=test@example.com",
    "MAIL_FROM_NAME=Test Absender",
    "MAIL_SUBJECT=Test-Einladung"
})
class ProcessServiceIntegrationTest {

  @Autowired
  private ProcessService processService;

  @Autowired
  private MailFromProperties mailFromProperties;

  @MockBean
  private JavaMailSender javaMailSender;

  @Test
  void testSendTemplatedEmails_sendsCorrectEmails() throws Exception {
    // Arrange
    MimeMessage mockMessage = mock(MimeMessage.class);
    when(javaMailSender.createMimeMessage()).thenReturn(mockMessage);

    String templateName = "default_template";
    String csvFile = "default_recipients";
    String subject = "Test-Einladung";

    // Act
    processService.sendTemplatedEmails(templateName, csvFile, subject);

    // Assert
    // Verify send was called 3 times
    verify(javaMailSender, times(3)).send(mockMessage);

    // Optionally capture and verify addresses set in the message
    ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
    verify(javaMailSender, times(3)).send(messageCaptor.capture());

    List<MimeMessage> sentMessages = messageCaptor.getAllValues();
    assertThat(sentMessages).hasSize(3);

    // Spot-check one of the messages
    MimeMessage firstMessage = sentMessages.getFirst();
    verify(firstMessage, atLeastOnce()).setSubject(subject, "UTF-8");
    verify(firstMessage, atLeastOnce()).setFrom(any(InternetAddress.class));
    verify(firstMessage, atLeastOnce()).setRecipient(eq(MimeMessage.RecipientType.TO), any(Address.class));
  }
}
