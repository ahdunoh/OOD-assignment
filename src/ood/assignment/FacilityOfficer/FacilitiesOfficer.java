package ood.assignment.FacilityOfficer;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import ood.assignment.WindowNavigation;

/** Entry page for the Facilities Officer's existing screens. */
public class FacilitiesOfficer extends JFrame {
    public FacilitiesOfficer() {
        this("");
    }

    public FacilitiesOfficer(String username) {
        super("SDAMS - Facilities Officer");
        setSize(440, 300);
        setLayout(new BorderLayout(10, 10));
        add(new JLabel("Facilities Officer: " + username, SwingConstants.CENTER),
                BorderLayout.NORTH);

        JPanel options = new JPanel(new GridLayout(3, 1, 8, 8));
        JButton inventory = new JButton("Equipment Inventory");
        JButton issues = new JButton("Facility Issues");
        JButton stock = new JButton("Stock Report");
        options.add(inventory);
        options.add(issues);
        options.add(stock);
        add(options, BorderLayout.CENTER);

        inventory.addActionListener(event ->
                WindowNavigation.show(new EquipmentIventoryFrame(), this));
        issues.addActionListener(event ->
                WindowNavigation.show(new FacilityIssue(), this));
        stock.addActionListener(event ->
                WindowNavigation.show(new StockReportFrame(), this));
        WindowNavigation.install(this);
    }
}
