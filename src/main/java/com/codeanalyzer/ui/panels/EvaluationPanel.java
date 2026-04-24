package com.codeanalyzer.ui.panels;

import com.codeanalyzer.ai.StudentEvaluator;
import com.codeanalyzer.database.AnalysisResultDAO;
import com.codeanalyzer.database.StudentEvaluationDAO;
import com.codeanalyzer.model.AnalysisResult;
import com.codeanalyzer.model.StudentEvaluation;
import com.codeanalyzer.ui.UIConstants;
import com.codeanalyzer.ui.components.StyledButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EvaluationPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final StudentEvaluationDAO evalDAO      = new StudentEvaluationDAO();
    private final AnalysisResultDAO    analysisDAO  = new AnalysisResultDAO();
    private final StudentEvaluator     evaluator    = new StudentEvaluator();

    private final DefaultTableModel tableModel;
    private final JTable            table;

    public EvaluationPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(UIConstants.BIG_PADDING, UIConstants.BIG_PADDING,
                                   UIConstants.BIG_PADDING, UIConstants.BIG_PADDING));
        setBackground(UIConstants.BACKGROUND);

        String[] cols = {
                "Username", "Bai da phan tich",
                "DSA Score (0-10)", "AI Dep (0-10)",
                "Top Thuat Toan", "Top CTDL",
                "Xep hang", "Cap nhat"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(UIConstants.MAIN);
        table.getTableHeader().setFont(UIConstants.SUBHEADER);
        table.getTableHeader().setBackground(UIConstants.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(UIConstants.BORDER);
        table.setSelectionBackground(UIConstants.PRIMARY_LIGHT);
        table.setSelectionForeground(UIConstants.TEXT);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(1).setCellRenderer(center);
        tcm.getColumn(2).setCellRenderer(new ScoreRenderer(true));
        tcm.getColumn(3).setCellRenderer(new ScoreRenderer(false));
        tcm.getColumn(6).setCellRenderer(new LabelRenderer());

        add(buildHeader(), BorderLayout.NORTH);
        add(wrapCard(new JScrollPane(table)), BorderLayout.CENTER);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) showDetailForSelected();
            }
        });

        refreshData();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Danh gia tong the sinh vien");
        title.setFont(UIConstants.HEADER);
        title.setForeground(UIConstants.PRIMARY_DARK);

        StyledButton btnRefresh = new StyledButton("Lam moi",              StyledButton.Variant.NEUTRAL);
        StyledButton btnRecalc  = new StyledButton("Danh gia lai tat ca",  StyledButton.Variant.PRIMARY);
        StyledButton btnDetail  = new StyledButton("Xem chi tiet",         StyledButton.Variant.ACCENT);

        btnRefresh.addActionListener(e -> refreshData());
        btnRecalc.addActionListener(e -> recalcAll(btnRecalc));
        btnDetail.addActionListener(e -> showDetailForSelected());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(btnRefresh);
        right.add(btnDetail);
        right.add(btnRecalc);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel wrapCard(Component comp) {
        JPanel c = new JPanel(new BorderLayout());
        c.setBackground(UIConstants.CARD_BG);
        c.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));
        c.add(comp, BorderLayout.CENTER);
        return c;
    }

    private void refreshData() {
        tableModel.setRowCount(0);
        List<StudentEvaluation> list = evalDAO.findAll();
        for (StudentEvaluation e : list) {
            tableModel.addRow(new Object[]{
                    e.getStudentUsername(),
                    e.getTotalAnalyzed(),
                    String.format("%.2f", e.getDsaScore()),
                    String.format("%.2f", e.getAiDependencyScore()),
                    e.getTopAlgorithms(),
                    e.getTopDataStructures(),
                    e.overallLabel(),
                    e.getEvaluatedAt() == null ? "-" : e.getEvaluatedAt().format(FMT)
            });
        }
    }

    private void recalcAll(JButton src) {
        src.setEnabled(false);
        new Thread(() -> {
            try {
                List<StudentEvaluation> out = evaluator.evaluateAll();
                SwingUtilities.invokeLater(() -> {
                    refreshData();
                    JOptionPane.showMessageDialog(this,
                            "Da danh gia lai " + out.size() + " sinh vien.",
                            "Hoan tat", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                        "Loi: " + ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE));
            } finally {
                SwingUtilities.invokeLater(() -> src.setEnabled(true));
            }
        }, "evaluator-thread").start();
    }

    private void showDetailForSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chon 1 dong truoc!");
            return;
        }
        String username = (String) tableModel.getValueAt(row, 0);
        List<StudentEvaluation> all = evalDAO.findAll();
        StudentEvaluation ev = all.stream()
                .filter(e -> username.equals(e.getStudentUsername()))
                .findFirst().orElse(null);
        if (ev == null) return;

        List<AnalysisResult> details = analysisDAO.findByStudentId(ev.getStudentId());
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:SansSerif;font-size:12px;'>");
        sb.append("<h2 style='color:#C96B30;margin:0'>").append(username).append("</h2>");
        sb.append("<p><b>Xep hang:</b> ").append(ev.overallLabel())
          .append(" &nbsp; <b>DSA:</b> ").append(String.format("%.2f", ev.getDsaScore()))
          .append(" &nbsp; <b>AI dep:</b> ").append(String.format("%.2f", ev.getAiDependencyScore()))
          .append(" &nbsp; <b>Bai:</b> ").append(ev.getTotalAnalyzed())
          .append("</p>");
        sb.append("<p><b>Top thuat toan:</b> ").append(esc(ev.getTopAlgorithms())).append("</p>");
        sb.append("<p><b>Top CTDL:</b> ").append(esc(ev.getTopDataStructures())).append("</p>");
        sb.append("<hr><h3>Chi tiet tung bai (" + details.size() + ")</h3>");
        sb.append("<table border='1' cellpadding='4' cellspacing='0' style='border-collapse:collapse;'>");
        sb.append("<tr style='background:#F5C4A1'><th>Bai</th><th>AI</th><th>Do phuc tap</th>"
                + "<th>Thuat toan</th><th>CTDL</th></tr>");
        for (AnalysisResult r : details) {
            sb.append("<tr><td>").append(esc(r.getProblemName())).append("</td>")
              .append("<td align='center'>").append(r.getAiUsageScore()).append("/10</td>")
              .append("<td>").append(esc(r.getComplexityEstimate())).append("</td>")
              .append("<td>").append(esc(r.getAlgorithms())).append("</td>")
              .append("<td>").append(esc(r.getDataStructures())).append("</td></tr>");
        }
        sb.append("</table>");
        sb.append("</body></html>");

        JEditorPane pane = new JEditorPane("text/html", sb.toString());
        pane.setEditable(false);
        pane.setCaretPosition(0);
        JScrollPane sp = new JScrollPane(pane);
        sp.setPreferredSize(new Dimension(900, 500));
        JOptionPane.showMessageDialog(this, sp, "Chi tiet: " + username,
                JOptionPane.PLAIN_MESSAGE);
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("<", "&lt;").replace(">", "&gt;");
    }

    private static class ScoreRenderer extends DefaultTableCellRenderer {
        private final boolean higherIsBetter;
        ScoreRenderer(boolean higherIsBetter) {
            this.higherIsBetter = higherIsBetter;
            setHorizontalAlignment(SwingConstants.CENTER);
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                       boolean f, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, sel, f, r, c);
            if (sel) return comp;
            double val = 0;
            try { val = Double.parseDouble(String.valueOf(v)); } catch (Exception ignored) {}
            Color bg;
            if (higherIsBetter) {
                bg = val >= 6 ? new Color(0xD5E8D0)
                   : val >= 3 ? new Color(0xFFF3D0)
                              : new Color(0xF8D7D0);
            } else {
                bg = val <= 3 ? new Color(0xD5E8D0)
                   : val <= 6 ? new Color(0xFFF3D0)
                              : new Color(0xF8D7D0);
            }
            comp.setBackground(bg);
            return comp;
        }
    }

    private static class LabelRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                       boolean f, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, sel, f, r, c);
            setHorizontalAlignment(SwingConstants.CENTER);
            if (sel) return comp;
            String s = String.valueOf(v);
            Color bg = switch (s) {
                case "T\u1ed1t"                  -> new Color(0xD5E8D0);
                case "Trung b\u00ecnh"           -> new Color(0xFFF3D0);
                case "Y\u1ebfu / Nghi ng\u1edd"  -> new Color(0xF8D7D0);
                default                          -> Color.WHITE;
            };
            comp.setBackground(bg);
            return comp;
        }
    }
}
