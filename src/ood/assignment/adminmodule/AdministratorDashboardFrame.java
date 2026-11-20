package ood.assignment.adminmodule;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.awt.GridLayout;
import javax.swing.*;

/** Open only after authorization by the existing team Login. No parallel login. */
public class AdministratorDashboardFrame extends JFrame {
    private final List<JFrame> openedFrames = new ArrayList<>();
    public AdministratorDashboardFrame(String username) {
        super("SDAMS - Administrator");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(420, 340);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        add(new JLabel("Logged in as Administrator: " + username,
                SwingConstants.CENTER), BorderLayout.NORTH);
        JPanel options = new JPanel(new GridLayout(4, 1, 8, 8));
        JButton btnStaff = new JButton("Manage Instructors / Facilities Officers");
        JButton btnStudents = new JButton("Manage Students");
        JButton btnPayments = new JButton("View Student Payment Records");
        JButton btnLogout = new JButton("Close Administrator Menu");
        options.add(btnStaff);
        options.add(btnStudents);
        options.add(btnPayments);
        options.add(btnLogout);
        add(options, BorderLayout.CENTER);
        btnStaff.addActionListener(e -> showChild(new AdministratorStaffFrame()));
        btnStudents.addActionListener(e -> showChild(new AdministratorStudentFrame()));
        btnPayments.addActionListener(e -> showChild(new AdministratorPaymentFrame()));
        btnLogout.addActionListener(e -> dispose());
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                // Close only windows launched by this dashboard.
                for (JFrame window : openedFrames) window.dispose();
                openedFrames.clear();
                // Team login controller still owns real logout and permission changes.
            }
        });
    }

    private void showChild(JFrame frame) {
        openedFrames.add(frame);
        frame.setVisible(true);
    }
}
