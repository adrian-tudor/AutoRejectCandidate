package com.demo.mailing.services;

import com.demo.mailing.DTO.EmailForm;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

@Service
public class SpreadsheetService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");


    public List<EmailForm> processSpreadsheet(MultipartFile file, String customSubject, String customBody) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            return StreamSupport.stream(sheet.spliterator(), false)
                    .skip(1)
                    .filter(this::isValidRow)
                    .filter(row -> "Reject".equalsIgnoreCase(formatter.formatCellValue(row.getCell(3))))
                    .map(row -> convertToEmailForm(row, formatter, customSubject, customBody))
                    .toList();
        }
    }

    private boolean isValidRow(Row row) {
        return row != null &&
                row.getCell(1) != null &&
                !row.getCell(1).toString().trim().isEmpty();
    }

    private EmailForm convertToEmailForm(Row row, DataFormatter formatter, String subjectTemplate, String bodyTemplate) {
        String name = formatter.formatCellValue(row.getCell(0));
        String email = formatter.formatCellValue(row.getCell(1));
        String role = formatter.formatCellValue(row.getCell(2));

        EmailForm form = new EmailForm();
        form.setTo(email);
        form.setFromUser("HR Department");

        String finalSubject = subjectTemplate.replace("{role}", role).replace("{name}", name);
        String finalBody = bodyTemplate.replace("{name}", name).replace("{role}", role);

        form.setSubject(finalSubject);
        form.setBody(finalBody);

        return form;
    }

    public List<String> sanitizeEmailList(String rawEmails) {
        if (rawEmails == null || rawEmails.isBlank()) return List.of();

        return Arrays.stream(rawEmails.split("[,;\\s]+"))
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .filter(email -> EMAIL_PATTERN.matcher(email).matches())
                .distinct()
                .toList();
    }
}