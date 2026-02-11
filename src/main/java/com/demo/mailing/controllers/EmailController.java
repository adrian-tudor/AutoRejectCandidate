package com.demo.mailing.controllers;

import com.demo.mailing.DTO.EmailForm;
import com.demo.mailing.services.EmailService;
import com.demo.mailing.services.SpreadsheetService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class EmailController {

    private final EmailService emailService;
    private final SpreadsheetService spreadsheetService;

    public EmailController(EmailService emailService, SpreadsheetService spreadsheetService) {
        this.emailService = emailService;
        this.spreadsheetService = spreadsheetService;
    }

    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("emailForm", new EmailForm());
        return "email-form";
    }

    @PostMapping("/send-email")
    public String send(@Valid @ModelAttribute("emailForm") EmailForm emailForm,
                       BindingResult bindingResult,
                       Model model) {

        if (bindingResult.hasErrors()) {
            return "email-form";
        }

        try {
            emailService.sendEmail(
                    emailForm.getFromUser(),
                    emailForm.getTo(),
                    emailForm.getSubject(),
                    emailForm.getBody()
            );
            model.addAttribute("message", "Emailul a fost trimis cu succes!");
            model.addAttribute("emailForm", new EmailForm());
        } catch (Exception e) {
            model.addAttribute("error", "Eroare tehnica: " + e.getMessage());
        }

        return "email-form";
    }

    @PostMapping("/bulk-reject-xlsx")
    public String bulkRejectXlsx(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Te rugam sa selectezi un fisier Excel.");
            return "email-form";
        }

        try {
            List<EmailForm> rejectionList = spreadsheetService.processSpreadsheet(file);

            // Stream through the generated forms and trigger the SendGrid service
            rejectionList.forEach(email -> {
                try {
                    emailService.sendEmail(email.getFromUser(), email.getTo(), email.getSubject(), email.getBody());
                } catch (Exception e) {
                    System.err.println("Failed to send to: " + email.getTo());
                }
            });

            model.addAttribute("message", "Procesat cu succes " + rejectionList.size() + " candidati din Excel.");
        } catch (Exception e) {
            model.addAttribute("error", "Eroare la procesarea fisierului: " + e.getMessage());
        }

        model.addAttribute("emailForm", new EmailForm());
        return "email-form";
    }

    @PostMapping("/bulk-reject-text")
    public String bulkRejectText(@ModelAttribute("emailForm") EmailForm emailForm, Model model) {
        String rawEmails = emailForm.getBulkEmails(); // Assuming you added this field to DTO

        List<String> cleanedEmails = spreadsheetService.sanitizeEmailList(rawEmails);

        if (cleanedEmails.isEmpty()) {
            model.addAttribute("error", "Nu am gasit adrese de email valide in lista.");
            return "email-form";
        }

        cleanedEmails.forEach(email -> {
            try {
                emailService.sendEmail("HR Department", email, "Update Aplicatie", "Multumim pentru interes...");
            } catch (Exception e) {
            }
        });

        model.addAttribute("message", "Trimis " + cleanedEmails.size() + " emailuri din lista text.");
        return "email-form";
    }
}