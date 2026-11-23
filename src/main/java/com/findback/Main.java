package com.findback;

import com.findback.ui.FindBackTheme;
import com.findback.ui.WelcomeFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FindBackTheme.setupLookAndFeel();
            WelcomeFrame welcome = new WelcomeFrame();
            welcome.setVisible(true);
        });
    }
}
