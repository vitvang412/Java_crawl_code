package com.codeanalyzer.ui.panels;

import com.codeanalyzer.scheduler.CrawlScheduler;
import com.codeanalyzer.ui.UIConstants;
import com.codeanalyzer.ui.components.StyledButton;
import com.codeanalyzer.util.AppConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class SchedulerPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    private final CrawlScheduler scheduler = CrawlScheduler.getInstance();

    private final JSpinner spinInterval = new JSpinner(
            new SpinnerNumberModel(AppConfig.getInstance().crawlerScheduleHours(), 1, 24 * 7, 1));
    private final JLabel lblStatus   = new JLabel();
    private final JLabel lblLastRun  = new JLabel();
    private final JLabel lblNextRun  = new JLabel();
    private final JTextArea logArea  = new JTextArea();

    private final StyledButton btnStart = new StyledButton("Bat lich",    StyledButton.Variant.SUCCESS);
    private final StyledButton btnStop  = new StyledButton("Tat lich",    StyledButton.Variant.DANGER);
    private final StyledButton btnRun   = new StyledButton("Chay ngay",   StyledButton.Variant.ACCENT);
    private final StyledButton btnClear = new StyledButton("Xoa log",     StyledButton.Variant.NEUTRAL);

    public SchedulerPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(UIConstants.BIG_PADDING, UIConstants.BIG_PADDING,
                                  UIConstants.BIG_PADDING, UIConstants.BIG_PADDING));
        setBackground(UIConstants.BACKGROUND);

        add(buildTopCard(),    BorderLayout.NORTH);
        add(buildLogCard(),    BorderLayout.CENTER);

        scheduler.setListener(this::appendLog);

        wireActions();
        refreshStatus();
        new Timer(2000, e -> refreshStatus()).start();
    }

    private JPanel buildTopCard() {
        JPanel card = card();
        card.setLayout(new BorderLayout(10, 10));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        controls.setOpaque(false);

        JLabel lbl = new JLabel("Chu ky (gio):");
        lbl.setFont(UIConstants.SUBHEADER);
        lbl.setForeground(UIConstants.TEXT);
        controls.add(lbl);

        ((JSpinner.DefaultEditor) spinInterval.getEditor())
                .getTextField().setColumns(4);
        spinInterval.setFont(UIConstants.MAIN);
        controls.add(spinInterval);

        controls.add(Box.createHorizontalStrut(16));
        controls.add(btnStart);
        controls.add(btnStop);
        controls.add(btnRun);

        JPanel statusPane = new JPanel(new GridLayout(3, 1, 0, 4));
        statusPane.setOpaque(false);
        statusPane.setBorder(new EmptyBorder(4, 4, 0, 4));

        for (JLabel l : new JLabel[]{lblStatus, lblLastRun, lblNextRun}) {
            l.setFont(UIConstants.MAIN);
            l.setForeground(UIConstants.TEXT);
        }
        statusPane.add(lblStatus);
        statusPane.add(lblLastRun);
        statusPane.add(lblNextRun);

        JLabel header = new JLabel("Lap Lich Tu Dong");
        header.setFont(UIConstants.HEADER);
        header.setForeground(UIConstants.PRIMARY_DARK);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(header,   BorderLayout.NORTH);
        top.add(controls, BorderLayout.CENTER);
        top.add(statusPane, BorderLayout.SOUTH);

        card.add(top, BorderLayout.CENTER);

        JLabel desc = new JLabel(
                "<html><span style='color:#8B7B6E'>Khi bat, he thong se tu dong thuc hien chu trinh "
                + "<b>Crawl -> Phan tich AI -> Danh gia</b> cho tat ca nick dang hoat dong. "
                + "Crawl dung Selenium semi-auto nen can ban xu ly Cloudflare neu co.</span></html>");
        desc.setFont(UIConstants.SMALL);
        desc.setBorder(new EmptyBorder(6, 0, 0, 0));
        card.add(desc, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildLogCard() {
        JPanel card = card();
        card.setLayout(new BorderLayout(6, 6));

        JLabel header = new JLabel("Log hoat dong");
        header.setFont(UIConstants.SUBHEADER);
        header.setForeground(UIConstants.PRIMARY_DARK);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(header, BorderLayout.WEST);
        headerRow.add(btnClear, BorderLayout.EAST);

        logArea.setEditable(false);
        logArea.setFont(UIConstants.MONO);
        logArea.setBackground(UIConstants.CONSOLE_BG);
        logArea.setForeground(UIConstants.CONSOLE_FG);
        logArea.setCaretColor(UIConstants.CONSOLE_FG);

        JScrollPane sp = new JScrollPane(logArea);
        sp.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));

        card.add(headerRow, BorderLayout.NORTH);
        card.add(sp,        BorderLayout.CENTER);
        return card;
    }

    private JPanel card() {
        JPanel c = new JPanel();
        c.setBackground(UIConstants.CARD_BG);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                new EmptyBorder(14, 16, 14, 16)));
        return c;
    }

    private void wireActions() {
        btnStart.addActionListener(e -> {
            int h = (int) spinInterval.getValue();
            scheduler.start(h);
            refreshStatus();
        });
        btnStop.addActionListener(e -> { scheduler.stop(); refreshStatus(); });
        btnRun.addActionListener(e -> {
            appendLog("Yeu cau chay ngay 1 chu trinh...");
            scheduler.runNow();
        });
        btnClear.addActionListener(e -> logArea.setText(""));
    }

    private void refreshStatus() {
        boolean on = scheduler.isRunning();
        lblStatus.setText("Trang thai: "
                + (on ? "DANG BAT  (chu ky " + scheduler.getIntervalHours() + "h)" : "TAT")
                + (scheduler.isJobActive() ? "  -  dang chay chu trinh..." : ""));

        lblLastRun.setText("Lan chay gan nhat: "
                + (scheduler.getLastRunAt() == null ? "--" : scheduler.getLastRunAt().format(FMT)));
        lblNextRun.setText("Lan chay ke tiep:  "
                + (!on || scheduler.getNextRunAt() == null ? "--" : scheduler.getNextRunAt().format(FMT)));

        btnStart.setEnabled(!on);
        btnStop.setEnabled(on);
        spinInterval.setEnabled(!on);
    }

    private void appendLog(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}
