package ood.assignment;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

final class AttendanceOfficerFrame extends JFrame {
    private final AttendanceService service = new AttendanceService();
    private final JComboBox<AttendanceService.Session> classes = new JComboBox<>();
    private final JComboBox<AttendanceService.Status> status =
            new JComboBox<>(AttendanceService.Status.values());
    private final JComboBox<String> groupBy = new JComboBox<>(new String[]{"Student", "Class"});
    private final DefaultTableModel roster = readOnlyTable("Student", "Attendance");
    private final DefaultTableModel records = readOnlyTable(
            "Student", "Class ID", "Class name", "Instructor", "Discipline", "Class date", "Status");
    private final DefaultTableModel report = readOnlyTable(
            "Student", "Total sessions", "Present", "Absent", "Late", "Attended (incl. late)");
    private final JTable rosterTable = table(roster);
    private final JLabel scheduleNote = new JLabel("Select a class to view its roster.");
    private final JLabel saveNote = new JLabel("Select a student and an attendance status.");
    private final JLabel resultsNote = new JLabel(" ");
    private final JButton saveButton = new JButton("Save selected student");
    private final JButton exportButton = new JButton("Export summary CSV");
    private final JTextField studentFilter = new JTextField(12);
    private final JTextField classFilter = new JTextField(12);
    private final JTextField instructorFilter = new JTextField(12);
    private final JTextField disciplineFilter = new JTextField(12);
    private final JTextField dateFilter = new JTextField(12);
    private final JTabbedPane dataTabs = new JTabbedPane();

    AttendanceOfficerFrame(String username) {
        super("SDAMS — Attendance Officer");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        JPanel heading = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Attendance Officer");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 21f));
        heading.add(title, BorderLayout.WEST);
        JButton logout = new JButton("Log out");
        logout.addActionListener(event -> dispose());
        JPanel account = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        account.add(new JLabel("Signed in as " + username));
        account.add(logout);
        heading.add(account, BorderLayout.EAST);
        page.add(heading, BorderLayout.NORTH);

        JTabbedPane sections = new JTabbedPane();
        sections.addTab("Class attendance", buildRosterPanel());
        sections.addTab("Records and reports", buildDataPanel());
        sections.addChangeListener(event -> {
            if (sections.getSelectedIndex() == 1) {
                refreshData();
            }
        });
        page.add(sections);
        setContentPane(page);
        setSize(1050, 570);
        setLocationRelativeTo(null);
        refreshClasses();
    }

    private JPanel buildRosterPanel() {
        JPanel panel = paddedPanel();
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.add(new JLabel("Scheduled class"));
        controls.add(classes);
        JButton refresh = new JButton("Refresh roster");
        controls.add(refresh);
        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.add(controls, BorderLayout.NORTH);
        top.add(scheduleNote, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(rosterTable));

        JPanel edit = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        edit.add(new JLabel("Attendance status"));
        edit.add(status);
        edit.add(saveButton);
        edit.add(saveNote);
        panel.add(edit, BorderLayout.SOUTH);
        classes.addActionListener(event -> refreshRoster());
        refresh.addActionListener(event -> refreshClasses());
        rosterTable.getSelectionModel().addListSelectionListener(event -> {
            int selected = rosterTable.getSelectedRow();
            if (!event.getValueIsAdjusting() && selected >= 0) {
                String previous = roster.getValueAt(rosterTable.convertRowIndexToModel(selected), 1)
                        .toString().replace(" (historical)", "");
                if (!previous.equals("Pending")) {
                    status.setSelectedItem(AttendanceService.Status.valueOf(previous.toUpperCase(Locale.ROOT)));
                } else {
                    status.setSelectedIndex(0);
                }
            }
            updateSaveButton();
        });
        saveButton.addActionListener(event -> saveSelection());
        return panel;
    }

