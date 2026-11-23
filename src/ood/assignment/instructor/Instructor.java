/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ood.assignment.instructor;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import ood.assignment.Users;
import ood.assignment.WindowNavigation;
/**
 *
 * @author kendrickyonatan2gmail.com
 */
public class Instructor extends Users {
    private static final String REPORT_FILE = "facility-reports.txt";
    private final DefaultTableModel schedule = readOnlyModel(new Object[0][],
            new String[]{"Class ID", "Class Name", "Schedule", "Capacity", "Enrolled", "Price", "Status"});
    private final DefaultTableModel reports = readOnlyModel(new Object[0][],
            new String[]{"Report ID", "Location", "Description", "Status"});

    public Instructor(String email, String username, String password, String role) {
        super(email, username, password, role);
    }

    @Override
    public void userMenu() {
        JFrame menu = new JFrame("Instructor Menu");
        menu.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        menu.add(new JLabel("Welcome, " + getUsername(), SwingConstants.CENTER),
                BorderLayout.NORTH);
        JPanel buttons = new JPanel(new GridLayout(3, 1, 8, 8));
        buttons.add(button("Manage Class Schedule", () -> showClassSchedule(menu)));
        buttons.add(button("Report Facility Problem", () -> showFacilityReports(menu)));
        buttons.add(button("View Instructor Ratings", () -> showInstructorRatings(menu)));
        menu.add(buttons, BorderLayout.CENTER);
        showWindow(menu, navigationParent, 400, 240);
    }

    private void showClassSchedule(JFrame parent) {
        JFrame window = new JFrame("Manage Class Schedule");
        reloadSchedule(window);
        JTable table = new JTable(schedule);
        JPanel actions = new JPanel();
        actions.add(button("Add Class", () -> editClass(window, -1)));
        actions.add(button("Update Class", () -> {
            int row = selectedModelRow(window, table);
            if (row >= 0) {
                editClass(window, row);
            }
        }));
        actions.add(button("Cancel Class", () -> {
            int row = selectedModelRow(window, table);
            if (row >= 0 && "Available".equals(schedule.getValueAt(row, 6))
                    && confirm(window, "Cancel class " + schedule.getValueAt(row, 0) + "?")) {
                String id = value(schedule, row, 0);
                if (changeRecord(window, CLASS_FILE, id, 2, fields -> {
                    String[] updated = java.util.Arrays.copyOf(fields, Math.max(8, fields.length));
                    updated[7] = "Cancelled";
                    return String.join(", ", updated);
                })) {
                    reloadSchedule(window);
                }
            }
        }));
        window.add(actions, BorderLayout.SOUTH);
        showTable(window, parent, table, schedule, "Search classes");
    }

