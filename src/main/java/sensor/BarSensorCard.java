package sensor;

import javax.swing.*;
import java.awt.*;

/**
 * 带进度条的传感器数据卡片类
 * 继承自SensorDataCard，额外显示一个进度条用于展示百分比或范围值
 */
public class BarSensorCard extends SensorDataCard {
    private JProgressBar progressBar;  // 进度条组件
    private double minValue;           // 最小值
    private double maxValue;           // 最大值
    
    /**
     * 构造函数
     * @param name 传感器名称
     * @param unit 单位
     * @param themeColor 主题颜色
     * @param min 最小值
     * @param max 最大值
     */
    public BarSensorCard(String name, String unit, Color themeColor, double min, double max) {
        super(name, unit, themeColor);
        this.minValue = min;
        this.maxValue = max;
        
        initProgressBar();
    }
    
    /**
     * 初始化进度条
     */
    private void initProgressBar() {
        progressBar = new JProgressBar((int)minValue, (int)maxValue);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("微软雅黑", Font.BOLD, 12));
        progressBar.setForeground(getThemeColor());
        progressBar.setBackground(new Color(240, 240, 240));
        progressBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        progressBar.setPreferredSize(new Dimension(250, 25));
        
        add(progressBar, BorderLayout.SOUTH);
    }
    
    /**
     * 获取主题颜色（从父类获取）
     */
    private Color getThemeColor() {
        return getBackground().darker();
    }
    
    @Override
    public void updateValue(double value) {
        super.updateValue(value);
        
        // 更新进度条
        int progress = (int)((value - minValue) / (maxValue - minValue) * 100);
        progress = Math.max(0, Math.min(100, progress));
        progressBar.setValue(progress);
        progressBar.setString(progress + "%");
    }
}