    private JPanel buildDataPanel() {
        JPanel panel = paddedPanel();
        JPanel fields = new JPanel(new GridLayout(1, 5, 8, 0));
        fields.add(filter("Student username", studentFilter));
        fields.add(filter("Class ID or name", classFilter));
        fields.add(filter("Instructor", instructorFilter));
        fields.add(filter("Discipline", disciplineFilter));
        fields.add(filter("Date or month", dateFilter));
        dateFilter.setToolTipText("YYYY-MM-DD or YYYY-MM");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton search = new JButton("Search");
        JButton clear = new JButton("Clear filters");
        JButton refresh = new JButton("Refresh");
        actions.add(search);
        actions.add(clear);
        actions.add(refresh);
        actions.add(resultsNote);
        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.add(fields, BorderLayout.NORTH);
        top.add(actions, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);

        dataTabs.addTab("Attendance records", new JScrollPane(table(records)));
        JPanel summary = paddedPanel();
        summary.add(new JScrollPane(table(report)));
        JPanel summaryActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        summaryActions.add(new JLabel("Group by"));
        summaryActions.add(groupBy);
        summaryActions.add(exportButton);
        summary.add(summaryActions, BorderLayout.SOUTH);
        dataTabs.addTab("Attendance summary", summary);
        panel.add(dataTabs);
        search.addActionListener(event -> refreshData());
        refresh.addActionListener(event -> refreshData());
        clear.addActionListener(event -> {
            for (JTextField field : filterFields()) {
                field.setText("");
            }
            refreshData();
        });
        for (JTextField field : filterFields()) {
            field.addActionListener(event -> refreshData());
        }
        groupBy.addActionListener(event -> refreshData());
        exportButton.addActionListener(event -> exportReport());
        return panel;
    }