    private void editClass(JFrame parent, int row) {
        JTextField id = new JTextField(value(schedule, row, 0), 18);
        id.setEditable(row < 0);
        JTextField name = new JTextField(value(schedule, row, 1), 18);
        JTextField dateTime = new JTextField(value(schedule, row, 2), 18);
        JTextField capacity = new JTextField(value(schedule, row, 3), 18);
        JTextField price = new JTextField(value(schedule, row, 5), 18);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Class ID"));
        form.add(id);
        form.add(new JLabel("Class Name"));
        form.add(name);
        form.add(new JLabel("Schedule (YYYY-MM-DD HH:mm)"));
        form.add(dateTime);
        form.add(new JLabel("Capacity"));
        form.add(capacity);
        form.add(new JLabel("Price (RM)"));
        form.add(price);

        while (accept(parent, form, row < 0 ? "Add Class" : "Update Class")) {
            String classId = id.getText().trim();
            String className = name.getText().trim();
            String slot = dateTime.getText().trim();
            String seats = capacity.getText().trim();
            String fee = price.getText().trim();
            try {
                if (classId.isEmpty() || className.isEmpty() || classId.contains(",")
                        || className.contains(",")) {
                    throw new IllegalArgumentException("Enter a class ID and name without commas.");
                }
                if (!slot.equals(value(schedule, row, 2))) {
                    if (!slot.matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}")) {
                        throw new IllegalArgumentException("Use YYYY-MM-DD HH:mm for the schedule.");
                    }
                    LocalDate.parse(slot.substring(0, 10));
                    LocalTime.parse(slot.substring(11));
                } else if (row < 0) {
                    throw new IllegalArgumentException("Enter the schedule.");
                }
                int seatCount = Integer.parseInt(seats);
                if (seatCount < 1 || !fee.matches("[0-9]+(?:[.][0-9]{1,2})?")
                        || !Double.isFinite(new BigDecimal(fee).doubleValue())) {
                    throw new IllegalArgumentException("Enter a positive capacity and a valid price.");
                }
                if (row < 0) {
                    appendRecord(CLASS_FILE, classId, String.join(", ", classId, className,
                            getUsername(), slot, seats, "0", fee));
                } else if (!changeRecord(parent, CLASS_FILE, classId, 2, fields -> {
                    if (seatCount < Integer.parseInt(fields[5])) {
                        throw new IllegalArgumentException("Capacity cannot be below current enrollment.");
                    }
                    fields[1] = className;
                    fields[3] = slot;
                    fields[4] = seats;
                    fields[6] = fee;
                    return String.join(", ", fields);
                })) {
                    continue;
                }
                reloadSchedule(parent);
                return;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(parent, "Enter a valid whole-number capacity.");
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(parent, "Enter a valid date and time.");
            } catch (IllegalArgumentException | IOException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage());
            }
        }
    }

    private void reloadSchedule(JFrame parent) {
        try {
            List<String> lines = readRecords(CLASS_FILE);
            schedule.setRowCount(0);
            for (String line : lines) {
                String[] fields = line.split(", ", -1);
                if (fields.length >= 7 && getUsername().equals(fields[2])) {
                    schedule.addRow(new Object[]{fields[0], fields[1], fields[3], fields[4],
                        fields[5], fields[6], isClassCancelled(fields) ? "Cancelled" : "Available"});
                }
            }
        } catch (IOException ex) {
            showFileError(parent, CLASS_FILE, ex);
        }
    }

    private void showFacilityReports(JFrame parent) {
        JFrame window = new JFrame("Facility Problems");
        reloadReports(window);
        JTable table = new JTable(reports);
        JPanel actions = new JPanel();
        actions.add(button("Add Report", () -> editReport(window, -1)));
        actions.add(button("View Report", () -> {
            int row = selectedModelRow(window, table);
            if (row >= 0) {
                JTextArea description = descriptionArea(value(reports, row, 2));
                description.setEditable(false);
                JPanel detail = new JPanel(new BorderLayout(0, 8));
                detail.add(new JLabel(value(reports, row, 1)), BorderLayout.NORTH);
                detail.add(new JScrollPane(description), BorderLayout.CENTER);
                JOptionPane.showMessageDialog(window, detail, "Facility Problem",
                        JOptionPane.PLAIN_MESSAGE);
            }
        }));
        actions.add(button("Update Report", () -> {
            int row = selectedModelRow(window, table);
            if (row >= 0) {
                editReport(window, row);
            }
        }));
        actions.add(button("Delete Report", () -> {
            int row = selectedModelRow(window, table);
            if (row >= 0 && confirm(window, "Delete the selected facility report?")) {
                if (changeRecord(window, REPORT_FILE, value(reports, row, 0), 1,
                        fields -> null)) {
                    reloadReports(window);
                }
            }
        }));
        window.add(actions, BorderLayout.SOUTH);
        showTable(window, parent, table, reports, "Search reports");
    }

    private void editReport(JFrame parent, int row) {
        JTextField location = new JTextField(value(reports, row, 1), 25);
        JTextArea description = descriptionArea(value(reports, row, 2));
        JPanel form = new JPanel(new BorderLayout(0, 8));
        JPanel locationPanel = new JPanel(new BorderLayout(8, 0));
        locationPanel.add(new JLabel("Location"), BorderLayout.WEST);
        locationPanel.add(location, BorderLayout.CENTER);
        form.add(locationPanel, BorderLayout.NORTH);
        JPanel descriptionPanel = new JPanel(new BorderLayout(0, 4));
        descriptionPanel.add(new JLabel("Describe the problem"), BorderLayout.NORTH);
        descriptionPanel.add(new JScrollPane(description), BorderLayout.CENTER);
        form.add(descriptionPanel, BorderLayout.CENTER);

        while (accept(parent, form, row < 0 ? "Add Facility Report" : "Update Facility Report")) {
            String reportLocation = location.getText().trim();
            String reportDescription = description.getText().trim();
            if (reportLocation.isEmpty() || reportDescription.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Enter a location and a description.");
                continue;
            }
            String data = encode(reportLocation) + ", " + encode(reportDescription);
            try {
                if (row < 0) {
                    String id = UUID.randomUUID().toString();
                    appendRecord(REPORT_FILE, id, id + ", " + getUsername() + ", " + data + ", Pending");
                } else if (!changeRecord(parent, REPORT_FILE, value(reports, row, 0), 1,
                        fields -> {
                            fields[2] = encode(reportLocation);
                            fields[3] = encode(reportDescription);
                            return String.join(", ", fields);
                        })) {
                    continue;
                }
                reloadReports(parent);
                return;
            } catch (IOException ex) {
                showFileError(parent, REPORT_FILE, ex);
            }
        }
    }

    private void reloadReports(JFrame parent) {
        try {
            List<String> lines = readRecords(REPORT_FILE);
            reports.setRowCount(0);
            for (String line : lines) {
                String[] fields = line.split(", ", -1);
                if (fields.length >= 4 && getUsername().equals(fields[1])) {
                    String status = fields.length < 5 || fields[4].isBlank() ? "Pending" : fields[4];
                    reports.addRow(new Object[]{fields[0], decode(fields[2]), decode(fields[3]), status});
                }
            }
        } catch (IOException | IllegalArgumentException ex) {
            showFileError(parent, REPORT_FILE, ex);
        }
    }

    private void showInstructorRatings(JFrame parent) {
        JFrame window = new JFrame("Instructor Ratings");
        DefaultTableModel model = readOnlyModel(new Object[0][],
                new String[]{"Class ID", "Score / 5", "Comment", "Date"});
        try {
            for (String line : readRecords(RATING_FILE)) {
                String[] fields = line.split(", ", 6);
                if (fields.length == 6 && getUsername().equals(fields[2])) {
                    int dateSeparator = fields[5].lastIndexOf(", ");
                    if (dateSeparator >= 0) {
                        model.addRow(new Object[]{fields[3], fields[4],
                            fields[5].substring(0, dateSeparator),
                            fields[5].substring(dateSeparator + 2)});
                    }
                }
            }
        } catch (IOException ex) {
            showFileError(parent, RATING_FILE, ex);
        }
        showTable(window, parent, new JTable(model), model, "Search ratings");
    }

    private static String encode(String text) {
        return java.util.Base64.getEncoder().encodeToString(
                text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static String decode(String text) {
        return new String(java.util.Base64.getDecoder().decode(text),
                java.nio.charset.StandardCharsets.UTF_8);
    }

    private static List<String> readRecords(String file) throws IOException {
        try {
            return new ArrayList<>(Files.readAllLines(Path.of(file)));
        } catch (NoSuchFileException ex) {
            return new ArrayList<>();
        }
    }

    private static void appendRecord(String file, String id, String record) throws IOException {
        List<String> lines = readRecords(file);
        for (String line : lines) {
            if (line.split(", ", 2)[0].equalsIgnoreCase(id)) {
                throw new IOException("That ID already exists.");
            }
        }
        lines.add(record);
        saveRecords(file, lines);
    }

    private boolean changeRecord(JFrame parent, String file, String id, int ownerColumn,
            java.util.function.Function<String[], String> change) {
        try {
            List<String> lines = readRecords(file);
            int minimumFields = ownerColumn == 2 ? 7 : 4;
            for (int index = 0; index < lines.size(); index++) {
                String[] fields = lines.get(index).split(", ", -1);
                if (fields.length >= minimumFields && fields[0].equals(id)
                        && getUsername().equals(fields[ownerColumn])) {
                    String replacement = change.apply(fields);
                    if (replacement == null) {
                        lines.remove(index);
                    } else {
                        lines.set(index, replacement);
                    }
                    saveRecords(file, lines);
                    return true;
                }
            }
            JOptionPane.showMessageDialog(parent, "The selected record no longer exists.");
        } catch (IOException ex) {
            showFileError(parent, file, ex);
        }
        return false;
    }

    private static void saveRecords(String file, List<String> lines) throws IOException {
        Path target = Path.of(file).toAbsolutePath();
        Path temporary = Files.createTempFile(target.getParent(), file, ".tmp");
        try {
            Files.write(temporary, lines);
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void showFileError(JFrame parent, String file, Exception ex) {
        JOptionPane.showMessageDialog(parent, file + " — " + ex.getMessage(),
                "Data file error", JOptionPane.ERROR_MESSAGE);
    }

    private void showTable(JFrame window, JFrame parent, JTable table,
            DefaultTableModel model, String searchLabel) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        sorter.setSortsOnUpdates(true);
        table.setRowSorter(sorter);

        JTextField queryField = new JTextField(18);
        JButton search = button("Search", () -> {
            String query = queryField.getText().trim();
            sorter.setRowFilter(query.isEmpty() ? null
                    : RowFilter.regexFilter("(?iu)" + Pattern.quote(query)));
        });
        queryField.addActionListener(event -> search.doClick());
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel(searchLabel));
        searchPanel.add(queryField);
        searchPanel.add(search);
        window.add(searchPanel, BorderLayout.NORTH);
        window.add(new JScrollPane(table), BorderLayout.CENTER);
        showWindow(window, parent, 650, 400);
    }

    private int selectedModelRow(JFrame parent, JTable table) {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(parent, "Select a row first.");
            return -1;
        }
        return table.convertRowIndexToModel(selected);
    }

    private static String value(DefaultTableModel model, int row, int column) {
        return row < 0 ? "" : model.getValueAt(row, column).toString();
    }

    private static boolean accept(JFrame parent, JPanel form, String title) {
        return JOptionPane.showConfirmDialog(parent, form, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    private static boolean confirm(JFrame parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, parent.getTitle(),
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private static JTextArea descriptionArea(String text) {
        JTextArea area = new JTextArea(text, 6, 25);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private static JButton button(String label, Runnable action) {
        JButton button = new JButton(label);
        button.addActionListener(event -> action.run());
        return button;
    }

    private static void showWindow(JFrame window, JFrame parent, int width, int height) {
        window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        window.setSize(width, height);
        window.setLocationRelativeTo(parent);
        WindowNavigation.show(window, parent);
    }

    private static DefaultTableModel readOnlyModel(Object[][] rows, String[] columns) {
        return new DefaultTableModel(rows, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private boolean isClassCancelled(String[] fields) {
    return fields.length > 7
            && "Cancelled".equalsIgnoreCase(fields[7].trim());
    }
}