package de.nosetr.mail.api;

import de.nosetr.mail.service.ProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class MailController {

  private final ProcessService processService;

  @Operation(
      summary = "Send emails with optional template, CSV file, and subject",
      description = "Send emails to recipients from the specified CSV file using the specified template and subject.",
      responses = {
          @ApiResponse(responseCode = "200", description = "Emails sent successfully",
              content = @Content(schema = @Schema(type = "string"))),
          @ApiResponse(responseCode = "500", description = "Error sending emails",
              content = @Content(schema = @Schema(type = "string")))
      }
  )
  @PostMapping("/send")
  public ResponseEntity<String> sendEmails(
      @Parameter(description = "Optional template file name without the '.ftl' extension, e.g. 'invitation'")
      @RequestParam(required = false) String template,

      @Parameter(description = "Optional CSV file name without the '.csv' extension, e.g. 'customers'")
      @RequestParam(required = false) String csvFile,

      @Parameter(description = "Optional email subject, overrides default subject")
      @RequestParam(required = false) String subject
  ) {
    try {
      processService.sendTemplatedEmails(template, csvFile, subject);
      return ResponseEntity.ok("Emails sent successfully.");
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error while sending emails: " + e.getMessage());
    }
  }
}
