package hospital;

import hospital.util.UIHelper;
import hospital.view.MainFrame;

import javax.swing.*;

/**
 * Application entry point.
 *
 * Run this class in Eclipse:
 *   Right-click → Run As → Java Application
 */
public class HospitalApp {

    public static void main(String[] args) {
        // All Swing operations must run on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            UIHelper.applyLookAndFeel();
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}