package ood.assignment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class AttendanceService {
    static final ZoneId ACADEMY_ZONE = ZoneId.of("Asia/Kuala_Lumpur");
    private static final Path CLASSES = Path.of("classes.txt");
    private static final Path ENROLLMENT = Path.of("enrollment.txt");
    private static final Path ATTENDANCE = Path.of("attendance.txt");
    private static final Pattern SCHEDULE = Pattern.compile(
            "([0-9]{4}-[0-9]{2}-[0-9]{2})\\s+([0-9]{1,2}:[0-9]{2})\\s*-\\s*([0-9]{1,2}:[0-9]{2})");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("H:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    enum Status {
        PRESENT("Present"), ABSENT("Absent"), LATE("Late");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    record Session(String id, String name, String instructor, String schedule,
                   String discipline, LocalDate date, LocalDateTime endsAt) {
        @Override
        public String toString() {
            return id + "  |  " + name + "  |  " + schedule;
        }

        boolean hasFinished() {
            return endsAt != null && !LocalDateTime.now(ACADEMY_ZONE).isBefore(endsAt);
        }
    }

    record Entry(String student, String classId, String className, String instructor,
                 String discipline, LocalDate date, Status status) {
        String[] fields() {
            return new String[]{student, classId, className, instructor, discipline,
                date.toString(), status.toString()};
        }
    }

    private record Key(String student, String classId) {
        Key {
            classId = classId.toUpperCase(Locale.ROOT);
        }
    }

    List<Session> sessions() throws IOException {
        List<Session> sessions = new ArrayList<>();
        for (String line : lines(CLASSES)) {
            String[] row = line.split(", ", -1);
            if (row.length < 7 || row[0].isBlank() || row[0].equalsIgnoreCase("classID")) {
                continue;
            }
            // The shared seven-field schema uses the class name as its discipline.
            sessions.add(parseSession(row[0], row[1], row[2], row[3], row[1]));
        }
        return sessions;
    }

    private Session parseSession(String id, String name, String instructor, String schedule,
                                 String discipline) {
        Matcher match = SCHEDULE.matcher(schedule.strip());
        if (match.matches()) {
            try {
                LocalDate date = LocalDate.parse(match.group(1));
                LocalTime start = LocalTime.parse(match.group(2), TIME);
                LocalTime end = LocalTime.parse(match.group(3), TIME);
                if (!end.equals(start)) {
                    LocalDate endDate = end.isBefore(start) ? date.plusDays(1) : date;
                    return new Session(id, name, instructor, schedule, discipline, date,
                            endDate.atTime(end));
                }
            } catch (DateTimeParseException ignored) {
                // Keep legacy or invalid schedules visible for read-only roster access.
            }
        }
        return new Session(id, name, instructor, schedule, discipline, null, null);
    }

    List<String> registeredStudents(String classId) throws IOException {
        TreeSet<String> students = new TreeSet<>();
        for (String line : lines(ENROLLMENT)) {
            String[] row = line.split(", ", -1);
            if (row.length >= 3 && !row[0].isBlank() && row[1].equalsIgnoreCase(classId)
                    && row[2].equalsIgnoreCase("active")) {
                students.add(row[0]);
            }
        }
        return List.copyOf(students);
    }

    List<Entry> records() throws IOException {
        return List.copyOf(readEntries().values());
    }

    boolean canRate(String student, String classId) throws IOException {
        Entry entry = readEntries().get(new Key(student, classId));
        return entry != null && entry.status() != Status.ABSENT;
    }

    void save(String classId, Map<String, Status> changes) throws IOException {
        if (changes.isEmpty()) {
            return;
        }
        synchronized (AttendanceService.class) {
            Session session = sessions().stream()
                    .filter(item -> item.id().equalsIgnoreCase(classId))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Class no longer exists."));
            if (!session.hasFinished()) {
                throw new IllegalArgumentException("Attendance can only be saved after a dated class ends.");
            }
            List<String> registered = registeredStudents(session.id());
            Map<Key, Entry> entries = readEntries();
            for (Map.Entry<String, Status> change : changes.entrySet()) {
                String student = change.getKey();
                Key key = new Key(student, session.id());
                Entry old = entries.get(key);
                if (!registered.contains(student) && old == null) {
                    throw new IllegalArgumentException(student + " is no longer registered for this class.");
                }
                Status status = change.getValue();
                Entry updated = old == null
                        ? new Entry(student, session.id(), session.name(), session.instructor(),
                                session.discipline(), session.date(), status)
                        : new Entry(old.student(), old.classId(), old.className(), old.instructor(),
                                old.discipline(), old.date(), status);
                entries.put(key, updated);
            }
            List<String> output = entries.values().stream()
                    .map(entry -> String.join(", ", entry.fields())).toList();
            Path temp = Files.createTempFile(ATTENDANCE.toAbsolutePath().getParent(), "attendance-", ".tmp");
            try {
                Files.write(temp, output);
                Files.move(temp, ATTENDANCE, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } finally {
                Files.deleteIfExists(temp);
            }
        }
    }

    private Map<Key, Entry> readEntries() throws IOException {
        Map<Key, Entry> entries = new LinkedHashMap<>();
        for (String line : lines(ATTENDANCE)) {
            String[] row = line.split(", ", -1);
            try {
                if (row.length != 7) {
                    throw new IllegalArgumentException("wrong number of fields");
                }
                Entry entry = new Entry(row[0], row[1], row[2], row[3], row[4],
                        LocalDate.parse(row[5]), Status.valueOf(row[6].toUpperCase(Locale.ROOT)));
                entries.put(new Key(entry.student(), entry.classId()), entry);
            } catch (IllegalArgumentException | DateTimeParseException ex) {
                throw new IOException("Invalid entry in attendance.txt.", ex);
            }
        }
        return entries;
    }

    private List<String> lines(Path path) throws IOException {
        try (var rows = Files.lines(path)) {
            return rows.filter(row -> !row.isBlank()).toList();
        } catch (java.nio.file.NoSuchFileException ex) {
            return List.of();
        } catch (java.io.UncheckedIOException ex) {
            throw ex.getCause();
        }
    }
}
