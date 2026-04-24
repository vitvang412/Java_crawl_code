package com.codeanalyzer.ui.panels;

import com.codeanalyzer.crawler.CodeforceCrawler;
import com.codeanalyzer.database.StudentDAO;
import com.codeanalyzer.model.PlatformType;
import com.codeanalyzer.model.Student;
import com.codeanalyzer.ui.UIConstants;
import com.codeanalyzer.ui.components.StyledButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class StudentManagementPanel extends JPanel {

    private final StudentDAO       studentDAO = new StudentDAO();
    private       JTable           table;
    private       DefaultTableModel tableModel;
    private       JTextArea        logArea;

    public StudentManagementPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(UIConstants.BIG_PADDING, UIConstants.BIG_PADDING,
                                   UIConstants.BIG_PADDING, UIConstants.BIG_PADDING));
        setBackground(UIConstants.BACKGROUND);

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
        add(buildLogPanel(), BorderLayout.SOUTH);

        refreshData();
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        StyledButton btnAdd     = new StyledButton("Them sinh vien", StyledButton.Variant.PRIMARY);
        StyledButton btnRefresh = new StyledButton("Lam moi",        StyledButton.Variant.NEUTRAL);
        StyledButton btnCrawl   = new StyledButton("Bat dau cao (Semi-Auto)", StyledButton.Variant.ACCENT);
        StyledButton btnDelete  = new StyledButton("Xoa",            StyledButton.Variant.DANGER);

        toolbar.add(btnAdd);
        toolbar.add(btnRefresh);
        toolbar.add(btnCrawl);
        toolbar.add(btnDelete);

        btnAdd.addActionListener(e -> addStudent());
        btnRefresh.addActionListener(e -> refreshData());
        btnCrawl.addActionListener(e -> crawlSelected());
        btnDelete.addActionListener(e -> deleteSelected());

        return toolbar;
    }

    private JPanel buildCenter() {
        String[] columns = {"ID", "Username", "Platform", "Them luc", "Cao lan cuoi", "Hoat dong"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.setFont(UIConstants.MAIN);
        table.setGridColor(UIConstants.BORDER);
        table.setSelectionBackground(UIConstants.PRIMARY_LIGHT);
        table.setSelectionForeground(UIConstants.TEXT);
        table.getTableHeader().setFont(UIConstants.SUBHEADER);
        table.getTableHeader().setBackground(UIConstants.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(5).setCellRenderer(center);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UIConstants.CARD_BG);
        card.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildLogPanel() {
        logArea = new JTextArea(5, 0);
        logArea.setEditable(false);
        logArea.setFont(UIConstants.MONO);
        logArea.setBackground(UIConstants.CONSOLE_BG);
        logArea.setForeground(UIConstants.CONSOLE_FG);
        logArea.setCaretColor(UIConstants.CONSOLE_FG);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER),
            new EmptyBorder(8, 8, 8, 8)));
        panel.setBackground(UIConstants.CARD_BG);

        JLabel lbl = new JLabel("Log cao du lieu");
        lbl.setFont(UIConstants.SUBHEADER);
        lbl.setForeground(UIConstants.PRIMARY_DARK);
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        return panel;
    }

    private void addStudent() {
        String username = JOptionPane.showInputDialog(this,
                "Nhap username Codeforces:", "Them sinh vien", JOptionPane.PLAIN_MESSAGE);
        if (username == null || username.isBlank()) return;

        Student s = new Student(username.trim(), PlatformType.CODEFORCES);
        int id = studentDAO.save(s);
        if (id > 0) {
            appendLog("Da them sinh vien: " + username);
            refreshData();
        } else {
            appendLog("Username da ton tai hoac co loi khi luu.");
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chon 1 sinh vien truoc!"); return; }

        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xoa sinh vien \"" + name + "\" khoi danh sach?",
                "Xac nhan xoa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            studentDAO.deactivate(id);
            appendLog("Da an sinh vien: " + name);
            refreshData();
        }
    }

    private void crawlSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui long chon 1 sinh vien!");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        Student s = studentDAO.findById(id);
        if (s == null) { appendLog("Khong tim thay sinh vien!"); return; }

        appendLog("======================================");
        appendLog("Bat dau cao cho: " + s.getUsername());

        new Thread(() -> {
            CodeforceCrawler crawler = new CodeforceCrawler();
            crawler.crawlForStudent(s, msg -> SwingUtilities.invokeLater(() -> appendLog(msg)));
            SwingUtilities.invokeLater(this::refreshData);
        }, "crawler-thread").start();
    }

    void refreshData() {
        tableModel.setRowCount(0);
        List<Student> students = studentDAO.findAll();
        for (Student s : students) {
            tableModel.addRow(new Object[]{
                s.getId(),
                s.getUsername(),
                s.getPlatform() != null ? s.getPlatform().getDisplayName() : "?",
                s.getAddedAt() != null ? s.getAddedAt().toString() : "-",
                s.getLastCrawledAt() != null ? s.getLastCrawledAt().toString() : "-",
                s.isActive() ? "Co" : "Khong"
            });
        }
    }

    private void appendLog(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}
