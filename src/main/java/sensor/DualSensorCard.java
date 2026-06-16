package sensor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 双值传感器卡片类
 * 左右并排显示两个传感器数值（如左前轮速/右前轮速）
 * 带有统计信息显示
 */
public class DualSensorCard extends JPanel {
    private JLabel nameLabel;
    private JLabel leftValueLabel;
    private JLabel rightValueLabel;
    private JLabel leftStatusLabel;
    private JLabel rightStatusLabel;
    private JLabel historyLabel;

    private String unit;
    private Color themeColor;

    private List<Double> recentLeftValues;
    private List<Double> recentRightValues;

    /**
     * 构造函数
     * @param name 传感器名称
     * @param leftLabel 左侧标签名
     * @param rightLabel 右侧标签名
     * @param unit 单位
     * @param themeColor 主题颜色
     */
    public DualSensorCard(String name, String leftLabel, String rightLabel, String unit, Color themeColor) {
        this.unit = unit;
        this.themeColor = themeColor;
        this.recentLeftValues = new ArrayList<>();
        this.recentRightValues = new ArrayList<>();

        initUI(name, leftLabel, rightLabel);
    }

    /**
     * 初始化UI组件
     */
    private void initUI(String name, String leftLabel, String rightLabel) {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(themeColor, 3),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(300, 200));

        nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
        nameLabel.setForeground(themeColor.darker());
        add(nameLabel, BorderLayout.NORTH);

        JPanel valuesPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        valuesPanel.setOpaque(false);

        leftValueLabel = createValueLabel();
        leftStatusLabel = createStatusLabel();
        rightValueLabel = createValueLabel();
        rightStatusLabel = createStatusLabel();

        valuesPanel.add(createColumn(leftLabel, leftValueLabel, leftStatusLabel));
        valuesPanel.add(createColumn(rightLabel, rightValueLabel, rightStatusLabel));

        add(valuesPanel, BorderLayout.CENTER);

        historyLabel = new JLabel("等待数据...");
        historyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        historyLabel.setForeground(new Color(100, 100, 100));
        historyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(historyLabel, BorderLayout.SOUTH);
    }

    /**
     * 创建单列面板（标签 + 数值 + 单位）
     */
    private JPanel createColumn(String position, JLabel valueLbl, JLabel unitLbl) {
        JPanel column = new JPanel(new BorderLayout(2, 2));
        column.setOpaque(false);

        JLabel posLabel = new JLabel(position, SwingConstants.CENTER);
        posLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        posLabel.setForeground(Color.GRAY);
        column.add(posLabel, BorderLayout.NORTH);

        column.add(valueLbl, BorderLayout.CENTER);
        column.add(unitLbl, BorderLayout.SOUTH);

        return column;
    }

    /**
     * 创建数值标签
     */
    private JLabel createValueLabel() {
        JLabel label = new JLabel("--");
        label.setFont(new Font("微软雅黑", Font.BOLD, 30));
        label.setForeground(themeColor.darker().darker());
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    /**
     * 创建单位标签
     */
    private JLabel createStatusLabel() {
        JLabel label = new JLabel(unit);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        label.setForeground(Color.GRAY);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    /**
     * 记录历史值（保留最近10个）
     */
    private void addRecent(List<Double> list, double value) {
        list.add(value);
        if (list.size() > 10) {
            list.remove(0);
        }
    }

    /**
     * 更新左侧数值
     * @param value 数值
     */
    public void updateLeftValue(double value) {
        addRecent(recentLeftValues, value);
        leftValueLabel.setText(String.format("%.1f", value));
        updateHistory();
    }

    /**
     * 更新右侧数值
     * @param value 数值
     */
    public void updateRightValue(double value) {
        addRecent(recentRightValues, value);
        rightValueLabel.setText(String.format("%.1f", value));
        updateHistory();
    }

    /**
     * 更新底部历史统计信息
     */
    private void updateHistory() {
        List<Double> allValues = new ArrayList<>();
        allValues.addAll(recentLeftValues);
        allValues.addAll(recentRightValues);

        if (allValues.isEmpty()) return;

        double min = allValues.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = allValues.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double avg = allValues.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        historyLabel.setText(String.format("最小:%.1f | 最大:%.1f | 平均:%.1f", min, max, avg));
    }

    /**
     * 获取主题颜色
     * @return 主题颜色
     */
    public Color getThemeColor() {
        return themeColor;
    }
}
