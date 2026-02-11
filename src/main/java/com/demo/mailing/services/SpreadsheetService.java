package com.demo.mailing.services;

import com.demo.mailing.DTO.EmailForm;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

@Service
public class SpreadsheetService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public List<EmailForm> processSpreadsheet(MultipartFile file, String customSubject, String customBody) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new Exception("The spreadsheet is empty or missing a header row.");
            }

            Map<String, Integer> columnMap = getColumnMapping(headerRow);

            if (!columnMap.containsKey("email") || !columnMap.containsKey("status")) {
                throw new Exception("Missing required columns. Please ensure 'Email' and 'Status' headers exist.");
            }

            return StreamSupport.stream(sheet.spliterator(), false)
                    .skip(1)
                    .filter(row -> isValidRow(row, columnMap.get("email")))
                    .filter(row -> "Reject".equalsIgnoreCase(formatter.formatCellValue(row.getCell(columnMap.get("status")))))
                    .map(row -> convertToEmailForm(row, formatter, customSubject, customBody, columnMap))
                    .toList();
        }
    }

    private Map<String, Integer> getColumnMapping(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (Cell cell : headerRow) {
            String header = cell.getStringCellValue().trim().toLowerCase();
            if (header.contains("name")) map.put("name", cell.getColumnIndex());
            else if (header.contains("email")) map.put("email", cell.getColumnIndex());
            else if (header.contains("role")) map.put("role", cell.getColumnIndex());
            else if (header.contains("status")) map.put("status", cell.getColumnIndex());
        }
        return map;
    }

    private boolean isValidRow(Row row, int emailIdx) {
        if (row == null) return false;
        Cell emailCell = row.getCell(emailIdx);
        return emailCell != null && !emailCell.toString().trim().isEmpty();
    }

    private EmailForm convertToEmailForm(Row row, DataFormatter formatter, String subjectTemplate, String bodyTemplate, Map<String, Integer> columnMap) {
        String name = columnMap.containsKey("name") ? formatter.formatCellValue(row.getCell(columnMap.get("name"))) : "Candidate";
        String email = formatter.formatCellValue(row.getCell(columnMap.get("email")));
        String role = columnMap.containsKey("role") ? formatter.formatCellValue(row.getCell(columnMap.get("role"))) : "the position";

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