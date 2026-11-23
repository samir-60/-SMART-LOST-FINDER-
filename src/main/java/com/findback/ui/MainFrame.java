package com.findback.ui;

import com.findback.model.User;
import com.findback.service.SessionManager;
import com.findback.ui.admin.*;
import com.findback.ui.student.*;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private SidebarPanel sidebarPanel;

    public static final String DASHBOARD = "dashboard";
    public static final String REPORT_LOST = "reportLost";
    public static final String REPORT_FOUND = "reportFound";
    public static final String BROWSE = "browse";
    public static final String MATCHES = "matches";
    public static final String CLAIMS = "claims";
    public static final String NOTIFICATIONS = "notifications";
    public static final String PROFILE = "profile";
    public static final String ADMIN_DASHBOARD = "adminDashboard";
    public static final String ADMIN_REPORTS = "adminReports";
    public static final String ADMIN_CLAIMS = "adminClaims";
    public static final String ADMIN_USERS = "adminUsers";
    public static final String ADMIN_CATEGORIES = "adminCategories";
    public static final String ADMIN_LOCATIONS = "adminLocations";
    public static final String ADMIN_HANDOVERS = "adminHandovers";
    public static final String ADMIN_ANALYTICS = "adminAnalytics";

    public MainFrame() {
        setTitle("FindBack — Smart Lost & Found Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1100, 700));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(FindBackTheme.BG);

        // Sidebar
        sidebarPanel = new SidebarPanel(this);
        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        // Content area with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(FindBackTheme.BG);

        // Student panels
        contentPanel.add(new DashboardPanel(), DASHBOARD);
        contentPanel.add(new LostReportPanel(), REPORT_LOST);
        contentPanel.add(new FoundReportPanel(), REPORT_FOUND);
        contentPanel.add(new BrowsePanel(), BROWSE);
        contentPanel.add(new MatchesPanel(), MATCHES);
        contentPanel.add(new ClaimsPanel(), CLAIMS);
        contentPanel.add(new NotificationsPanel(), NOTIFICATIONS);
        contentPanel.add(new ProfilePanel(), PROFILE);

        // Admin panels
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.isStaffOrAdmin()) {
            contentPanel.add(new AdminDashboardPanel(), ADMIN_DASHBOARD);
            contentPanel.add(new AdminReportsPanel(), ADMIN_REPORTS);
            contentPanel.add(new AdminClaimsPanel(), ADMIN_CLAIMS);
            contentPanel.add(new AdminUsersPanel(), ADMIN_USERS);
            contentPanel.add(new AdminCategoriesPanel(), ADMIN_CATEGORIES);
            contentPanel.add(new AdminLocationsPanel(), ADMIN_LOCATIONS);
            contentPanel.add(new AdminHandoversPanel(), ADMIN_HANDOVERS);
            contentPanel.add(new AdminAnalyticsPanel(), ADMIN_ANALYTICS);
        }

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);

        showPanel(DASHBOARD);
    }

    public void showPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
        sidebarPanel.setActiveButton(panelName);

        // Refresh panel data
        for (Component comp : contentPanel.getComponents()) {
            if (comp.isVisible() && comp instanceof Refreshable) {
                ((Refreshable) comp).refresh();
            }
        }
    }

    public void logout() {
        SessionManager.getInstance().logout();
        dispose();
        WelcomeFrame welcome = new WelcomeFrame();
        welcome.setVisible(true);
    }

    public interface Refreshable {
        void refresh();
    }
}
