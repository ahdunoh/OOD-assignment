package ood.assignment.adminmodule;

/** Shared OOP base class: encapsulation, inheritance and abstraction. */
public abstract class Person {
    private final String id;
    private final String name;

    protected Person(String id, String name) {
        this.id = checkField(id, "ID");
        this.name = checkField(name, "Name");
    }

    protected static String checkField(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        String text = value.trim();
        if (text.contains("|") || text.contains("\n") || text.contains("\r")) {
            throw new IllegalArgumentException(fieldName + " cannot contain | or a line break.");
        }
        return text;
    }

    public String getId() { return id; }
    public String getName() { return name; }

    public abstract String toFileLine();
}
