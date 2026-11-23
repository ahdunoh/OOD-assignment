package ood.assignment.FacilityOfficer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/** Reads the Instructor's report format and preserves it when updating status. */
public final class FacilityReportStore {
    public static final List<String> STATUSES = List.of("Pending", "In Progress", "Resolved");
    private final Path file;

    public record Report(String id, String instructor, String location,
            String description, String status) {}

    public FacilityReportStore(Path file) {
        this.file = file;
    }

    private List<String> readLines() throws IOException {
        try {
            return new ArrayList<>(Files.readAllLines(file, StandardCharsets.UTF_8));
        } catch (NoSuchFileException ex) {
            return new ArrayList<>();
        }
    }

    public List<Report> read() throws IOException {
        List<Report> reports = new ArrayList<>();
        for (String line : readLines()) {
            if (!line.isBlank()) {
                reports.add(parse(line));
            }
        }
        return reports;
    }

    private Report parse(String line) throws IOException {
        String[] fields = line.split(", ", -1);
        if (fields.length < 4 || fields[0].isBlank() || fields[1].isBlank()) {
            throw new IOException("A facility report has an invalid record format.");
        }
        try {
            String location = new String(Base64.getDecoder().decode(fields[2]), StandardCharsets.UTF_8);
            String description = new String(Base64.getDecoder().decode(fields[3]), StandardCharsets.UTF_8);
            String status = fields.length < 5 || fields[4].isBlank() ? "Pending" : fields[4];
            return new Report(fields[0], fields[1], location, description, status);
        } catch (IllegalArgumentException ex) {
            throw new IOException("A facility report has invalid encoded text.", ex);
        }
    }

    public synchronized void updateStatus(String id, String status) throws IOException {
        if (!STATUSES.contains(status)) {
            throw new IllegalArgumentException("Select a valid facility issue status.");
        }
        List<String> lines = readLines();
        int selected = -1;
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            if (line.isBlank()) {
                continue;
            }
            Report report = parse(line);
            if (report.id().equals(id)) {
                if (selected >= 0) {
                    throw new IOException("Duplicate report ID. Please check the report file.");
                }
                selected = index;
            }
        }
        if (selected < 0) {
            throw new IOException("This report no longer exists. Refresh the search results.");
        }
        String[] fields = lines.get(selected).split(", ", -1);
        String[] updated = java.util.Arrays.copyOf(fields, Math.max(5, fields.length));
        updated[4] = status;
        lines.set(selected, String.join(", ", updated));

        Path target = file.toAbsolutePath();
        Path temporary = Files.createTempFile(target.getParent(), "facility-reports-", ".tmp");
        try {
            Files.write(temporary, lines, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
