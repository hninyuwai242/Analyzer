package branch;

import server.StockInterface;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Professional Dashboard Panel with Glassmorphism and Modern UI/UX.
 * Daily Revenue displayed in MMK (Ks) with Safe Number Parsing & Auto-Refresh.
 */
public class DashboardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final StockInterface service;
    private final int branchId;
    private final Consumer<Boolean> onViewInventory;

    private JLabel lblBranchName, lblTotalStock, lblLowStockCount, lblRecentSales;
    private JButton btnRefresh;
    private float animationAlpha = 0f;
    private Timer autoRefreshTimer;

    // Font Configuration with Fallback
    private final String MAIN_FONT = "SansSerif";

    // --- Modern Design System (Indigo/Slate Theme) ---
    private final Color CLR_BG_TOP = new Color(248, 250, 252);
    private final Color CLR_BG_BOTTOM = new Color(226, 232, 240);
    private final Color CLR_ACCENT = new Color(79, 70, 229);    // Indigo 600
    private final Color CLR_DANGER = new Color(239, 68, 68);    // Red 500
    private final Color CLR_SUCCESS = new Color(16, 185, 129);  // Emerald 500
    private final Color CLR_CARD_BG = new Color(255, 255, 255, 210);
    private final Color CLR_TEXT_HEAD = new Color(15, 23, 42);   // Slate 900
    private final Color CLR_TEXT_BODY = new Color(71, 85, 105);  // Slate 600

    public DashboardPanel(StockInterface service, int branchId, Consumer<Boolean> onViewInventory) {
        this.service = service;
        this.branchId = branchId;
        this.onViewInventory = onViewInventory;

        setLayout(new BorderLayout());
        setOpaque(false);
        
        initUI();
        refreshStats();
        startEntranceAnimation();
        setupAutoRefresh();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Background Gradient
        GradientPaint gp = new GradientPaint(0, 0, CLR_BG_TOP, 0, getHeight(), CLR_BG_BOTTOM);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // Decorative background shapes
        g2.setColor(new Color(99, 102, 241, 35));
        g2.fillOval(getWidth() - 250, -100, 400, 400);
        g2.setColor(new Color(16, 185, 129, 25));
        g2.fillOval(-150, getHeight() - 250, 500, 500);
        
        g2.dispose();
    }

    private void initUI() {
        // --- 1. Header Section ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(30, 50, 10, 50));

        JPanel titleGroup = new JPanel(new GridLayout(3, 1, 0, 2));
        titleGroup.setOpaque(false);
        
        // Dynamic Branch Name Label (အပေါ်ဆုံးတွင် Font Size ကြီးကြီးဖြင့် ထားရှိသည်)
        lblBranchName = new JLabel("Loading Branch...");
        lblBranchName.setFont(new Font(MAIN_FONT, Font.BOLD, 36));
        lblBranchName.setForeground(CLR_TEXT_HEAD);

        // Subtitle (Branch Overview)
        JLabel lblGreet = new JLabel("Branch Overview");
        lblGreet.setFont(new Font(MAIN_FONT, Font.BOLD, 18));
        lblGreet.setForeground(CLR_ACCENT);

        // Date Display
        JLabel lblDate = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        lblDate.setFont(new Font(MAIN_FONT, Font.BOLD, 14));
        lblDate.setForeground(CLR_TEXT_BODY);
        
        // နေရာ အစီအစဉ် ပြောင်းလဲခြင်း: Branch Name -> Branch Overview -> Date
        titleGroup.add(lblBranchName);
        titleGroup.add(lblGreet);
        titleGroup.add(lblDate);
        headerPanel.add(titleGroup, BorderLayout.WEST);

        // --- Refresh Button ---
        btnRefresh = new JButton("🔄 Sync Data");
        btnRefresh.setFont(new Font(MAIN_FONT, Font.BOLD, 13));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> refreshStats());
        
        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnRefresh);
        headerPanel.add(btnWrapper, BorderLayout.EAST);

        // --- 2. KPI Cards Grid ---
        JPanel cardGrid = new JPanel(new GridLayout(1, 3, 30, 0));
        cardGrid.setOpaque(false);
        cardGrid.setBorder(new EmptyBorder(20, 50, 20, 50));

        lblTotalStock = new JLabel("-");
        lblLowStockCount = new JLabel("-");
        lblRecentSales = new JLabel("- Ks");

        cardGrid.add(createModernCard("📦", "Total Items", lblTotalStock, "Units in stock", CLR_ACCENT, false));
        cardGrid.add(createModernCard("⚠️", "Critical Alerts", lblLowStockCount, "Below min limit", CLR_DANGER, true));
        cardGrid.add(createModernCard("📈", "Daily Revenue", lblRecentSales, "Settled today", CLR_SUCCESS, false));

        // --- 3. Bottom Glass Panel ---
        JPanel bottomWrapper = new JPanel(new BorderLayout());
        bottomWrapper.setOpaque(false);
        bottomWrapper.setBorder(new EmptyBorder(0, 50, 40, 50));

        JPanel glassPanel = new JPanel(new BorderLayout()) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f * animationAlpha));
                
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                
                g2.setStroke(new BasicStroke(1.2f));
                g2.setColor(new Color(255, 255, 255, 180));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 30, 30);
                
                g2.dispose();
            }
        };
        glassPanel.setOpaque(false);
        glassPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel lblInfoTitle = new JLabel("System Vitality & Synchronization");
        lblInfoTitle.setFont(new Font(MAIN_FONT, Font.BOLD, 18));
        lblInfoTitle.setForeground(CLR_TEXT_HEAD);
        
        JPanel detailsGrid = new JPanel(new GridLayout(1, 3, 40, 0));
        detailsGrid.setOpaque(false);
        detailsGrid.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        detailsGrid.add(createInsightItem("Cloud Sync", "Active", CLR_SUCCESS));
        detailsGrid.add(createInsightItem("Performance", "High", CLR_ACCENT));
        detailsGrid.add(createInsightItem("Update Interval", "30 Secs", CLR_TEXT_BODY));

        glassPanel.add(lblInfoTitle, BorderLayout.NORTH);
        glassPanel.add(detailsGrid, BorderLayout.CENTER);
        bottomWrapper.add(glassPanel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(cardGrid, BorderLayout.CENTER);
        add(bottomWrapper, BorderLayout.SOUTH);
    }

    private JPanel createModernCard(String icon, String title, JLabel valLabel, String footer, Color themeColor, boolean isAlert) {
        JPanel card = new JPanel(new BorderLayout()) {
            private static final long serialVersionUID = 1L;
            private boolean hovered = false;
            
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                    public void mousePressed(MouseEvent e) { onViewInventory.accept(isAlert); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, animationAlpha));
                
                g2.setColor(new Color(0, 0, 0, hovered ? 25 : 12));
                g2.fillRoundRect(hovered ? 2 : 4, hovered ? 4 : 6, getWidth()-(hovered?4:8), getHeight()-(hovered?4:8), 35, 35);

                g2.setColor(hovered ? Color.WHITE : CLR_CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 35, 35);
                
                g2.setColor(themeColor);
                g2.fillRoundRect(25, 30, 4, 25, 2, 2);
                
                if (hovered) {
                    g2.setColor(new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 30));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 35, 35);
                }

                g2.dispose();
            }
        };
        
        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel centerContent = new JPanel(new GridLayout(2, 1, 0, 2));
        centerContent.setOpaque(false);
        
        JLabel t = new JLabel(title);
        t.setFont(new Font(MAIN_FONT, Font.BOLD, 13));
        t.setForeground(CLR_TEXT_BODY);
        
        valLabel.setFont(new Font(MAIN_FONT, Font.BOLD, 28));
        valLabel.setForeground(CLR_TEXT_HEAD);
        
        centerContent.add(t);
        centerContent.add(valLabel);

        JLabel f = new JLabel(footer);
        f.setFont(new Font(MAIN_FONT, Font.BOLD, 12));
        f.setForeground(themeColor);
        
        JLabel i = new JLabel(icon);
        i.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        i.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(i, BorderLayout.EAST);

        card.add(topRow, BorderLayout.NORTH);
        card.add(centerContent, BorderLayout.CENTER);
        card.add(f, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createInsightItem(String label, String value, Color color) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        
        JLabel l = new JLabel(label);
        l.setFont(new Font(MAIN_FONT, Font.BOLD, 12));
        l.setForeground(CLR_TEXT_BODY);
        
        JLabel v = new JLabel(value);
        v.setFont(new Font(MAIN_FONT, Font.BOLD, 16));
        v.setForeground(color);
        
        p.add(l, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private void startEntranceAnimation() {
        Timer timer = new Timer(15, e -> {
            animationAlpha += 0.05f;
            if (animationAlpha >= 1f) {
                animationAlpha = 1f;
                ((Timer)e.getSource()).stop();
            }
            repaint();
        });
        timer.start();
    }

    private void setupAutoRefresh() {
        autoRefreshTimer = new Timer(30000, e -> refreshStats());
        autoRefreshTimer.start();
    }

    // --- REFRESH STATS & FETCH BRANCH NAME METHOD ---
    private void refreshStats() {
        new SwingWorker<Void, Void>() {
            private String branchName = "";
            private int total = 0;
            private long lowStock = 0;
            private double dailyTotal = 0;
            private boolean isError = false;

            @Override
            protected Void doInBackground() {
                try {
                    // 1. Fetch Branch Name via RMI
                    branchName = service.getBranchName(branchId);

                    // 2. Stock Statistics
                    List<Map<String, Object>> stocks = service.getBranchStock(branchId);
                    if (stocks != null) {
                        total = stocks.size();
                        lowStock = stocks.stream()
                                .filter(s -> {
                                    if (s.get("quantity") == null || s.get("min_limit") == null) return false;
                                    int q = (int) Math.round(Double.parseDouble(s.get("quantity").toString()));
                                    int m = (int) Math.round(Double.parseDouble(s.get("min_limit").toString()));
                                    return q < m;
                                })
                                .count();
                    }

                    // 3. Daily Sales Calculation
                    String today = LocalDate.now().toString(); // YYYY-MM-DD
                    List<Map<String, Object>> reportData = service.getSalesReport(branchId, "DAILY", today);
                    
                    if (reportData != null) {
                        for (Map<String, Object> row : reportData) {
                            if (row.get("total") != null) {
                                double total = Double.parseDouble(row.get("total").toString());
                                dailyTotal += total;
                            }
                        }
                    }
                } catch (Exception e) {
                    isError = true;
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                if (isError) {
                    lblBranchName.setText("Branch " + branchId);
                    lblTotalStock.setText("Err");
                    lblLowStockCount.setText("Err");
                    lblRecentSales.setText("Err");
                } else {
                    lblBranchName.setText(branchName);
                    lblTotalStock.setText(String.valueOf(total));
                    lblLowStockCount.setText(String.valueOf(lowStock));
                    lblRecentSales.setText(String.format("%,.0f Ks", dailyTotal));
                }
            }
        }.execute();
    }
}