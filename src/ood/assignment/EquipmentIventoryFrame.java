/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package ood.assignment;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
/**
 *
 * @author lwjlpl
 */
public class EquipmentIventoryFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = 
            java.util.logging.Logger.getLogger(
                    EquipmentIventoryFrame.class.getName());

    private String appliedSearch = "";

    /**
     * Creates new form EquipmentIventoryFrame
     */
    public EquipmentIventoryFrame() {
        initComponents();
        jTable3.setDefaultEditor(Object.class, null);
        jTable3.getTableHeader().setReorderingAllowed(false);
        jTable3.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                fillFieldsFromSelection();
            }
        });
        refreshEquipmentTable("");
    }

    private void refreshEquipmentTable(String keyword) {
        try {
            List<Object[]> rows = readEquipmentRows(keyword);
            DefaultTableModel model = (DefaultTableModel) jTable3.getModel();
            model.setRowCount(0);
            for (Object[] row : rows) {
                model.addRow(row);
            }
            appliedSearch = keyword;
        } catch (IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(
                    this, "Could not load equipment: " + ex.getMessage());
        }
    }

    static List<Object[]> readEquipmentRows(String keyword) throws IOException {
        List<Object[]> rows = new ArrayList<>();
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of("equipment.txt"));
        } catch (NoSuchFileException ex) {
            return rows;
        }

        String query = keyword.toLowerCase(Locale.ROOT);
        for (String line : lines) {
            String[] fields = line.split("\\|", -1);
            if (fields.length != 3) {
                continue;
            }
            String id = fields[0];
            String name = fields[1];
            if (query.isEmpty() || id.toLowerCase(Locale.ROOT).contains(query)
                    || name.toLowerCase(Locale.ROOT).contains(query)) {
                try {
                    int quantity = Integer.parseInt(fields[2].trim());
                    rows.add(new Object[]{id, name, quantity});
                } catch (NumberFormatException ex) {
                    logger.warning("Skipped an equipment record with an invalid quantity.");
                }
            }
        }
        return rows;
    }

    private void fillFieldsFromSelection() {
        int selectedRow = jTable3.getSelectedRow();
        if (selectedRow != -1) {
            jTextField1.setText(String.valueOf(jTable3.getValueAt(selectedRow, 0)));
            jTextField2.setText(String.valueOf(jTable3.getValueAt(selectedRow, 1)));
            jTextField3.setText(String.valueOf(jTable3.getValueAt(selectedRow, 2)));
        }
    }

    private static void updateEquipmentRecord(Path file, String id, String name, int quantity)
            throws IOException {
        Path target = file.toAbsolutePath();
        List<String> lines = Files.readAllLines(target);
        int matchingLine = findUniqueEquipmentLine(lines, id);
        lines.set(matchingLine, id + "|" + name + "|" + quantity);
        writeEquipmentLines(target, lines);
    }

    private static void deleteEquipmentRecord(Path file, String id) throws IOException {
        Path target = file.toAbsolutePath();
        List<String> lines = Files.readAllLines(target);
        int matchingLine = findUniqueEquipmentLine(lines, id);
        lines.remove(matchingLine);
        writeEquipmentLines(target, lines);
    }

    private static int findUniqueEquipmentLine(List<String> lines, String id) throws IOException {
        int matchingLine = -1;
        for (int i = 0; i < lines.size(); i++) {
            String[] fields = lines.get(i).split("\\|", -1);
            if (fields.length == 3 && fields[0].equals(id)) {
                if (matchingLine != -1) {
                    throw new IOException("More than one record has ID " + id + ".");
                }
                matchingLine = i;
            }
        }
        if (matchingLine == -1) {
            throw new IOException("Equipment ID " + id + " was not found.");
        }
        return matchingLine;
    }

    private static void writeEquipmentLines(Path target, List<String> lines) throws IOException {
        Path temporary = Files.createTempFile(target.getParent(), "equipment-", ".tmp");
        try {
            Files.write(temporary, lines);
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jMenu1 = new javax.swing.JMenu();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        AddButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        Search = new javax.swing.JTextField();
        UpdateButton = new javax.swing.JButton();
        DeleteButton = new javax.swing.JButton();
        StockButton = new javax.swing.JButton();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        jMenu1.setText("jMenu1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Equipment Inventory");

        jLabel2.setText("Equipment ID");

        jLabel3.setText("Name");

        jLabel4.setText("Quantity");

        jTextField1.addActionListener(this::jTextField1ActionPerformed);

        AddButton.setText("Add Equipment");
        AddButton.addActionListener(this::AddButtonActionPerformed);

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Equipment ID", "Name", "Quantity"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane3.setViewportView(jTable3);

        jTextField2.addActionListener(this::jTextField2ActionPerformed);

        jTextField3.addActionListener(this::jTextField3ActionPerformed);

        jLabel5.setText("Search");

        Search.addActionListener(this::SearchActionPerformed);

        UpdateButton.setText("Update Equipment");
        UpdateButton.addActionListener(this::UpdateButtonActionPerformed);

        DeleteButton.setText("Delete Equipment");
        DeleteButton.addActionListener(this::DeleteButtonActionPerformed);

        StockButton.setText("Stock Report");
        StockButton.addActionListener(this::StockButtonActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(Search, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5)
                        .addGap(575, 575, 575))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(590, 590, 590))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(173, 173, 173)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 1010, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(622, 622, 622)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                            .addComponent(jTextField2)
                            .addComponent(jTextField3))))
                .addContainerGap(201, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(AddButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(StockButton, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(DeleteButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(UpdateButton))
                .addGap(503, 503, 503))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Search, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(AddButton)
                    .addComponent(UpdateButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(DeleteButton)
                    .addComponent(StockButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 86, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 482, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(89, 89, 89))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void AddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddButtonActionPerformed
        // TODO add your handling code here:
        try{
            int num = Integer.parseInt(jTextField3.getText().trim());
                if (num < 0) {
                    javax.swing.JOptionPane.showMessageDialog(
                            this, "Quantity cannot be negative");
                    return;
                }
                System.out.println(num);
                String record = 
                        jTextField1.getText().trim() + 
                        "|" + jTextField2.getText().trim() + 
                        "|" + num;
                try (
                        FileWriter writer = new FileWriter(
                        "equipment.txt", true)) {
                    writer.write(record + System.lineSeparator());
                } catch (IOException ex) {
                    javax.swing.JOptionPane.showMessageDialog(
                            this, "Could not save equipment.");
                    return;
                }
                refreshEquipmentTable(appliedSearch);
                javax.swing.JOptionPane.showMessageDialog(
                        this, "File has been written");
        } catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(
                    this, "The text is not a number");
        }
    }//GEN-LAST:event_AddButtonActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void SearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchActionPerformed
        String keyword = Search.getText().trim();
        refreshEquipmentTable(keyword);
    }//GEN-LAST:event_SearchActionPerformed

    private void UpdateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdateButtonActionPerformed
        int selectedRow = jTable3.getSelectedRow();
        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(
                    this, "Please select an equipment item."
            );
            return;
        }

        String equipmentId = String.valueOf(jTable3.getValueAt(selectedRow, 0));
        String name = jTextField2.getText().trim();
        if (name.isEmpty() || name.contains("|")) {
            javax.swing.JOptionPane.showMessageDialog(
                    this, "Enter a name without the | character."
            );
            return;
        }

        int quantity;
        try {
            quantity = 
                    Integer.parseInt(jTextField3.getText().trim());
        } catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(
                    this, "Enter a whole number for Quantity."
            );
            return;
        }
        if (quantity < 0) {
            javax.swing.JOptionPane.showMessageDialog(
                    this, "Quantity cannot be negative."
            );
            return;
        }

        try {
            updateEquipmentRecord(
                    Path.of("equipment.txt"), equipmentId, name, quantity
            );
        } catch (IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(
                    this, "Could not update equipment: " + ex.getMessage()
            );
            return;
        }

        jTextField1.setText(equipmentId);
        refreshEquipmentTable(appliedSearch);
        javax.swing.JOptionPane.showMessageDialog(
                this, "Equipment updated."
        );
    }//GEN-LAST:event_UpdateButtonActionPerformed

    private void DeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteButtonActionPerformed
        // TODO add your handling code here:
        int selectedRow = jTable3.getSelectedRow();
        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(
                    this, "Please select an equipment item."
            );
            return;
        }
        String equipmentId = String.valueOf(jTable3.getValueAt(selectedRow, 0));
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete equipment " + equipmentId + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            deleteEquipmentRecord(Path.of("equipment.txt"), equipmentId);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not delete equipment: " + ex.getMessage());
            return;
        }

        jTextField1.setText("");
        jTextField2.setText("");
        jTextField3.setText("");
        refreshEquipmentTable(appliedSearch);
        JOptionPane.showMessageDialog(this, "Equipment deleted.");
    }//GEN-LAST:event_DeleteButtonActionPerformed

    private void StockButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StockButtonActionPerformed
        // TODO add your handling code here:
        StockReportFrame report = new StockReportFrame();
        report.setLocationRelativeTo(this);
        report.setVisible(true);
    }//GEN-LAST:event_StockButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new EquipmentIventoryFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddButton;
    private javax.swing.JButton DeleteButton;
    private javax.swing.JTextField Search;
    private javax.swing.JButton StockButton;
    private javax.swing.JButton UpdateButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    // End of variables declaration//GEN-END:variables
}
