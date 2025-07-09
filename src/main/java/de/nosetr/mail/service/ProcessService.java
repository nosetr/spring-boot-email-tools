package de.nosetr.mail.service;

import com.google.common.collect.Lists;
import de.nosetr.mail.config.MailFromProperties;
import it.ozimov.springboot.mail.model.Email;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmail;
import it.ozimov.springboot.mail.service.EmailService;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessService {

  private static final String RECIPIENTS_CSV = "recipients.csv";
  private final EmailService emailService;
  private final MailFromProperties mailFromProperties;

  public void sendTemplatedEmails() {
    List<Recipient> recipients = loadRecipientsFromCsv();

    for (Recipient recipient : recipients) {
      try {
        final Email email = DefaultEmail.builder()
            .from(new InternetAddress(mailFromProperties.getEmail(), mailFromProperties.getName()))
            .to(Lists.newArrayList(new InternetAddress(recipient.email(), recipient.name())))
            .subject(mailFromProperties.getSubject())
            .body("") // Template body handled separately
            .encoding("UTF-8")
            .build();

        Map<String, Object> model = new HashMap<>();
        model.put("name", recipient.name());
        model.put("email", recipient.email());

        emailService.send(email, "invitation_template.ftl", model);
        log.info("Email sent to {} <{}>", recipient.name(), recipient.email());

      } catch (UnsupportedEncodingException e) {
        log.error("Encoding error for recipient {}: {}", recipient.email(), e.getMessage(), e);
      } catch (Exception e) {
        log.error("Failed to send email to {}: {}", recipient.email(), e.getMessage(), e);
      }
    }
  }

  private List<Recipient> loadRecipientsFromCsv() {
    List<Recipient> recipients = new ArrayList<>();

    CSVFormat format = CSVFormat.DEFAULT.builder()
        .setHeader("Name","Strasse","PLZ","Stadt","Telefon","E-Mail")
        .setSkipHeaderRecord(true)
        .setTrim(true)
        .build();

    try (
        Reader reader = new InputStreamReader(
            new ClassPathResource(RECIPIENTS_CSV).getInputStream(), StandardCharsets.UTF_8
        );
        CSVParser csvParser = format.parse(reader)
    ) {
      for (CSVRecord record : csvParser) {
        String name = record.get("Name");
        String email = record.get("E-Mail");

        try {
          new InternetAddress(email, true); // true = validate
          recipients.add(new Recipient(name, email));
        } catch (AddressException e) {
          log.warn("Invalid email skipped: {}", email);
        }
      }
    } catch (Exception e) {
      log.error("Error reading CSV file '{}': {}", RECIPIENTS_CSV, e.getMessage(), e);
    }

    return recipients;
  }

  private record Recipient(String name, String email) {}
}
