package ood.assignment.adminmodule;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Student CRUD against the team's credentials.txt. Other roles/flags are preserved. */
public class StudentStore {
    public static final Path FILE = Paths.get(System.getProperty("sdams.data.dir", "."), "credentials.txt");

    private List<String> raw() throws IOException {
        return Files.exists(FILE) ? Files.readAllLines(FILE, StandardCharsets.UTF_8) : new ArrayList<>();
    }
    private String[] fields(String line) { return line.split("\\s*,\\s*", -1); }
    private boolean isStudent(String[] row) {
        return row.length >= 4 && row[3].trim().equalsIgnoreCase("student");
    }
    private Student parse(String[] row, int lineNo) throws IOException {
        try {
            return new Student(row[0], row[1], row[2], row[3],
                    Arrays.asList(row).subList(4, row.length));
        } catch (IllegalArgumentException ex) {
            throw new IOException(FILE + " line " + lineNo + ": " + ex.getMessage(), ex);
        }
    }
    public synchronized List<Student> readAll() throws IOException {
        List<Student> result = new ArrayList<>();
        Set<String> usernames = new HashSet<>();
        List<String> lines = raw();
        for (int i=0;i<lines.size();i++) {
            String[] fields = fields(lines.get(i));
            if (!isStudent(fields)) continue;
            Student s = parse(fields, i+1);
            if (!usernames.add(s.getUsername().toLowerCase(Locale.ROOT))) {
                throw new IOException(FILE+" line "+(i+1)+": duplicate student username.");
            }
            result.add(s);
        }
        return result;
    }
    private void write(List<String> lines) throws IOException { TxtFileWriter.write(FILE, lines); }

    /** Existing Student module authenticates against username, not the first credential field. */
    public synchronized void add(Student student) throws IOException {
        List<String> all = raw();
        for (String line : all) {
            String[] row = fields(line);
            if (row.length >= 2) {
                if (row[1].equalsIgnoreCase(student.getUsername()))
                    throw new IllegalArgumentException("Username already exists in credentials.txt.");
                if (row[0].equalsIgnoreCase(student.getStudentId()))
                    throw new IllegalArgumentException("ID/email already exists in credentials.txt.");
            }
        }
        all.add(student.toFileLine());
        write(all);
    }
    /** Username is stable because enrollment/payment/ratings use it as their foreign key. */
    public synchronized void update(String username, Student newRecord) throws IOException {
        if (!username.equalsIgnoreCase(newRecord.getUsername())) {
            throw new IllegalArgumentException("Username cannot be changed (enrollment/payment references use it).");
        }
        List<String> all = raw();
        int index=-1;
        for (int i=0;i<all.size();i++) {
            String[] row = fields(all.get(i));
            if (isStudent(row) && row[1].equalsIgnoreCase(username)) index=i;
            else if (row.length >= 1 && row[0].equalsIgnoreCase(newRecord.getStudentId())) {
                // Different account with the new first field.
                if (!(isStudent(row) && row[1].equalsIgnoreCase(username)))
                    throw new IllegalArgumentException("ID/email already belongs to another account.");
            }
        }
        if (index < 0) throw new IllegalArgumentException("Student username not found.");
        all.set(index,newRecord.toFileLine());
        write(all);
    }
    /** Does not erase the historical payments/enrollments linked to this username. */
    public synchronized void delete(String username) throws IOException {
        List<String> all=raw();
        int index=-1;
        for(int i=0;i<all.size();i++) {
            String[] row=fields(all.get(i));
            if(isStudent(row)&&row[1].equalsIgnoreCase(username)) { index=i; break; }
        }
        if(index<0) throw new IllegalArgumentException("Student username not found.");
        all.remove(index);
        write(all);
    }
}
