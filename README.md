# Candidate Rejection Tool

A modern, Spring Boot-powered web application designed for HR departments to streamline the candidate rejection process. This tool allows for single manual sends, bulk processing via Excel spreadsheets, and quick-paste email lists with dark mode support.

## Features
- Modern UI: Clean, responsive interface with a built-in Dark Mode toggle.
- Three Send Modes:
  - Single Email: For manual, personalized messages.
  - Spreadsheet Bulk: Process .xlsx files and auto-send to candidates marked as "Reject".
  - Quick List: Paste a raw list of emails (comma/semicolon separated) for fast processing.
- Placeholders: Support for {name} and {role} dynamic replacement in templates.
- SendGrid Integration: High-deliverability email sending.

---

## Getting Started

### 1. Prerequisites
- Java 17 or higher
- Maven 3.6+
- A SendGrid API Key (and a verified sender email)

### 2. Configuration
The application requires specific environment settings to communicate with SendGrid.
1. Copy the template file:
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties


   ### Spreadsheet Format

The tool automatically detects data columns based on the header names in the first row. This allows for flexible spreadsheet structures where the order of columns does not matter.

#### Column Requirements
| Header Name | Required | Purpose |
| :--- | :--- | :--- |
| **Email** | Yes | Recipient address for the rejection message. |
| **Status** | Yes | Must contain the word "Reject" (case-insensitive) to trigger sending. |
| **Name** | No | Used to replace `{name}` in your message template. Defaults to "Candidate". |
| **Role** | No | Used to replace `{role}` in your template. Defaults to "the position". |

#### Example Table
| Status | Email | Name | Role |
| :--- | :--- | :--- | :--- |
| Reject | candidate@example.com | John Doe | Java Developer |
| Selected | winner@example.com | Jane Smith | UI Designer |

*Note: Any row where the Status is not "Reject" will be ignored by the tool.*
