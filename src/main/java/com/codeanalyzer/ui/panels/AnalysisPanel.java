package com.codeanalyzer.ui.panels;

import com.codeanalyzer.ai.CodeAnalyzer;
import com.codeanalyzer.database.StudentDAO;
import com.codeanalyzer.database.SubmissionDAO;
import com.codeanalyzer.model.Student;
import com.codeanalyzer.model.Submission;
import com.codeanalyzer.ui.UIConstants;
import com.codeanalyzer.ui.components.StyledButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class AnalysisPanel extends JPanel {

    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final StudentDAO    studentDAO    = new StudentDAO();
    private final CodeAnalyzer  analyzer      = new CodeAnalyzer();

    private JTextArea logArea;
    private JProgressBar progressBar;
    private JSpinner spinLimit;
    private JComboBox<StudentItem> cboStudent;

    public AnalysisPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(UIConstants.BIG_PADDING, UIConstants.BIG_PADDING,
                                   UIConstants.BIG_PADDING, UIConstants.BIG_PADDING));
        setBackground(UIConstants.BACKGROUND);

        add(buildTopPanel(),  BorderLayout.NORTH);
        add(buildLogPanel(),  BorderLayout.CENTER);
    }

    private JPanel buildTopPanel() {
        JPanel top = new JPanel(new BorderLayout(6, 8));
        top.setOpaque(false);

        JPanel configRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configRow.setOpaque(false);

        cboStudent = new JComboBox<>();
        cboStudent.setPreferredSize(new Dimension(180, 28));
        cboStudent.setFont(UIConstants.MAIN);
        loadStudentCombo();

        JLabel lblSv = new JLabel("Sinh vien:");
        lblSv.setFont(UIConstants.SUBHEADER);
        lblSv.setForeground(UIConstants.TEXT);
        configRow.add(lblSv);
        configRow.add(cboStudent);

        spinLimit = new JSpinner(new SpinnerNumberModel(10, 1, 200, 5));
        spinLimit.setFont(UIConstants.MAIN);
        JLabel lblLimit = new JLabel("  So bai moi dot:");
        lblLimit.setFont(UIConstants.SUBHEADER);
        lblLimit.setForeground(UIConstants.TEXT);
        configRow.add(lblLimit);
        configRow.add(spinLimit);

        top.add(configRow, BorderLayout.NORTH);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        btnRow.setOpaque(false);

        StyledButton btnStart = new StyledButton("Bat dau phan tich (Gemini AI)", StyledButton.Variant.SUCCESS);
        StyledButton btnClear = new StyledButton("Xoa log", StyledButton.Variant.NEUTRAL);

        btnStart.addActionListener(e -> startAnalysis(btnStart));
        btnClear.addActionListener(e -> logArea.setText(""));

        btnRow.add(btnStart);
        btnRow.add(btnClear);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Cho...");
        progressBar.setFont(UIConstants.SMALL);
        progressBar.setPreferredSize(new Dimension(250, 24));
        btnRow.add(progressBar);

        top.add(btnRow, BorderLayout.CENTER);
        return top;
    }

    private JPanel buildLogPanel() {
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(UIConstants.MONO);
        logArea.setBackground(UIConstants.CONSOLE_BG);
        logArea.setForeground(UIConstants.CONSOLE_FG);
        logArea.setCaretColor(UIConstants.CONSOLE_FG);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIConstants.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER),
            new EmptyBorder(8, 8, 8, 8)));

        JLabel lbl = new JLabel("Log phan tich Gemini AI");
        lbl.setFont(UIConstants.SUBHEADER);
        lbl.setForeground(UIConstants.PRIMARY_DARK);
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        return panel;
    }

    private void loadStudentCombo() {
        cboStudent.removeAllItems();
        cboStudent.addItem(new StudentItem(0, "-- Tat ca --"));
        for (Student s : studentDAO.findAll()) {
            cboStudent.addItem(new StudentItem(s.getId(), s.getUsername()));
        }
    }

    private void startAnalysis(JButton btnStart) {
        btnStart.setEnabled(false);
        int limit = (int) spinLimit.getValue();

        new Thread(() -> {
            List<Submission> unanalyzed;

            StudentItem sel = (StudentItem) cboStudent.getSelectedItem();
            if (sel != null && sel.id != 0) {
                unanalyzed = submissionDAO.findUnanalyzedByStudent(sel.id, limit);
            } else {
                unanalyzed = submissionDAO.findUnanalyzed(limit);
            }

            if (unanalyzed.isEmpty()) {
                appendLog("Khong co bai nao can phan tich them.");
                SwingUtilities.invokeLater(() -> {
                    btnStart.setEnabled(true);
                    progressBar.setValue(0);
                    progressBar.setString("Khong con bai can phan tich.");
                });
                return;
            }

            appendLog("======================================");
            appendLog("Bat dau phan tich " + unanalyzed.size() + " bai nop bang Gemini AI...");

            int total = unanalyzed.size();
            for (int i = 0; i < total; i++) {
                Submission s = unanalyzed.get(i);
                int finalI = i;
                SwingUtilities.invokeLater(() -> {
                    int pct = (int) (100.0 * finalI / total);
                    progressBar.setValue(pct);
                    progressBar.setString(String.format("%d/%d bai (%d%%)", finalI + 1, total, pct));
                });

                appendLog(String.format("\n[%d/%d] Phan tich: \"%s\" cua %s",
                        i + 1, total, s.getProblemName(), s.getStudentUsername()));

                try {
                    analyzer.analyzeSubmission(s);
                    appendLog("  -> Hoan tat.");
                } catch (Exception ex) {
                    appendLog("  -> Loi: " + ex.getMessage());
                }

                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }

            appendLog("\n======================================");
            appendLog("Phan tich xong " + total + " bai!");
            SwingUtilities.invokeLater(() -> {
                btnStart.setEnabled(true);
                progressBar.setValue(100);
                progressBar.setString("Hoan thanh!");
            });
        }, "analysis-thread").start();
    }

    private void appendLog(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private record StudentItem(int id, String name) {
        @Override public String toString() { return name; }
    }
}
