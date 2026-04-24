package com.codeanalyzer.ui.components;

import com.codeanalyzer.scheduler.CrawlScheduler;
import com.codeanalyzer.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class StatusBar extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM");

    private final JLabel leftLabel  = new JLabel("He thong da san sang.");
    private final JLabel rightLabel = new JLabel();

    public StatusBar() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.PRIMARY_LIGHT);
        setBorder(new EmptyBorder(4, 12, 4, 12));
        setPreferredSize(new Dimension(0, 28));

        leftLabel.setFont(UIConstants.SMALL);
        leftLabel.setForeground(UIConstants.TEXT);
        rightLabel.setFont(UIConstants.SMALL);
        rightLabel.setForeground(UIConstants.TEXT);
        rightLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        add(leftLabel,  BorderLayout.WEST);
        add(rightLabel, BorderLayout.EAST);

        new Timer(5000, e -> refreshSchedulerInfo()).start();
        refreshSchedulerInfo();
    }

    public void setMessage(String msg) { leftLabel.setText(msg); }

    private void refreshSchedulerInfo() {
        CrawlScheduler s = CrawlScheduler.getInstance();
        StringBuilder sb = new StringBuilder();
        if (s.isRunning()) {
            sb.append("Lich: ON (").append(s.getIntervalHours()).append("h)");
            if (s.getNextRunAt() != null) {
                sb.append("  |  Ke: ").append(s.getNextRunAt().format(FMT));
            }
        } else {
            sb.append("Lich: OFF");
        }
        if (s.isJobActive()) sb.append("  |  Dang chay...");
        rightLabel.setText(sb.toString());
    }
}
