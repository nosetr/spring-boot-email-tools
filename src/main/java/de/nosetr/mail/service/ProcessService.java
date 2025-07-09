package de.nosetr.mail.service;

import de.nosetr.mail.config.MailFromProperties;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import freemarker.template.Configuration;
import freemarker.template.Template;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessService {

  private static final String DEFAULT_RECIPIENTS_CSV = "default_recipients.csv";
  private static final String DEFAULT_TEMPLATE_FTL = "default_template.ftl";
  private static final String RECIPIENTS_LISTS = "recipients-lists/";

  private final JavaMailSender mailSender;
  private final Configuration freemarkerConfig;
  private final MailFromProperties mailFromProperties;

  public void sendTemplatedEmails(String templateName, String csvFile, String subject) {
    String templateToUse = (templateName == null || templateName.isBlank())
        ? DEFAULT_TEMPLATE_FTL
        : templateName + ".ftl";

    String csvToUse = RECIPIENTS_LISTS + ((csvFile == null || csvFile.isBlank())
        ? DEFAULT_RECIPIENTS_CSV
        : csvFile + ".csv");

    String subjectToUse = (subject == null || subject.isBlank())
        ? mailFromProperties.getSubject()
        : subject;

    List<Recipient> recipients = loadRecipientsFromCsv(csvToUse);

    for (Recipient recipient : recipients) {
      try {
        // FreeMarker Template laden und rendern
        Map<String, Object> model = new HashMap<>();
        model.put("name", recipient.name());
        model.put("email", recipient.email());

        Template template = freemarkerConfig.getTemplate(templateToUse);
        String htmlBody = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

        sendEmail(recipient.email(), recipient.name(), subjectToUse, htmlBody);

        log.info("Email sent to {} <{}>", recipient.name(), recipient.email());
      } catch (Exception e) {
        log.error("Failed to send email to {}: {}", recipient.email(), e.getMessage(), e);
      }
    }
  }

  private void sendEmail(String toEmail, String toName, String subject, String htmlBody) throws Exception {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    helper.setFrom(new InternetAddress(mailFromProperties.getEmail(), mailFromProperties.getName()));
    helper.setTo(new InternetAddress(toEmail, toName));
    helper.setSubject(subject);
    helper.setText(htmlBody, true); // true = HTML

    mailSender.send(message);
  }

  private List<Recipient> loadRecipientsFromCsv(String csvToUse) {
    List<Recipient> recipients = new ArrayList<>();

    CSVFormat format = CSVFormat.DEFAULT.builder()
        .setHeader("Name", "Strasse", "PLZ", "Stadt", "Telefon", "E-Mail")
        .setSkipHeaderRecord(true)
        .setTrim(true)
        .build();

    try (
        Reader reader = new InputStreamReader(
            new ClassPathResource(csvToUse).getInputStream(), StandardCharsets.UTF_8
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
      log.error("Error reading CSV file '{}': {}", csvToUse, e.getMessage(), e);
    }

    return recipients;
  }

  private record Recipient(String name, String email) {
  }
}
