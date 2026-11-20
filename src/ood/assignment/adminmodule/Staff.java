package ood.assignment.adminmodule;

/** Staff inherits common attributes from Person and provides its own TXT format. */
public class Staff extends Person {
    private final String role;

    public Staff(String staffId, String name, String role) {
        super(staffId, name);
        this.role = checkField(role, "Role");
    }

    public String getStaffId() { return getId(); }
    public String getRole() { return role; }

    @Override
    public String toFileLine() {
        return getStaffId() + "|" + getName() + "|" + role;
    }
}
