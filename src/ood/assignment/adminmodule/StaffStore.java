package ood.assignment.adminmodule;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.List;

/** Shared text-file storage for the Administrator and Manager screens. */
public class StaffStore {
    private static final Path DATA_FOLDER = Paths.get(System.getProperty("sdams.data.dir", "."));

    public static final String ADMINISTRATOR = "Administrator";
    public static final String INSTRUCTOR = "Instructor";
    public static final String FACILITIES_OFFICER = "Facilities Officer";

    private Path pathFor(String role) {
        switch (role) {
            case ADMINISTRATOR: return DATA_FOLDER.resolve("administrators.txt");
            case INSTRUCTOR: return DATA_FOLDER.resolve("instructors.txt");
            case FACILITIES_OFFICER: return DATA_FOLDER.resolve("facilities_officers.txt");
            default: throw new IllegalArgumentException("Unknown staff role: " + role);
        }
    }

    public synchronized List<Staff> readByRole(String role) throws IOException {
        Path file = pathFor(role);
        List<Staff> records = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        if (!Files.exists(file)) return records;

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] columns = line.split("\\|", -1);
            if (columns.length != 3) {
                throw new IOException(file + " line " + (i + 1) + ": expected three pipe-delimited fields");
            }
            Staff person;
            try {
                // The Manager module stores administrators as ID|Name|Phone.
                // For staff reporting, derive the role from this file, never from Phone.
                if (ADMINISTRATOR.equals(role)) {
                    person = new Staff(columns[0], columns[1], ADMINISTRATOR);
                } else {
                    person = new Staff(columns[0], columns[1], columns[2]);
                }
            } catch (IllegalArgumentException e) {
                throw new IOException(file + " line " + (i + 1) + ": " + e.getMessage(), e);
            }
            if (!role.equals(person.getRole())) {
                throw new IOException(file + " line " + (i + 1) + ": role does not match file");
            }
            if (!ids.add(person.getStaffId().toLowerCase(Locale.ROOT))) {
                throw new IOException(file + " line " + (i + 1) + ": duplicate Staff ID");
            }
            records.add(person);
        }
        return records;
    }

    public synchronized List<Staff> readAll() throws IOException {
        List<Staff> all = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (String role : Arrays.asList(ADMINISTRATOR, INSTRUCTOR, FACILITIES_OFFICER)) {
            for (Staff staff : readByRole(role)) {
                if (!ids.add(staff.getStaffId().toLowerCase(Locale.ROOT))) {
                    throw new IOException("Duplicate Staff ID across staff role files: " + staff.getStaffId());
                }
                all.add(staff);
            }
        }
        return all;
    }

    private void saveRole(String role, List<Staff> records) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Staff staff : records) lines.add(staff.toFileLine());
        TxtFileWriter.write(pathFor(role), lines);
    }

    /** Only the Administrator's two managed staff roles can be changed here. */
    private void checkManagedRole(String role) {
        if (!INSTRUCTOR.equals(role) && !FACILITIES_OFFICER.equals(role)) {
            throw new IllegalArgumentException("Administrator may manage only Instructor / Facilities Officer.");
        }
    }

    public synchronized void addStaff(Staff staff) throws IOException {
        checkManagedRole(staff.getRole());
        for (Staff existing : readAll()) {
            if (existing.getStaffId().equalsIgnoreCase(staff.getStaffId())) {
                throw new IllegalArgumentException("This Staff ID already exists.");
            }
        }
        List<Staff> records = readByRole(staff.getRole());
        records.add(staff);
        saveRole(staff.getRole(), records);
    }

    public synchronized void updateStaff(String role, String staffId, String newName) throws IOException {
        checkManagedRole(role);
        List<Staff> records = readByRole(role);
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getStaffId().equalsIgnoreCase(staffId)) {
                records.set(i, new Staff(records.get(i).getStaffId(), newName, role));
                saveRole(role, records);
                return;
            }
        }
        throw new IllegalArgumentException("Staff ID not found.");
    }

    public synchronized void deleteStaff(String role, String staffId) throws IOException {
        checkManagedRole(role);
        List<Staff> records = readByRole(role);
        boolean removed = records.removeIf(s -> s.getStaffId().equalsIgnoreCase(staffId));
        if (!removed) throw new IllegalArgumentException("Staff ID not found.");
        saveRole(role, records);
    }
}
