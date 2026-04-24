package com.codeanalyzer.ui;

import com.codeanalyzer.ui.components.StatusBar;
import com.codeanalyzer.ui.panels.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Code Analyzer");
        setSize(1200, 780);
        setMinimumSize(new Dimension(1000, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        getContentPane().setBackground(UIConstants.BACKGROUND);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabs(),   BorderLayout.CENTER);
        add(new StatusBar(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.PRIMARY);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 54));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 16, 0, 16));

        JLabel title = new JLabel("Code Analyzer");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel subtitle = new JLabel("Codeforces  |  Phan tich AI  |  Danh gia");
        subtitle.setForeground(new Color(255, 255, 255, 200));
        subtitle.setFont(UIConstants.SMALL);
        subtitle.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(title,    BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);
        return header;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIConstants.SUBHEADER);
        tabs.setBackground(UIConstants.BACKGROUND);

        tabs.addTab("Quan ly Nick",    new StudentManagementPanel());
        tabs.addTab("Bai Nop",        new SubmissionPanel());
        tabs.addTab("Phan Tich AI",   new AnalysisPanel());
        tabs.addTab("Danh Gia",       new EvaluationPanel());
        tabs.addTab("Lich Tu Dong",   new SchedulerPanel());

        return tabs;
    }
}
