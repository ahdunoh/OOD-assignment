package ood.assignment;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/** Keeps each window connected to the page that opened it. */
public final class WindowNavigation {
    private static final String PARENT = "navigation.parent";
    private static final String CHILD = "navigation.child";
    private static final String INSTALLED = "navigation.installed";

    private WindowNavigation() {}

    public static void install(JFrame window) {
        if (Boolean.TRUE.equals(window.getRootPane().getClientProperty(INSTALLED))) {
            return;
        }
        window.getRootPane().putClientProperty(INSTALLED, true);
        window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container content = window.getContentPane();
        if (!hasBackButton(content)) {
            JPanel page = new JPanel(new BorderLayout());
            JPanel navigation = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton back = new JButton("Back");
            back.addActionListener(event -> window.dispose());
            navigation.add(back);
            page.add(navigation, BorderLayout.NORTH);
            page.add(content, BorderLayout.CENTER);
            window.setContentPane(page);
            int toolbarHeight = navigation.getPreferredSize().height;
            window.setSize(window.getWidth(), window.getHeight() + toolbarHeight);
        }
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                JFrame child = (JFrame) window.getRootPane().getClientProperty(CHILD);
                if (child != null) {
                    window.getRootPane().putClientProperty(CHILD, null);
                    child.dispose();
                }
                JFrame parent = (JFrame) window.getRootPane().getClientProperty(PARENT);
                if (parent != null && parent.isDisplayable()
                        && parent.getRootPane().getClientProperty(CHILD) == window) {
                    parent.getRootPane().putClientProperty(CHILD, null);
                    parent.setVisible(true);
                    parent.toFront();
                }
            }
        });
    }

    private static boolean hasBackButton(Container container) {
        for (java.awt.Component component : container.getComponents()) {
            if (component instanceof JButton button
                    && button.getText() != null && button.getText().startsWith("Back")) {
                return true;
            }
            if (component instanceof Container child && hasBackButton(child)) {
                return true;
            }
        }
        return false;
    }

    public static void show(JFrame window, JFrame parent) {
        install(window);
        window.getRootPane().putClientProperty(PARENT, parent);
        if (parent != null) {
            parent.getRootPane().putClientProperty(CHILD, window);
            parent.setVisible(false);
        }
        window.setLocationRelativeTo(parent);
        window.setVisible(true);
    }
}
