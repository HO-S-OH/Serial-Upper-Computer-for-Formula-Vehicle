package sensor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 三轴传感器卡片类（矢量箭头可视化版）
 * 用于显示加速度、陀螺仪、磁力计的X/Y/Z三个轴向数据
 * 中央显示矢量箭头，周围显示各轴数值
 */
public class TriAxisSensorCard extends JPanel {
    private JLabel nameLabel;
    private JLabel xValueLabel;
    private JLabel yValueLabel;
    private JLabel zValueLabel;
    private JLabel historyLabel;
    
    private VectorArrowPanel vectorPanel;

    private String unit;
    private Color themeColor;

    private List<Double> recentXValues;
    private List<Double> recentYValues;
    private List<Double> recentZValues;
    
    private double currentX = 0;
    private double currentY = 0;
    private double currentZ = 0;

    /**
     * 构造函数
     * @param name 传感器名称
     * @param unit 单位
     * @param themeColor 主题颜色
     */
    public TriAxisSensorCard(String name, String unit, Color themeColor) {
        this.unit = unit;
        this.themeColor = themeColor;
        this.recentXValues = new ArrayList<>();
        this.recentYValues = new ArrayList<>();
        this.recentZValues = new ArrayList<>();

        initUI(name);
    }

    /**
     * 初始化UI组件
     */
    private void initUI(String name) {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(themeColor, 3),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(320, 240));

        nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
        nameLabel.setForeground(themeColor.darker());
        add(nameLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 0));
        centerPanel.setOpaque(false);

        vectorPanel = new VectorArrowPanel();
        centerPanel.add(vectorPanel, BorderLayout.CENTER);

        JPanel valuesPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        valuesPanel.setOpaque(false);
        valuesPanel.setPreferredSize(new Dimension(120, 0));

        xValueLabel = createAxisLabel("X轴");
        yValueLabel = createAxisLabel("Y轴");
        zValueLabel = createAxisLabel("Z轴");

        valuesPanel.add(xValueLabel);
        valuesPanel.add(yValueLabel);
        valuesPanel.add(zValueLabel);

        centerPanel.add(valuesPanel, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);

        historyLabel = new JLabel("等待数据...");
        historyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        historyLabel.setForeground(new Color(100, 100, 100));
        historyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(historyLabel, BorderLayout.SOUTH);
    }

    /**
     * 创建轴向标签
     */
    private JLabel createAxisLabel(String axisName) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setOpaque(false);

        JLabel label = new JLabel(axisName);
        label.setFont(new Font("微软雅黑", Font.BOLD, 12));
        label.setForeground(themeColor.darker());
        label.setPreferredSize(new Dimension(40, 25));
        panel.add(label, BorderLayout.WEST);

        JLabel valueLabel = new JLabel("-- " + unit);
        valueLabel.setFont(new Font("Consolas", Font.BOLD, 14));
        valueLabel.setForeground(new Color(60, 60, 60));
        panel.add(valueLabel, BorderLayout.CENTER);

        return valueLabel;
    }

    /**
     * 记录历史值（保留最近10个）
     */
    private void addRecent(List<Double> list, double value) {
        list.add(value);
        if (list.size() > 10) {
            list.removeFirst();
        }
    }

    /**
     * 更新X轴数值
     * @param value 数值
     */
    public void updateXValue(double value) {
        currentX = value;
        addRecent(recentXValues, value);
        xValueLabel.setText(String.format("%.2f %s", value, unit));
        vectorPanel.repaint();
        updateHistory();
    }

    /**
     * 更新Y轴数值
     * @param value 数值
     */
    public void updateYValue(double value) {
        currentY = value;
        addRecent(recentYValues, value);
        yValueLabel.setText(String.format("%.2f %s", value, unit));
        vectorPanel.repaint();
        updateHistory();
    }

    /**
     * 更新Z轴数值
     * @param value 数值
     */
    public void updateZValue(double value) {
        currentZ = value;
        addRecent(recentZValues, value);
        zValueLabel.setText(String.format("%.2f %s", value, unit));
        vectorPanel.repaint();
        updateHistory();
    }

    /**
     * 更新底部历史统计信息
     */
    protected void updateHistory() {
        List<Double> allValues = new ArrayList<>();
        allValues.addAll(recentXValues);
        allValues.addAll(recentYValues);
        allValues.addAll(recentZValues);

        if (allValues.isEmpty()) return;

        double min = allValues.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = allValues.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double avg = allValues.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        historyLabel.setText(String.format("最小:%.2f | 最大:%.2f | 平均:%.2f", min, max, avg));
    }

    /**
     * 矢量箭头绘制面板
     */
    private class VectorArrowPanel extends JPanel {
        public VectorArrowPanel() {
            setPreferredSize(new Dimension(150, 150));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radius = Math.min(getWidth(), getHeight()) / 2 - 20;

            // 绘制背景圆
            g2d.setColor(new Color(240, 240, 240));
            g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            g2d.setColor(new Color(200, 200, 200));
            g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

            // 绘制坐标轴
            g2d.setStroke(new BasicStroke(1));
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawLine(centerX - radius, centerY, centerX + radius, centerY);
            g2d.drawLine(centerX, centerY - radius, centerX, centerY + radius);

            // 标注轴向
            g2d.setFont(new Font("微软雅黑", Font.BOLD, 10));
            g2d.setColor(new Color(255, 87, 34));
            g2d.drawString("X+", centerX + radius - 15, centerY - 5);
            g2d.setColor(new Color(33, 150, 243));
            g2d.drawString("Y+", centerX + 5, centerY - radius + 15);

            // 计算矢量在XY平面的投影
            double magnitude = Math.sqrt(currentX * currentX + currentY * currentY + currentZ * currentZ);
            if (magnitude < 0.01) return;

            double normalizedX = currentX / magnitude;
            double normalizedY = currentY / magnitude;

            int arrowLength = (int) (radius * 0.8);
            int endX = centerX + (int) (normalizedX * arrowLength);
            int endY = centerY - (int) (normalizedY * arrowLength);

            // 绘制箭头
            g2d.setColor(themeColor);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawLine(centerX, centerY, endX, endY);

            // 箭头头部
            double angle = Math.atan2(centerY - endY, endX - centerX);
            int headLength = 10;
            
            int[] xPoints = {
                endX,
                endX + (int) (headLength * Math.cos(angle - Math.PI / 6)),
                endX + (int) (headLength * Math.cos(angle + Math.PI / 6))
            };
            int[] yPoints = {
                endY,
                endY - (int) (headLength * Math.sin(angle - Math.PI / 6)),
                endY - (int) (headLength * Math.sin(angle + Math.PI / 6))
            };
            
            g2d.fillPolygon(xPoints, yPoints, 3);

            // 显示合成矢量大小
            g2d.setColor(new Color(60, 60, 60));
            g2d.setFont(new Font("Consolas", Font.BOLD, 11));
            g2d.drawString(String.format("|V|=%.2f", magnitude), centerX - 30, centerY + radius + 15);
        }
    }
}
