package com.chatbot.integration;

import com.chatbot.model.Lead;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleSheetClient {

    @Value("${leads.csv.path:leads.csv}")
    private String csvPath;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    /**
     * Appends a lead as a new row in a local CSV file.
     * Runs asynchronously so it doesn't block the webhook response.
     *
     * CSV columns: Date,Name,Phone,City,Check-In,Check-Out,Guests,Budget,Status
     */
    @Async
    public void appendLead(Lead lead) {
        try {
            Path path = Path.of(csvPath);
            boolean fileExists = Files.exists(path);

            List<String> columns = new ArrayList<>();
            String date = LocalDateTime.now().format(FORMATTER);
            columns.add(date);
            columns.add(nullSafe(lead.getName()));
            columns.add(nullSafe(lead.getPhone()));
            columns.add(nullSafe(lead.getCity()));
            columns.add(nullSafe(lead.getCheckIn()));
            columns.add(nullSafe(lead.getCheckOut()));
            columns.add(lead.getGuests() != null ? lead.getGuests().toString() : "");
            columns.add(nullSafe(lead.getBudget()));
            columns.add(lead.getStatus().name());

            try (BufferedWriter writer = Files.newBufferedWriter(path,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {

                if (!fileExists) {
                    writer.write("Date,Name,Phone,City,Check-In,Check-Out,Guests,Budget,Status\n");
                }

                writer.write(buildCsvLine(columns));
                writer.write("\n");
            }

            log.info("Lead appended to CSV: {}", lead.getPhone());

        } catch (Exception e) {
            log.error("Failed to append lead to CSV file: {}", e.getMessage(), e);
        }
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    private String buildCsvLine(List<String> cols) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.size(); i++) {
            String c = cols.get(i);
            sb.append(escapeCsv(c));
            if (i < cols.size() - 1) sb.append(',');
        }
        return sb.toString();
    }

    private String escapeCsv(String field) {
        if (field == null) return "";
        boolean needQuotes = field.contains(",") || field.contains("\n") || field.contains("\"");
        String escaped = field.replace("\"", "\"\"");
        return needQuotes ? '"' + escaped + '"' : escaped;
    }
}
