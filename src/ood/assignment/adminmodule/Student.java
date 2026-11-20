package ood.assignment.adminmodule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** One existing credentials.txt student row: ID/email, username, password, role, optional flags. */
public class Student extends Person {
    private final String password;
    private final String role;
    private final List<String> flags;

    public Student(String idOrEmail, String username, String password) {
        this(idOrEmail, username, password, "student", Collections.emptyList());
    }

    public Student(String idOrEmail, String username, String password, String role, List<String> flags) {
        super(idOrEmail, username);
        this.password = checked(password, "Password");
        this.role = checked(role, "Role");
        if (!"student".equalsIgnoreCase(this.role)) {
            throw new IllegalArgumentException("Only student credentials can be managed here.");
        }
        List<String> copy = new ArrayList<>();
        for (String flag : flags) copy.add(checked(flag, "Account flag"));
        this.flags = Collections.unmodifiableList(copy);
        checked(getId(), "Account ID / Email");
        checked(getName(), "Username");
    }

    private static String checked(String value, String field) {
        String v = checkField(value, field);
        if (v.contains(",")) throw new IllegalArgumentException(field + " cannot contain a comma.");
        return v;
    }
    public String getStudentId() { return getId(); } // first credentials.txt field: email or ID
    public String getUsername() { return getName(); }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public List<String> getFlags() { return flags; }
    public String getStatusDisplay() { return flags.isEmpty() ? "" : String.join(" / ", flags); }

    @Override public String toFileLine() {
        StringBuilder row = new StringBuilder(getId()).append(", ")
                .append(getUsername()).append(", ")
                .append(password).append(", ").append(role);
        for (String flag : flags) row.append(", ").append(flag);
        return row.toString();
    }
}
