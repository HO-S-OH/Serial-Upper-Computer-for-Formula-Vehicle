package sensor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 悬架受力传感器卡片类
 * 每个悬架显示5个受力探头数据
 * 带有受力颜色指示（低载绿色→中载橙色→过载红色）
 */
public class SuspensionLoadCard extends JPanel {
    private JLabel nameLabel;
    private JLabel[] valueLabels;
    private JLabel[] statusLabels;
    private JLabel historyLabel;

    private String suspensionName;
    private Color themeColor;

    private List<List<Double>> recentValuesList;

    private static final double LOAD_WARN = 2000.0;
    private static final double LOAD_DANGER = 4000.0;

    private static final int SENSOR_COUNT = 5;

    private static final String[] POSITION_LABELS = {"上叉臂1", "上叉臂2", "下叉臂1", "下叉臂2", "推杆"};

    /**
     * 构造函数
     * @param suspensionName 悬架名称（如"左前悬架"）
     * @param themeColor 主题颜色
     */
    public SuspensionLoadCard(String suspensionName, Color themeColor) {
        this.suspensionName = suspensionName;
        this.themeColor = themeColor;
        this.valueLabels = new JLabel[SENSOR_COUNT];
        this.statusLabels = new JLabel[SENSOR_COUNT];
        this.recentValuesList = new ArrayList<>();
        for (int i = 0; i < SENSOR_COUNT; i++) {
            recentValuesList.add(new ArrayList<>());
        }

        initUI();
    }

    /**
     * 初始化UI组件
     */
    private void initUI() {
        setLayout(new BorderLayout(6, 4));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(themeColor, 3),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(300, 240));

        nameLabel = new JLabel(suspensionName + " 受力");
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        nameLabel.setForeground(themeColor.darker());
        add(nameLabel, BorderLayout.NORTH);

        JPanel valuesPanel = new JPanel(new GridLayout(1, SENSOR_COUNT, 3, 0));
        valuesPanel.setOpaque(false);

        for (int i = 0; i < SENSOR_COUNT; i++) {
            valueLabels[i] = createValueLabel();
            statusLabels[i] = createStatusLabel();
            valuesPanel.add(createColumn(POSITION_LABELS[i], valueLabels[i], statusLabels[i]));
        }

        add(valuesPanel, BorderLayout.CENTER);

        historyLabel = new JLabel("等待数据...");
        historyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        historyLabel.setForeground(new Color(100, 100, 100));
        historyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(historyLabel, BorderLayout.SOUTH);
    }

    /**
     * 创建单列面板（标签 + 数值 + 状态）
     */
    private JPanel createColumn(String label, JLabel valueLbl, JLabel statusLbl) {
        JPanel column = new JPanel(new BorderLayout(1, 1));
        column.setOpaque(false);

        JLabel posLabel = new JLabel(label, SwingConstants.CENTER);
        posLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
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
        label.setFont(new Font("微软雅黑", Font.BOLD, 18));
        label.setForeground(new Color(60, 60, 60));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    /**
     * 创建状态标签
     */
    private JLabel createStatusLabel() {
        JLabel label = new JLabel("N");
        label.setFont(new Font("微软雅黑", Font.PLAIN, 9));
        label.setForeground(Color.GRAY);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    /**
     * 根据受力返回颜色
     */
    private Color getLoadColor(double load) {
        if (load >= LOAD_DANGER) {
            return new Color(220, 53, 69);
        } else if (load >= LOAD_WARN) {
            return new Color(255, 152, 0);
        } else {
            return new Color(40, 167, 69);
        }
    }

    /**
     * 根据受力返回状态文字
     */
    private String getLoadStatus(double load) {
        if (load >= LOAD_DANGER) {
            return "⚠过载";
        } else if (load >= LOAD_WARN) {
            return "▲中载";
        } else {
            return "●正常";
        }
    }

    /**
     * 更新指定探头的受力值
     * @param index 探头索引（0~4）
     * @param value 受力值（N）
     */
    public void updateLoad(int index, double value) {
        if (index < 0 || index >= SENSOR_COUNT) return;

        valueLabels[index].setText(String.format("%.0f", value));
        valueLabels[index].setForeground(getLoadColor(value));
        statusLabels[index].setText(getLoadStatus(value));
        statusLabels[index].setForeground(getLoadColor(value));

        List<Double> list = recentValuesList.get(index);
        list.add(value);
        if (list.size() > 10) {
            list.remove(0);
        }

        updateHistory();
    }

    /**
     * 更新底部历史统计信息
     */
    private void updateHistory() {
        List<Double> allValues = new ArrayList<>();
        for (List<Double> list : recentValuesList) {
            allValues.addAll(list);
        }

        if (allValues.isEmpty()) return;

        double min = allValues.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = allValues.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double avg = allValues.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        historyLabel.setText(String.format("最小:%.0fN | 最大:%.0fN | 平均:%.0fN", min, max, avg));
    }

    /**
     * 获取主题颜色
     * @return 主题颜色
     */
    public Color getThemeColor() {
        return themeColor;
    }
}
