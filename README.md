# Spring Boot Email Service Application

This is a Spring Boot application that provides email sending functionality using templated emails and integration with SMTP servers.

## Features

- Send emails based on FreeMarker templates.
- Read recipient data from CSV files.
- Configurable SMTP mail server properties.
- Integration tests for email sending functionality.
- Supports JavaMailSender for sending MIME emails.
- (Optional) Swagger UI integration for API documentation.

## Getting Started

### Prerequisites

- Java 21
- Maven or Gradle build tool
- SMTP server credentials (for real email sending)

### Configuration

Configure your mail server properties in `application.properties` or via environment variables:

```properties
MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=your_username
MAIL_PASSWORD=your_password
MAIL_FROM_EMAIL=your_email@example.com
MAIL_FROM_NAME=Your Name
MAIL_SUBJECT=Your Default Subject
```

### Project Structure

- src/main/resources/templates — Email FreeMarker templates.

- src/main/resources/recipients-lists — CSV files with recipient data.