    private void refreshClasses() {
        AttendanceService.Session selected = (AttendanceService.Session) classes.getSelectedItem();
        try {
            List<AttendanceService.Session> items = service.sessions();
            classes.setModel(new DefaultComboBoxModel<>(items.toArray(AttendanceService.Session[]::new)));
            if (selected != null) {
                items.stream().filter(item -> item.id().equalsIgnoreCase(selected.id()))
                        .findFirst().ifPresent(classes::setSelectedItem);
            }
            refreshRoster();
        } catch (IOException ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshRoster() {
        AttendanceService.Session session = (AttendanceService.Session) classes.getSelectedItem();
        roster.setRowCount(0);
        saveNote.setText("Select a student and an attendance status.");
        if (session == null) {
            scheduleNote.setText("No scheduled classes found.");
            updateSaveButton();
            return;
        }
        try {
            Map<String, AttendanceService.Status> previous = new TreeMap<>();
            for (AttendanceService.Entry entry : service.records()) {
                if (entry.classId().equalsIgnoreCase(session.id())) {
                    previous.put(entry.student(), entry.status());
                }
            }
            for (String student : service.registeredStudents(session.id())) {
                AttendanceService.Status saved = previous.remove(student);
                roster.addRow(new Object[]{student, saved == null ? "Pending" : saved.toString()});
            }
            previous.forEach((student, saved) ->
                    roster.addRow(new Object[]{student, saved.toString() + " (historical)"}));
            String availability = session.date() == null ? " A dated schedule is needed to save attendance."
                    : session.hasFinished() ? " Attendance entry is open."
                    : " Attendance opens after the class ends.";
            scheduleNote.setText("Instructor " + session.instructor() + "   |   Discipline "
                    + session.discipline() + "   |   " + roster.getRowCount() + " roster rows." + availability);
        } catch (IOException ex) {
            scheduleNote.setText("Unable to load the roster.");
            showError(ex.getMessage());
        }
        updateSaveButton();
    }

    private void updateSaveButton() {
        AttendanceService.Session session = (AttendanceService.Session) classes.getSelectedItem();
        saveButton.setEnabled(session != null && session.hasFinished() && rosterTable.getSelectedRow() >= 0);
    }

    private void saveSelection() {
        AttendanceService.Session session = (AttendanceService.Session) classes.getSelectedItem();
        int row = rosterTable.convertRowIndexToModel(rosterTable.getSelectedRow());
        String student = roster.getValueAt(row, 0).toString();
        AttendanceService.Status selected = (AttendanceService.Status) status.getSelectedItem();
        try {
            service.save(session.id(), Map.of(student, selected));
            refreshRoster();
            saveNote.setText("Saved " + selected + " for " + student + ".");
        } catch (IOException | IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshData() {
        try {
            String date = dateFilter.getText().strip();
            if (!date.isEmpty()) {
                date = date.length() == 7 ? YearMonth.parse(date).toString() : LocalDate.parse(date).toString();
            }
            List<AttendanceService.Entry> matches = new ArrayList<>();
            for (AttendanceService.Entry entry : service.records()) {
                if (matches(entry.student(), studentFilter)
                        && (matches(entry.classId(), classFilter) || matches(entry.className(), classFilter))
                        && matches(entry.instructor(), instructorFilter)
                        && matches(entry.discipline(), disciplineFilter)
                        && entry.date().toString().startsWith(date)) {
                    matches.add(entry);
                }
            }
            records.setRowCount(0);
            matches.forEach(entry -> records.addRow(entry.fields()));
            buildSummary(matches);
            resultsNote.setText(matches.size() + " saved attendance records");
        } catch (DateTimeParseException ex) {
            clearData("Enter a date as YYYY-MM-DD or a month as YYYY-MM.");
        } catch (IOException ex) {
            clearData(ex.getMessage());
        }
    }

    private void clearData(String message) {
        records.setRowCount(0);
        report.setRowCount(0);
        exportButton.setEnabled(false);
        resultsNote.setText(message);
    }

    private void buildSummary(List<AttendanceService.Entry> entries) {
        boolean byStudent = groupBy.getSelectedIndex() == 0;
        Map<String, int[]> totals = new TreeMap<>();
        Map<String, String> classLabels = new TreeMap<>();
        for (AttendanceService.Entry entry : entries) {
            String key = byStudent ? entry.student() : entry.classId().toUpperCase(Locale.ROOT);
            if (!byStudent) {
                classLabels.putIfAbsent(key, entry.classId() + " — " + entry.className());
            }
            int[] counts = totals.computeIfAbsent(key, ignored -> new int[AttendanceService.Status.values().length]);
            counts[entry.status().ordinal()]++;
        }
        report.setColumnIdentifiers(new String[]{byStudent ? "Student" : "Class",
            byStudent ? "Total sessions" : "Recorded students",
            "Present", "Absent", "Late", "Attended (incl. late)"});
        report.setRowCount(0);
        totals.forEach((key, counts) -> {
            String label = byStudent ? key : classLabels.get(key);
            int present = counts[AttendanceService.Status.PRESENT.ordinal()];
            int absent = counts[AttendanceService.Status.ABSENT.ordinal()];
            int late = counts[AttendanceService.Status.LATE.ordinal()];
            report.addRow(new Object[]{label, present + absent + late, present, absent, late, present + late});
        });
        exportButton.setEnabled(!totals.isEmpty());
    }

    private void exportReport() {
        refreshData();
        if (report.getRowCount() == 0) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("attendance-summary.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path destination = chooser.getSelectedFile().toPath();
        if (!destination.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            destination = destination.resolveSibling(destination.getFileName() + ".csv");
        }
        List<String> lines = new ArrayList<>();
        String[] row = new String[report.getColumnCount()];
        for (int i = 0; i < row.length; i++) {
            row[i] = report.getColumnName(i);
        }
        lines.add(csvLine(row));
        for (int i = 0; i < report.getRowCount(); i++) {
            for (int j = 0; j < row.length; j++) {
                row[j] = report.getValueAt(i, j).toString();
            }
            lines.add(csvLine(row));
        }
        try {
            Files.write(destination, lines, StandardOpenOption.CREATE_NEW);
            resultsNote.setText("Exported " + destination.getFileName());
        } catch (java.nio.file.FileAlreadyExistsException ex) {
            showError("That file already exists. Choose a different CSV filename.");
        } catch (IOException ex) {
            showError(ex.getMessage());
        }
    }

    private static String csvLine(String[] fields) {
        return String.join(",", Arrays.stream(fields).map(field -> {
            String value = field.stripLeading();
            if (!value.isEmpty() && "=+-@".indexOf(value.charAt(0)) >= 0) {
                field = "'" + field;
            }
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }).toList());
    }

    private List<JTextField> filterFields() {
        return List.of(studentFilter, classFilter, instructorFilter, disciplineFilter, dateFilter);
    }

    private static boolean matches(String value, JTextField filter) {
        return value.toLowerCase(Locale.ROOT).contains(filter.getText().strip().toLowerCase(Locale.ROOT));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Attendance Officer", JOptionPane.ERROR_MESSAGE);
    }

    private static JPanel filter(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        JLabel caption = new JLabel(label);
        caption.setLabelFor(field);
        panel.add(caption, BorderLayout.NORTH);
        panel.add(field);
        return panel;
    }

    private static JPanel paddedPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        return panel;
    }

    private static JTable table(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        return table;
    }

    private static DefaultTableModel readOnlyTable(String... headers) {
        return new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}
