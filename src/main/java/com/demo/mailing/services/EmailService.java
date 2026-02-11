package com.demo.mailing.services;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${spring.sendgrid.api-key}")
    private String apiKey;

    @Value("${app.sendgrid.from-email}")
    private String myFromEmail;

    public void sendEmail(String fromUser, String toEmail, String subject, String body) throws IOException {


        Email from = new Email(myFromEmail, fromUser);

        Email to = new Email(toEmail);

        String finalBodyText = "Message from: " + fromUser + "\n\n" + body;
        Content content = new Content("text/plain", finalBodyText);

        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);

        if (response.getStatusCode() >= 400) {
            throw new IOException("SendGrid Error (" + response.getStatusCode() + "): " + response.getBody());
        }
    }
}