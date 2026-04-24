package com.codeanalyzer.ui.panels;

import com.codeanalyzer.database.StudentDAO;
import com.codeanalyzer.database.SubmissionDAO;
import com.codeanalyzer.model.Student;
import com.codeanalyzer.model.Submission;
import com.codeanalyzer.ui.UIConstants;
import com.codeanalyzer.ui.components.StyledButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SubmissionPanel extends JPanel {

    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final StudentDAO    studentDAO    = new StudentDAO();
    private DefaultTableModel tableModel;
    private JComboBox<StudentItem> cboStudent;
    private JTextArea sourceArea;

    public SubmissionPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(UIConstants.BIG_PADDING, UIConstants.BIG_PADDING,
                                   UIConstants.BIG_PADDING, UIConstants.BIG_PADDING));
        setBackground(UIConstants.BACKGROUND);

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildSplitPane(), BorderLayout.CENTER);

        loadStudentCombo();
        refreshData();
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bar.setOpaque(false);

        cboStudent = new JComboBox<>();
        cboStudent.setPreferredSize(new Dimension(200, 28));
        cboStudent.setFont(UIConstants.MAIN);

        JLabel lbl = new JLabel("Sinh vien:");
        lbl.setFont(UIConstants.SUBHEADER);
        lbl.setForeground(UIConstants.TEXT);
        bar.add(lbl);
        bar.add(cboStudent);

        StyledButton btnRefresh = new StyledButton("Lam moi", StyledButton.Variant.NEUTRAL);
        btnRefresh.addActionListener(e -> refreshData());
        bar.add(btnRefresh);

        return bar;
    }

    private JSplitPane buildSplitPane() {
        String[] cols = {"ID", "Username", "Problem ID", "Ten bai", "Ngon ngu", "Verdict", "Thoi gian nop"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setFont(UIConstants.MAIN);
        table.setGridColor(UIConstants.BORDER);
        table.setSelectionBackground(UIConstants.PRIMARY_LIGHT);
        table.setSelectionForeground(UIConstants.TEXT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(UIConstants.SUBHEADER);
        table.getTableHeader().setBackground(UIConstants.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showSourceCode(table.getSelectedRow());
        });

        sourceArea = new JTextArea();
        sourceArea.setEditable(false);
        sourceArea.setFont(UIConstants.MONO);
        sourceArea.setTabSize(4);
        sourceArea.setBackground(UIConstants.CONSOLE_BG);
        sourceArea.setForeground(UIConstants.CONSOLE_FG);
        sourceArea.setCaretColor(UIConstants.CONSOLE_FG);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(UIConstants.CARD_BG);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER),
            new EmptyBorder(8, 8, 8, 8)));

        JLabel lbl = new JLabel("Source Code");
        lbl.setFont(UIConstants.SUBHEADER);
        lbl.setForeground(UIConstants.PRIMARY_DARK);
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));

        rightPanel.add(lbl, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(sourceArea), BorderLayout.CENTER);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(UIConstants.CARD_BG);
        leftPanel.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));
        leftPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftPanel, rightPanel);
        split.setResizeWeight(0.5);
        return split;
    }

    private void loadStudentCombo() {
        cboStudent.removeAllItems();
        cboStudent.addItem(new StudentItem(0, "-- Tat ca --"));
        for (Student s : studentDAO.findAll()) {
            cboStudent.addItem(new StudentItem(s.getId(), s.getUsername()));
        }
    }

    private void refreshData() {
        tableModel.setRowCount(0);
        sourceArea.setText("");

        StudentItem selected = (StudentItem) cboStudent.getSelectedItem();
        List<Submission> list;
        if (selected == null || selected.id == 0) {
            list = submissionDAO.findAll(200);
        } else {
            list = submissionDAO.findByStudentId(selected.id);
        }

        for (Submission s : list) {
            tableModel.addRow(new Object[]{
                s.getId(),
                s.getStudentUsername(),
                s.getProblemId(),
                s.getProblemName(),
                s.getLanguage(),
                s.getVerdict(),
                s.getSubmittedAt() != null ? s.getSubmittedAt().toString() : "-"
            });
        }
    }

    private void showSourceCode(int row) {
        if (row < 0) { sourceArea.setText(""); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        StudentItem sel = (StudentItem) cboStudent.getSelectedItem();
        List<Submission> list = (sel == null || sel.id == 0)
            ? submissionDAO.findAll(200)
            : submissionDAO.findByStudentId(sel.id);

        list.stream().filter(s -> s.getId() == id).findFirst().ifPresent(s -> {
            String code = s.getSourceCode();
            sourceArea.setText(code != null ? code : "(Khong co source code)");
            sourceArea.setCaretPosition(0);
        });
    }

    private record StudentItem(int id, String name) {
        @Override public String toString() { return name; }
    }
}
