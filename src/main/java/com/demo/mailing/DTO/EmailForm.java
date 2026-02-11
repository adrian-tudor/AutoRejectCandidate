package com.demo.mailing.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EmailForm {

    @NotBlank(message = "Te rugam sa introduci numele tau.")
    private String fromUser;

    @NotBlank(message = "Adresa de email este obligatorie.", groups = SingleSend.class)
    @Email(message = "Te rugam să introduci o adresa de email valida.")
    private String to;

    @NotBlank(message = "Subiectul nu poate fi gol.")
    @Size(min = 3, message = "Subiectul trebuie sa aiba minim 3 caractere.")
    private String subject;

    @NotBlank(message = "Mesajul nu poate fi gol.")
    @Size(max = 5000, message = "Mesajul este prea lung (max 5000 caractere).")
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