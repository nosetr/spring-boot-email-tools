package de.nosetr.mail.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.mail.properties.mail.from")
@Component
@Data
public class MailFromProperties {
  private String email;
  private String name;
  private String subject;
}

