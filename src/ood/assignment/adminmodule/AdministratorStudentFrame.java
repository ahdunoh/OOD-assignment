package ood.assignment.adminmodule;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Administrator: manage only student accounts in the team's credentials.txt. */
public class AdministratorStudentFrame extends JFrame {
    private final StudentStore store = new StudentStore();
    private final JTextField txtId = new JTextField(18);
    private final JTextField txtUsername = new JTextField(16);
    private final JPasswordField txtPassword = new JPasswordField(16);
    private final JTextField txtSearch = new JTextField(18);
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] {"Account ID / Email", "Username", "Role", "Flags"},0) {
        @Override public boolean isCellEditable(int row, int col) {return false;}
    };
    private final JTable table = new JTable(model);
    private String selectedUsername;

    public AdministratorStudentFrame() {
        super("Administrator - Manage Students (credentials.txt)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(930, 525); setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        JPanel fields = new JPanel(new GridLayout(2, 3, 6, 4));
        fields.add(new JLabel("Account ID / Email"));
        fields.add(new JLabel("Username (locked after creation)"));
        fields.add(new JLabel("Password (blank = keep on update)"));
        fields.add(txtId); fields.add(txtUsername); fields.add(txtPassword);
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
        search.add(new JLabel("Search ID / Username:")); search.add(txtSearch);
        JButton btnSearch=new JButton("Search"), btnRefresh=new JButton("Show All");
        search.add(btnSearch);search.add(btnRefresh);
        JPanel top=new JPanel(new BorderLayout());top.add(fields,BorderLayout.NORTH);
        top.add(search,BorderLayout.SOUTH);add(top,BorderLayout.NORTH);
        add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel bottom=new JPanel(new FlowLayout());
        JButton btnAdd=new JButton("Add"), btnUpdate=new JButton("Update"),
                btnDelete=new JButton("Delete"),btnClear=new JButton("Clear / New");
        for(JButton b:new JButton[]{btnAdd,btnUpdate,btnDelete,btnClear}) bottom.add(b);
        add(bottom,BorderLayout.SOUTH);
        table.getSelectionModel().addListSelectionListener(e->{
            if(e.getValueIsAdjusting()||table.getSelectedRow()<0) return;
            int row=table.convertRowIndexToModel(table.getSelectedRow());
            selectedUsername=model.getValueAt(row,1).toString();
            txtId.setText(model.getValueAt(row,0).toString());
            txtUsername.setText(selectedUsername);txtUsername.setEditable(false);
            txtPassword.setText("");
        });
        btnSearch.addActionListener(e->reload());
        btnRefresh.addActionListener(e->{txtSearch.setText("");reload();});
        btnClear.addActionListener(e->clear());
        btnAdd.addActionListener(e->addStudent());
        btnUpdate.addActionListener(e->updateStudent());
        btnDelete.addActionListener(e->deleteStudent());
        reload();
        ood.assignment.WindowNavigation.install(this);
    }
    private void clear(){table.clearSelection();selectedUsername=null;
        txtId.setText("");txtUsername.setText("");txtUsername.setEditable(true);txtPassword.setText("");}
    private void reload(){try {
        List<Student> all=store.readAll();String search=txtSearch.getText().trim().toLowerCase();
        model.setRowCount(0);
        for(Student st:all){
            if(!st.getStudentId().toLowerCase().contains(search)
                    && !st.getUsername().toLowerCase().contains(search)) continue;
            model.addRow(new Object[]{st.getStudentId(),st.getUsername(),st.getRole(),st.getStatusDisplay()});
        }
        clear();
    } catch(IOException ex){error(ex);}}
    private Student draft(boolean update) throws IOException{
        String pass=new String(txtPassword.getPassword());
        if(update){
            for(Student old:store.readAll()){
                if(old.getUsername().equalsIgnoreCase(selectedUsername)){
                    return new Student(txtId.getText(),old.getUsername(),
                            pass.isEmpty()?old.getPassword():pass,old.getRole(),old.getFlags());
                }
            }
            throw new IllegalArgumentException("Student not found.");
        }
        return new Student(txtId.getText(),txtUsername.getText(),pass);
    }
    private void addStudent(){try{
        if(selectedUsername!=null)throw new IllegalArgumentException("Click Clear / New first.");
        store.add(draft(false));reload();JOptionPane.showMessageDialog(this,"Student account saved.");
    }catch(IOException|IllegalArgumentException ex){error(ex);}}
    private void updateStudent(){try{
        if(selectedUsername==null)throw new IllegalArgumentException("Select a student first.");
        store.update(selectedUsername,draft(true));reload();
        JOptionPane.showMessageDialog(this,"Student account updated.");
    }catch(IOException|IllegalArgumentException ex){error(ex);}}
    private void deleteStudent(){try{
        if(selectedUsername==null)throw new IllegalArgumentException("Select a student first.");
        if(JOptionPane.showConfirmDialog(this,"Delete login for "+selectedUsername+"? Historic enrollments and payments remain.",
                "Confirm Delete",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        store.delete(selectedUsername);reload();JOptionPane.showMessageDialog(this,"Student account deleted.");
    }catch(IOException|IllegalArgumentException ex){error(ex);}}
    private void error(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
}
