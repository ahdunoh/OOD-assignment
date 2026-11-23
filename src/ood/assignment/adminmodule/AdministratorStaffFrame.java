package ood.assignment.adminmodule;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Administrator screen: add/view/search/update/delete Instructor and Facilities Officer. */
public class AdministratorStaffFrame extends JFrame {
    private final StaffStore store = new StaffStore();
    private final JTextField txtId = new JTextField(12);
    private final JTextField txtName = new JTextField(15);
    private final JComboBox<String> cmbRole = new JComboBox<>(new String[] {
        StaffStore.INSTRUCTOR, StaffStore.FACILITIES_OFFICER
    });
    private final JTextField txtSearch = new JTextField(16);
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] {"Staff ID", "Name", "Role"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private Staff selected;

    public AdministratorStaffFrame() {
        super("Administrator - Manage Staff");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(760, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JPanel fields = new JPanel(new GridLayout(2, 3, 8, 4));
        fields.add(new JLabel("Staff ID"));
        fields.add(new JLabel("Name"));
        fields.add(new JLabel("Role"));
        fields.add(txtId);
        fields.add(txtName);
        fields.add(cmbRole);

        JPanel top = new JPanel(new BorderLayout(4, 4));
        top.add(fields, BorderLayout.CENTER);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search ID / Name:"));
        searchPanel.add(txtSearch);
        JButton btnSearch = new JButton("Search");
        searchPanel.add(btnSearch);
        top.add(searchPanel, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout());
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update Name");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear / New");
        JButton btnRefresh = new JButton("Refresh");
        for (JButton button : new JButton[] {btnAdd, btnUpdate, btnDelete, btnClear, btnRefresh}) {
            buttons.add(button);
        }
        add(buttons, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || table.getSelectedRow() < 0) return;
            int row = table.getSelectedRow();
            selected = new Staff(model.getValueAt(row, 0).toString(),
                    model.getValueAt(row, 1).toString(), model.getValueAt(row, 2).toString());
            txtId.setText(selected.getStaffId());
            txtName.setText(selected.getName());
            cmbRole.setSelectedItem(selected.getRole());
            txtId.setEditable(false);
            cmbRole.setEnabled(false);
        });

        btnSearch.addActionListener(e -> reload());
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); reload(); });
        btnClear.addActionListener(e -> clearSelection());
        btnAdd.addActionListener(e -> performAdd());
        btnUpdate.addActionListener(e -> performUpdate());
        btnDelete.addActionListener(e -> performDelete());
        reload();
        ood.assignment.WindowNavigation.install(this);
    }

    private void clearSelection() {
        table.clearSelection();
        selected = null;
        txtId.setEditable(true);
        cmbRole.setEnabled(true);
        txtId.setText("");
        txtName.setText("");
        txtId.requestFocusInWindow();
    }

    private void reload() {
        try {
            String search = txtSearch.getText().trim().toLowerCase();
            List<Staff> all = store.readAll();
            model.setRowCount(0);
            for (Staff staff : all) {
                if (StaffStore.ADMINISTRATOR.equals(staff.getRole())) continue;
                if (!staff.getStaffId().toLowerCase().contains(search)
                        && !staff.getName().toLowerCase().contains(search)) continue;
                model.addRow(new Object[] {staff.getStaffId(), staff.getName(), staff.getRole()});
            }
            clearSelection();
        } catch (IOException ex) { showError(ex); }
    }

    private void performAdd() {
        try {
            if (selected != null) throw new IllegalArgumentException("Click Clear / New before adding.");
            store.addStaff(new Staff(txtId.getText(), txtName.getText(), (String) cmbRole.getSelectedItem()));
            reload();
            JOptionPane.showMessageDialog(this, "Staff added.");
        } catch (IOException | IllegalArgumentException ex) { showError(ex); }
    }

    private void performUpdate() {
        try {
            if (selected == null) throw new IllegalArgumentException("Select a staff record first.");
            store.updateStaff(selected.getRole(), selected.getStaffId(), txtName.getText());
            reload();
            JOptionPane.showMessageDialog(this, "Name updated.");
        } catch (IOException | IllegalArgumentException ex) { showError(ex); }
    }

    private void performDelete() {
        try {
            if (selected == null) throw new IllegalArgumentException("Select a staff record first.");
            if (JOptionPane.showConfirmDialog(this, "Delete " + selected.getName() + "?",
                    "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            store.deleteStaff(selected.getRole(), selected.getStaffId());
            reload();
            JOptionPane.showMessageDialog(this, "Staff deleted.");
        } catch (IOException | IllegalArgumentException ex) { showError(ex); }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

}
