package sensor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 普通传感器数据卡片类
 * 用于展示单个传感器的实时数据，包括数值、单位和统计信息
 */
public class SensorDataCard extends JPanel {
    private JLabel nameLabel;        // 传感器名称标签
    private JLabel valueLabel;       // 数值显示标签
    private JLabel unitLabel;        // 单位显示标签
    private JLabel historyLabel;     // 历史统计信息标签
    
    private String sensorName;       // 传感器名称
    private String unit;             // 单位
    protected Color themeColor;      // 主题颜色（改为protected，让子类可以访问）
    private List<Double> recentValues; // 最近的数据值列表
    
    /**
     * 构造函数
     * @param name 传感器名称
     * @param unit 单位
     * @param themeColor 主题颜色
     */
    public SensorDataCard(String name, String unit, Color themeColor) {
        this.sensorName = name;
        this.unit = unit;
        this.themeColor = themeColor;
        this.recentValues = new ArrayList<>();
        
        initUI();
    }
    
    /**
     * 初始化UI组件
     */
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(themeColor, 3),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(300, 180));
        
        // 传感器名称
        nameLabel = new JLabel(sensorName);
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        nameLabel.setForeground(themeColor.darker());
        add(nameLabel, BorderLayout.NORTH);
        
        // 中心数值显示区域
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        
        valueLabel = new JLabel("--");
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 38));
        valueLabel.setForeground(themeColor.darker().darker());
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(valueLabel, BorderLayout.CENTER);
        
        unitLabel = new JLabel(unit);
        unitLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
        unitLabel.setForeground(Color.GRAY);
        unitLabel.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(unitLabel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // 历史统计信息
        historyLabel = new JLabel("等待数据...");
        historyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        historyLabel.setForeground(new Color(100, 100, 100));
        add(historyLabel, BorderLayout.SOUTH);
    }
    
    /**
     * 更新传感器数值
     * @param value 新的传感器数值
     */
    public void updateValue(double value) {
        valueLabel.setText(String.format("%.2f", value));
        
        // 记录历史数据（保留最近10个值）
        recentValues.add(value);
        if (recentValues.size() > 10) {
            recentValues.remove(0);
        }
        
        // 更新统计信息
        updateStatistics();
    }
    
    /**
     * 更新历史统计信息
     */
    private void updateStatistics() {
        if (recentValues.isEmpty()) return;
        
        double min = recentValues.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = recentValues.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double avg = recentValues.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        
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
