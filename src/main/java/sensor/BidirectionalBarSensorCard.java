package sensor;

import javax.swing.*;
import java.awt.*;

/**
 * 带双向进度条的传感器数据卡片类
 * 支持显示正负值范围（如方向盘角度 -90° ~ +90°）
 * 继承自SensorDataCard，额外显示一个双向进度条
 */
public class BidirectionalBarSensorCard extends SensorDataCard {
    private JProgressBar progressBar;
    private double minValue;
    private double maxValue;
    
    /**
     * 构造函数
     * @param name 传感器名称
     * @param unit 单位
     * @param themeColor 主题颜色
     * @param min 最小值（负值）
     * @param max 最大值（正值）
     */
    public BidirectionalBarSensorCard(String name, String unit, Color themeColor, double min, double max) {
        super(name, unit, themeColor);
        this.minValue = min;
        this.maxValue = max;
        
        initBidirectionalProgressBar();
    }
    
    /**
     * 初始化双向进度条
     */
    private void initBidirectionalProgressBar() {
        progressBar = new JProgressBar((int)minValue, (int)maxValue);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("微软雅黑", Font.BOLD, 11));
        progressBar.setForeground(themeColor);
        progressBar.setBackground(new Color(240, 240, 240));
        progressBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        progressBar.setPreferredSize(new Dimension(250, 25));
        
        add(progressBar, BorderLayout.SOUTH);
    }
    
    @Override
    public void updateValue(double value) {
        super.updateValue(value);
        
        progressBar.setValue((int)value);
        progressBar.setString(String.format("%.1f", value));
        
        if (value > 0) {
            progressBar.setForeground(themeColor.brighter());
        } else if (value < 0) {
            progressBar.setForeground(themeColor.darker());
        } else {
            progressBar.setForeground(Color.GRAY);
        }
    }
}

