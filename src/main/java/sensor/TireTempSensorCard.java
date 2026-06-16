package sensor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 轮胎温度传感器卡片类
 * 每个轮胎显示三个温度探头数据：左侧、中间、右侧
 * 带有温度颜色指示（低温绿色→中温黄色→高温红色）
 */
public class TireTempSensorCard extends JPanel {
    private JLabel nameLabel;
    private JLabel leftValueLabel;
    private JLabel centerValueLabel;
    private JLabel rightValueLabel;
    private JLabel leftStatusLabel;
    private JLabel centerStatusLabel;
    private JLabel rightStatusLabel;
    private JLabel historyLabel;

    private String tireName;
    private Color themeColor;

    private List<Double> recentLeftValues;
    private List<Double> recentCenterValues;
    private List<Double> recentRightValues;

    private static final double TEMP_WARN = 80.0;
    private static final double TEMP_DANGER = 110.0;

    /**
     * 构造函数
     * @param tireName 轮胎名称（如"左前轮"）
     * @param themeColor 主题颜色
     */
    public TireTempSensorCard(String tireName, Color themeColor) {
        this.tireName = tireName;
        this.themeColor = themeColor;
        this.recentLeftValues = new ArrayList<>();
        this.recentCenterValues = new ArrayList<>();
        this.recentRightValues = new ArrayList<>();

        initUI();
    }

    /**
     * 初始化UI组件
     */
    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(themeColor, 3),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(300, 220));

        nameLabel = new JLabel( tireName + " 胎温");
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
        nameLabel.setForeground(themeColor.darker());
        add(nameLabel, BorderLayout.NORTH);

        JPanel valuesPanel = new JPanel(new GridLayout(1, 3, 6, 0));
        valuesPanel.setOpaque(false);

        leftValueLabel = createValueLabel();
        leftStatusLabel = createStatusLabel();
        centerValueLabel = createValueLabel();
        centerStatusLabel = createStatusLabel();
        rightValueLabel = createValueLabel();
        rightStatusLabel = createStatusLabel();

        valuesPanel.add(createTempColumn("左侧", leftValueLabel, leftStatusLabel));
        valuesPanel.add(createTempColumn("中间", centerValueLabel, centerStatusLabel));
        valuesPanel.add(createTempColumn("右侧", rightValueLabel, rightStatusLabel));

        add(valuesPanel, BorderLayout.CENTER);

        historyLabel = new JLabel("等待数据...");
        historyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        historyLabel.setForeground(new Color(100, 100, 100));
        historyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(historyLabel, BorderLayout.SOUTH);
    }

    /**
     * 创建温度列面板（标签 + 数值 + 状态）
     */
    private JPanel createTempColumn(String position, JLabel valueLbl, JLabel statusLbl) {
        JPanel column = new JPanel(new BorderLayout(2, 2));
        column.setOpaque(false);

        JLabel posLabel = new JLabel(position, SwingConstants.CENTER);
        posLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        posLabel.setForeground(Color.GRAY);
        column.add(posLabel, BorderLayout.NORTH);

        column.add(valueLbl, BorderLayout.CENTER);

        column.add(statusLbl, BorderLayout.SOUTH);

        return column;
    }

    /**
     * 创建数值标签
     */
    private JLabel createValueLabel() {
        JLabel label = new JLabel("--");
        label.setFont(new Font("微软雅黑", Font.BOLD, 26));
        label.setForeground(new Color(60, 60, 60));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    /**
     * 创建状态标签
     */
    private JLabel createStatusLabel() {
        JLabel label = new JLabel("°C");
        label.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        label.setForeground(Color.GRAY);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    /**
     * 根据温度返回颜色
     * 低温(<80°C)=绿色, 中温(80~110°C)=橙色, 高温(>110°C)=红色
     */
    private Color getTempColor(double temp) {
        if (temp >= TEMP_DANGER) {
            return new Color(220, 53, 69);
        } else if (temp >= TEMP_WARN) {
            return new Color(255, 152, 0);
        } else {
            return new Color(40, 167, 69);
        }
    }

    /**
     * 根据温度返回状态文字
     */
    private String getTempStatus(double temp) {
        if (temp >= TEMP_DANGER) {
            return "⚠ 高温";
        } else if (temp >= TEMP_WARN) {
            return "▲ 中温";
        } else {
            return "● 正常";
        }
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
     * 更新左侧温度值
     * @param value 温度值
     */
    public void updateLeftTemp(double value) {
        addRecent(recentLeftValues, value);
        leftValueLabel.setText(String.format("%.1f", value));
        leftValueLabel.setForeground(getTempColor(value));
        leftStatusLabel.setText(getTempStatus(value));
        leftStatusLabel.setForeground(getTempColor(value));
        updateHistory();
    }

    /**
     * 更新中间温度值
     * @param value 温度值
     */
    public void updateCenterTemp(double value) {
        addRecent(recentCenterValues, value);
        centerValueLabel.setText(String.format("%.1f", value));
        centerValueLabel.setForeground(getTempColor(value));
        centerStatusLabel.setText(getTempStatus(value));
        centerStatusLabel.setForeground(getTempColor(value));
        updateHistory();
    }

    /**
     * 更新右侧温度值
     * @param value 温度值
     */
    public void updateRightTemp(double value) {
        addRecent(recentRightValues, value);
        rightValueLabel.setText(String.format("%.1f", value));
        rightValueLabel.setForeground(getTempColor(value));
        rightStatusLabel.setText(getTempStatus(value));
        rightStatusLabel.setForeground(getTempColor(value));
        updateHistory();
    }

    /**
     * 更新底部历史统计信息
     */
    private void updateHistory() {
        List<Double> allValues = new ArrayList<>();
        allValues.addAll(recentLeftValues);
        allValues.addAll(recentCenterValues);
        allValues.addAll(recentRightValues);

        if (allValues.isEmpty()) return;

        double min = allValues.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = allValues.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double avg = allValues.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        historyLabel.setText(String.format("最小:%.1f°C | 最大:%.1f°C | 平均:%.1f°C", min, max, avg));
    }

    /**
     * 获取主题颜色
     * @return 主题颜色
     */
    public Color getThemeColor() {
        return themeColor;
    }
}
