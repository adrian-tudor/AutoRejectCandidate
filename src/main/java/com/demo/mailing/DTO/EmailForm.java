package com.demo.mailing.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EmailForm {

    @NotBlank(message = "Please enter your name.")
    private String fromUser;

    @NotBlank(message = "Recipient email address is required.", groups = SingleSend.class)
    @Email(message = "Please enter a valid email address.")
    private String to;

    @NotBlank(message = "Subject cannot be empty.")
    @Size(min = 3, message = "The subject must be at least 3 characters long.")
    private String subject;

    @NotBlank(message = "Message body cannot be empty.")
    @Size(max = 5000, message = "The message is too long (maximum 5000 characters).")
    private String body;

    private String bulkEmails;

    public String getFromUser() { return fromUser; }
    public void setFromUser(String fromUser) { this.fromUser = fromUser; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getBulkEmails() { return bulkEmails; }
    public void setBulkEmails(String bulkEmails) { this.bulkEmails = bulkEmails; }

    public interface SingleSend {}
}