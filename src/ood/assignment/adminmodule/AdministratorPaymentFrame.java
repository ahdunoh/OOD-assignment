package ood.assignment.adminmodule;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Read-only Administrator view of students' past payment records. */
public class AdministratorPaymentFrame extends JFrame {
    private final PaymentStore paymentStore = new PaymentStore();
    private final StudentStore studentStore = new StudentStore();
    private final JTextField txtSearch = new JTextField(15);
    private final JComboBox<String> cmbStatus = new JComboBox<>(
            new String[] {"All Statuses", "Paid", "Pending", "Refunded"});
    private final JTextField txtFrom = new JTextField(9);
    private final JTextField txtTo = new JTextField(9);
    private final JLabel lblSummary = new JLabel(" ");
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] {"Payment ID", "Username", "Account ID / Email", "Class ID",
                "Amount (RM)", "Date", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    public AdministratorPaymentFrame() {
        super("Administrator - Payment Records (Read Only)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1010, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        txtFrom.setToolTipText("YYYY-MM-DD, leave empty for no start date");
        txtTo.setToolTipText("YYYY-MM-DD, leave empty for no end date");

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(new JLabel("Search:"));
        filters.add(txtSearch);
        filters.add(new JLabel("Status:"));
        filters.add(cmbStatus);
        filters.add(new JLabel("From:"));
        filters.add(txtFrom);
        filters.add(new JLabel("To:"));
        filters.add(txtTo);
        JButton btnFilter = new JButton("Search / Filter");
        JButton btnClear = new JButton("Reset");
        filters.add(btnFilter);
        filters.add(btnClear);
        add(filters, BorderLayout.NORTH);
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        add(lblSummary, BorderLayout.SOUTH);

        btnFilter.addActionListener(e -> reload());
        cmbStatus.addActionListener(e -> reload());
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            cmbStatus.setSelectedIndex(0);
            txtFrom.setText("");
            txtTo.setText("");
            reload();
        });
        reload();
        ood.assignment.WindowNavigation.install(this);
    }

    private LocalDate parseDate(String value, String label) {
        if (value.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(label + " must use YYYY-MM-DD.");
        }
    }

    private void reload() {
        try {
            LocalDate from = parseDate(txtFrom.getText(), "From date");
            LocalDate to = parseDate(txtTo.getText(), "To date");
            if (from != null && to != null && from.isAfter(to)) {
                throw new IllegalArgumentException("From date must not be after To date.");
            }
            Map<String, String> names = new HashMap<>();
            for (Student student : studentStore.readAll()) {
                names.put(student.getUsername().toLowerCase(), student.getStudentId());
            }
            String keyword = txtSearch.getText().trim().toLowerCase();
            String status = (String) cmbStatus.getSelectedItem();
            List<PaymentRecord> payments = paymentStore.readAll();
            model.setRowCount(0);
            int count = 0;
            BigDecimal paidTotal = BigDecimal.ZERO;
            for (PaymentRecord record : payments) {
                String studentName = names.getOrDefault(record.getStudentId().toLowerCase(),
                        "(unknown account)");
                boolean searchMatch = record.getPaymentId().toLowerCase().contains(keyword)
                        || record.getStudentId().toLowerCase().contains(keyword)
                        || record.getClassId().toLowerCase().contains(keyword)
                        || studentName.toLowerCase().contains(keyword);
                boolean statusMatch = "All Statuses".equals(status)
                        || record.getStatus().equalsIgnoreCase(status);
                boolean dateMatch = (from == null || !record.getDate().isBefore(from))
                        && (to == null || !record.getDate().isAfter(to));
                if (!searchMatch || !statusMatch || !dateMatch) continue;
                count++;
                if (record.getStatus().equalsIgnoreCase("Paid")) {
                    paidTotal = paidTotal.add(record.getAmount());
                }
                model.addRow(new Object[] {record.getPaymentId(), record.getStudentId(),
                        studentName, record.getClassId(), record.getAmount().toPlainString(),
                        record.getDate().toString(), record.getStatus()});
            }
            lblSummary.setText("Records shown: " + count + "   |   Paid amount (shown): RM "
                    + paidTotal.setScale(2).toPlainString() + "   |   Read-only");
        } catch (IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Payment Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
