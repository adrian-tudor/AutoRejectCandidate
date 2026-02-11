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

    private EmailForm createDefaultForm() {
        EmailForm form = new EmailForm();
        form.setFromUser("Hiring Team");
        form.setSubject("Update regarding your application for {role}");
        form.setBody("Hi {name},\n\nThank you for the time you invested in applying for the {role} position. " +
                "After carefully reviewing your profile, we have decided to move forward with other candidates at this time.\n\n" +
                "We appreciate your interest and wish you the best of luck in your search!");
        return form;
    }

    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("emailForm", createDefaultForm());
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
            model.addAttribute("message", "Email sent successfully!");
            model.addAttribute("emailForm", createDefaultForm());
        } catch (Exception e) {
            model.addAttribute("error", "Technical error: " + e.getMessage());
        }

        return "email-form";
    }

    @PostMapping("/bulk-reject-xlsx")
    public String bulkRejectXlsx(@Valid @ModelAttribute("emailForm") EmailForm emailForm,
                                 BindingResult bindingResult,
                                 @RequestParam("file") MultipartFile file,
                                 Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Please ensure the Sender, Subject, and Message fields are not empty.");
            return "email-form";
        }

        if (file.isEmpty()) {
            model.addAttribute("error", "Please select an Excel file.");
            return "email-form";
        }

        try {
            List<EmailForm> rejectionList = spreadsheetService.processSpreadsheet(
                    file,
                    emailForm.getSubject(),
                    emailForm.getBody()
            );

            rejectionList.forEach(email -> {
                try {
                    emailService.sendEmail(emailForm.getFromUser(), email.getTo(), email.getSubject(), email.getBody());
                } catch (Exception e) {
                    System.err.println("Failed to send to: " + email.getTo());
                }
            });

            model.addAttribute("message", "Successfully processed " + rejectionList.size() + " candidates.");
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
        }

        return "email-form";
    }

    @PostMapping("/bulk-reject-text")
    public String bulkRejectText(@Valid @ModelAttribute("emailForm") EmailForm emailForm,
                                 BindingResult bindingResult,
                                 Model model) {

        // We validate that they didn't clear the subject/body for the text list too
        if (bindingResult.hasFieldErrors("fromUser") || bindingResult.hasFieldErrors("subject") || bindingResult.hasFieldErrors("body")) {
            model.addAttribute("error", "Please fill in the template fields.");
            return "email-form";
        }

        List<String> cleanedEmails = spreadsheetService.sanitizeEmailList(emailForm.getBulkEmails());

        if (cleanedEmails.isEmpty()) {
            model.addAttribute("error", "No valid email addresses found in the list.");
            return "email-form";
        }

        cleanedEmails.forEach(email -> {
            try {
                emailService.sendEmail(
                        emailForm.getFromUser(),
                        email,
                        emailForm.getSubject(),
                        emailForm.getBody()
                );
            } catch (Exception e) {
                System.err.println("Failed to send to: " + email);
            }
        });

        model.addAttribute("message", "Sent " + cleanedEmails.size() + " emails from the text list.");
        return "email-form";
    }
}