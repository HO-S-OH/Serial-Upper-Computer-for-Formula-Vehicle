package ui;

import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import sensor.BarSensorCard;
import sensor.SensorDataCard;
import serial.SerialConfig;
import serial.SerialHelper;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 传感器数据监控面板
 * 采用类似音乐播放器的现代化UI设计，提供高密度的传感器数据可视化展示
 * 支持多传感器数据卡片、实时波形图、数据统计等功能
 */
public class SerialGuiApp extends JFrame {
    // 串口业务逻辑助手对象
    private SerialHelper serialHelper;
    
    // ========== 界面组件声明 ==========
    
    // 数据显示区域（接收区）
    private JTextArea receiveTextArea;
    
    // 状态标签（显示连接状态）
    private JLabel statusLabel;
    
    // 自动滚动复选框
    private JCheckBox autoScrollCheckBox;
    
    // 显示时间戳复选框
    private JCheckBox showTimeStampCheckBox;
    
    // 清空接收区按钮
    private JButton clearReceiveButton;
    
    // 传感器数据卡片容器
    private JPanel sensorCardsPanel;
    
    // 传感器数据存储
    private Map<String, SensorDataCard> sensorCardsMap;
    
    // 主分割面板
    private JSplitPane mainSplitPane;
    
    // 工具栏按钮
    private JButton settingsButton;
    private JButton connectButton;
    
    // 连接状态标识
    private boolean isConnected = false;
    
    /**
     * 构造函数
     * 初始化串口助手对象并创建GUI界面
     */
    public SerialGuiApp() {
        serialHelper = new SerialHelper();
        sensorCardsMap = new HashMap<>();
        initUI();
    }
    
    /**
     * 初始化用户界面
     * 创建窗口主框架、设置布局、添加各个面板组件
     */
    private void initUI() {
        // 窗口基本设置
        setTitle("车辆传感器数据监控面板");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 创建主面板，使用BorderLayout布局
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        
        // 顶部工具栏
        JPanel toolbar = createToolbar();
        mainPanel.add(toolbar, BorderLayout.NORTH);
        
        // 中间内容区域（传感器卡片 + 原始数据）
        mainSplitPane = createContentPanel();
        mainPanel.add(mainSplitPane, BorderLayout.CENTER);
        
        // 底部状态栏
        statusLabel = new JLabel("未连接 - 点击\"连接串口\"开始");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    /**
     * 创建顶部工具栏
     * 包含连接按钮、设置按钮等核心控制功能
     * @return 工具栏面板对象
     */
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(new Color(30, 33, 40));
        toolbar.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        
        // 左侧：连接控制
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);
        
        connectButton = new JButton(" 连接串口");
        connectButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        connectButton.setForeground(Color.BLACK);
        connectButton.setBackground(new Color(0, 100, 150));
        connectButton.setFocusPainted(false);
        connectButton.setBorder(BorderFactory.createLineBorder(new Color(0, 80, 130), 2));
        connectButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        connectButton.setPreferredSize(new Dimension(160, 48));
        connectButton.addActionListener(e -> showConnectDialog());
        leftPanel.add(connectButton);
        
        settingsButton = new JButton(" 串口设置");
        settingsButton.setFont(new Font("微软雅黑", Font.BOLD, 15));
        settingsButton.setForeground(Color.BLACK);
        settingsButton.setBackground(new Color(60, 65, 72));
        settingsButton.setFocusPainted(false);
        settingsButton.setBorder(BorderFactory.createLineBorder(new Color(50, 55, 62), 2));
        settingsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        settingsButton.setPreferredSize(new Dimension(140, 48));
        settingsButton.setEnabled(false);
        settingsButton.addActionListener(e -> showSettingsDialog());
        leftPanel.add(settingsButton);
        
        toolbar.add(leftPanel, BorderLayout.WEST);
        
        // 右侧：标题和统计信息
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(" 车辆传感器实时监控");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        rightPanel.add(titleLabel);
        
        toolbar.add(rightPanel, BorderLayout.EAST);
        
        return toolbar;
    }
    
    /**
     * 创建内容面板
     * 包含上方的传感器卡片区域和下方的原始数据区域
     * @return 分割面板对象
     */
    private JSplitPane createContentPanel() {
        // ========== 上方：传感器卡片区域 ==========
        sensorCardsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        sensorCardsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        sensorCardsPanel.setBackground(new Color(240, 242, 245));
        
        JScrollPane cardsScrollPane = new JScrollPane(sensorCardsPanel);
        cardsScrollPane.setBorder(null);
        cardsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        JPanel cardsWrapper = new JPanel(new BorderLayout());
        cardsWrapper.setBackground(new Color(240, 242, 245));
        cardsWrapper.add(cardsScrollPane, BorderLayout.CENTER);
        
        // 添加传感器卡片
        addSensorCards();
        
        // ========== 下方：原始数据区域 ==========
        JPanel rawDataPanel = new JPanel(new BorderLayout());
        rawDataPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 2), 
                "原始数据流",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("微软雅黑", Font.BOLD, 14),
                new Color(60, 60, 60)));
        
        receiveTextArea = new JTextArea();
        receiveTextArea.setEditable(false);
        receiveTextArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        receiveTextArea.setBackground(new Color(25, 25, 25));
        receiveTextArea.setForeground(new Color(0, 255, 0));
        JScrollPane receiveScrollPane = new JScrollPane(receiveTextArea);
        rawDataPanel.add(receiveScrollPane, BorderLayout.CENTER);
        
        // 原始数据控制按钮
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controlPanel.setBackground(new Color(245, 245, 245));
        
        autoScrollCheckBox = new JCheckBox("自动滚动");
        autoScrollCheckBox.setSelected(true);
        autoScrollCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        autoScrollCheckBox.setForeground(Color.BLACK);
        controlPanel.add(autoScrollCheckBox);
        
        showTimeStampCheckBox = new JCheckBox("显示时间戳");
        showTimeStampCheckBox.setSelected(true);
        showTimeStampCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        showTimeStampCheckBox.setForeground(Color.BLACK);
        controlPanel.add(showTimeStampCheckBox);
        
        clearReceiveButton = new JButton("清空数据");
        clearReceiveButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        clearReceiveButton.setBackground(new Color(230, 230, 230));
        clearReceiveButton.setForeground(Color.BLACK);
        clearReceiveButton.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 2));
        clearReceiveButton.addActionListener(e -> receiveTextArea.setText(""));
        controlPanel.add(clearReceiveButton);
        
        rawDataPanel.add(controlPanel, BorderLayout.SOUTH);
        
        // ========== 创建垂直分割面板 ==========
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cardsWrapper, rawDataPanel);
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.7);
        
        return splitPane;
    }
    
    /**
     * 添加传感器卡片
     */
    private void addSensorCards() {
        // TS电压卡片 - 普通显示
        SensorDataCard tsVoltageCard = new SensorDataCard("TS电压", "V", new Color(220, 53, 69), false);
        sensorCardsPanel.add(tsVoltageCard);
        sensorCardsMap.put("ts_voltage", tsVoltageCard);
        
        // TS电流卡片 - 普通显示
        SensorDataCard tsCurrentCard = new SensorDataCard("TS电流", "A", new Color(13, 110, 253), false);
        sensorCardsPanel.add(tsCurrentCard);
        sensorCardsMap.put("ts_current", tsCurrentCard);
        
        // 刹车压力卡片 - 普通显示（无数值，只有数值）
        SensorDataCard brakePressureCard = new SensorDataCard("刹车压力", "Bar", new Color(255, 193, 7), false);
        sensorCardsPanel.add(brakePressureCard);
        sensorCardsMap.put("brake_pressure", brakePressureCard);
        
        // 电门开度卡片 - 带进度条显示
        BarSensorCard throttleCard = new BarSensorCard("电门开度", "%", new Color(25, 135, 84), 0, 100);
        sensorCardsPanel.add(throttleCard);
        sensorCardsMap.put("throttle", throttleCard);
        
        // 车速卡片 - 普通显示
        SensorDataCard speedCard = new SensorDataCard("车速", "km/h", new Color(111, 66, 193), false);
        sensorCardsPanel.add(speedCard);
        sensorCardsMap.put("speed", speedCard);
        
        // 水温卡片 - 普通显示
        SensorDataCard tempCard = new SensorDataCard("水温", "°C", new Color(23, 162, 184), false);
        sensorCardsPanel.add(tempCard);
        sensorCardsMap.put("water_temp", tempCard);
    }
    
    /**
     * 显示连接对话框
     * 首次连接时弹出串口选择和配置对话框
     */
    private void showConnectDialog() {
        if (isConnected) {
            disconnectPort();
            return;
        }
        
        SerialConfigDialog dialog = new SerialConfigDialog(this, serialHelper);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            // 连接成功
            isConnected = true;
            connectButton.setText(" 断开连接");
            connectButton.setBackground(new Color(220, 53, 69));
            settingsButton.setEnabled(true);
            
            SerialConfig config = serialHelper.getCurrentConfig();
            statusLabel.setText("✓ 已连接: " + config.getPortName() + 
                              " [波特率:" + config.getBaudRate() + "]");
            statusLabel.setForeground(new Color(25, 135, 84));
            
            // 设置数据监听器
            setupDataListener();
        }
    }
    
    /**
     * 显示设置对话框
     * 用于修改串口配置参数
     */
    private void showSettingsDialog() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(this, "请先连接串口！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        SerialConfigDialog dialog = new SerialConfigDialog(this, serialHelper);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            // 更新状态显示
            SerialConfig config = serialHelper.getCurrentConfig();
            statusLabel.setText("✓ 已连接: " + config.getPortName() + 
                              " [波特率:" + config.getBaudRate() + "]");
        }
    }
    
    /**
     * 设置数据监听器
     * 监听串口接收到的传感器数据并更新卡片显示
     */
    private void setupDataListener() {
        serialHelper.setDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                byte[] buffer = new byte[serialHelper.getBytesAvailable()];
                serialHelper.readBytes(buffer, buffer.length);
                String data = new String(buffer);
                
                // 调试输出：显示原始接收到的数据
                System.out.println("接收到原始数据: [" + data + "]");
                System.out.println("数据长度: " + data.length());
                
                SwingUtilities.invokeLater(() -> {
                    // 解析并更新传感器数据
                    parseAndUpdateSensorData(data);
                    
                    // 显示原始数据
                    if (showTimeStampCheckBox.isSelected()) {
                        String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
                        receiveTextArea.append("[" + timeStamp + "] ");
                    }
                    receiveTextArea.append(data);
                    
                    if (autoScrollCheckBox.isSelected()) {
                        receiveTextArea.setCaretPosition(receiveTextArea.getDocument().getLength());
                    }
                });
            }
        });
    }
    
    /**
     * 解析并更新传感器数据
     * 根据实际数据格式解析传感器数值并更新对应卡片
     * @param data 接收到的原始数据
     */
    private void parseAndUpdateSensorData(String data) {
        try {
            // 清理数据：移除所有换行符、回车符、多余逗号
            String cleanedData = data.replaceAll("[\\r\\n]+", "").trim();
            // 移除末尾的逗号
            if (cleanedData.endsWith(",")) {
                cleanedData = cleanedData.substring(0, cleanedData.length() - 1);
            }
            
            if (cleanedData.isEmpty()) return;
            
            System.out.println("清理后的数据: " + cleanedData);
            
            // 按逗号分割
            String[] pairs = cleanedData.split(",");
            System.out.println("分割成 " + pairs.length + " 个键值对");
            
            for (int i = 0; i < pairs.length; i++) {
                String pair = pairs[i].trim();
                System.out.println("处理第 " + (i+1) + " 个: [" + pair + "]");
                
                if (pair.isEmpty()) {
                    System.out.println("  跳过空项");
                    continue;
                }
                
                // 按冒号分割
                String[] keyValue = pair.split(":");
                System.out.println("  分割结果: " + keyValue.length + " 部分");
                
                if (keyValue.length == 2) {
                    String sensorType = keyValue[0].trim();
                    String valueStr = keyValue[1].trim();
                    
                    System.out.println("  传感器类型: [" + sensorType + "]");
                    System.out.println("  原始数值: [" + valueStr + "]");
                    
                    // 清理数值字符串：只保留数字、小数点、负号
                    valueStr = valueStr.replaceAll("[^0-9.\\-]", "");
                    System.out.println("  清理后数值: [" + valueStr + "]");
                    
                    if (!valueStr.isEmpty()) {
                        try {
                            double value = Double.parseDouble(valueStr);
                            System.out.println("  解析数值: " + value);
                            
                            // 更新对应的传感器卡片
                            if (sensorCardsMap.containsKey(sensorType)) {
                                sensorCardsMap.get(sensorType).updateValue(value);
                                System.out.println("  ✓ 更新成功: " + sensorType);
                            } else {
                                System.out.println("  ✗ 未找到传感器: " + sensorType);
                                System.out.println("  可用传感器: " + sensorCardsMap.keySet());
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("   数值格式错误: " + valueStr + ", 错误: " + e.getMessage());
                        }
                    } else {
                        System.out.println("  ✗ 数值为空");
                    }
                } else {
                    System.out.println("  ✗ 键值对格式错误，应该是 key:value");
                }
            }
        } catch (Exception e) {
            System.err.println("数据解析错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 断开串口连接
     */
    private void disconnectPort() {
        serialHelper.closePort();
        isConnected = false;
        
        connectButton.setText(" 连接串口");
        connectButton.setBackground(new Color(0, 100, 150));
        settingsButton.setEnabled(false);
        
        statusLabel.setText("未连接 - 点击\"连接串口\"开始");
        statusLabel.setForeground(Color.BLACK);
    }
    
    /**
     * 传感器数据卡片类
     * 用于展示单个传感器的实时数据
     */
    class SensorDataCard extends JPanel {
        private JLabel nameLabel;
        private JLabel valueLabel;
        private JLabel unitLabel;
        private JLabel historyLabel;
        private String sensorName;
        private String unit;
        private Color themeColor;
        private List<Double> recentValues;
        
        public SensorDataCard(String name, String unit, Color themeColor, boolean withBar) {
            this.sensorName = name;
            this.unit = unit;
            this.themeColor = themeColor;
            this.recentValues = new ArrayList<>();
            
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(themeColor, 3),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(300, 180));
            
            // 传感器名称
            nameLabel = new JLabel(name);
            nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            nameLabel.setForeground(themeColor.darker());
            add(nameLabel, BorderLayout.NORTH);
            
            // 中心数值显示
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
            
            // 记录历史数据
            recentValues.add(value);
            if (recentValues.size() > 10) {
                recentValues.remove(0);
            }
            
            // 更新统计信息
            updateStatistics();
        }
        
        /**
         * 更新统计信息
         */
        private void updateStatistics() {
            if (recentValues.isEmpty()) return;
            
            double min = recentValues.stream().mapToDouble(Double::doubleValue).min().orElse(0);
            double max = recentValues.stream().mapToDouble(Double::doubleValue).max().orElse(0);
            double avg = recentValues.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            
            historyLabel.setText(String.format("最小:%.1f | 最大:%.1f | 平均:%.1f", min, max, avg));
        }
    }
    
    /**
     * 带条形显示的传感器数据卡片类
     * 用于展示百分比或范围值的传感器数据
     */
    class BarSensorCard extends SensorDataCard {
        private JProgressBar progressBar;
        private double minValue;
        private double maxValue;
        
        public BarSensorCard(String name, String unit, Color themeColor, double min, double max) {
            super(name, unit, themeColor, true);
            this.minValue = min;
            this.maxValue = max;
            
            // 创建进度条
            progressBar = new JProgressBar((int)minValue, (int)maxValue);
            progressBar.setStringPainted(true);
            progressBar.setFont(new Font("微软雅黑", Font.BOLD, 12));
            progressBar.setForeground(themeColor);
            progressBar.setBackground(new Color(240, 240, 240));
            progressBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
            progressBar.setPreferredSize(new Dimension(250, 25));
            
            add(progressBar, BorderLayout.SOUTH);
        }
        
        @Override
        public void updateValue(double value) {
            super.updateValue(value);
            
            // 更新进度条
            int progress = (int)((value - minValue) / (maxValue - minValue) * 100);
            progress = Math.max(0, Math.min(100, progress));
            progressBar.setValue(progress);
            progressBar.setString(String.valueOf(progress) + "%");
        }
    }
    
    /**
     * 串口配置对话框
     * 用于选择和配置串口参数
     */
    class SerialConfigDialog extends JDialog {
        private JComboBox<String> portComboBox;
        private JComboBox<Integer> baudRateComboBox;
        private JComboBox<Integer> dataBitsComboBox;
        private JComboBox<Integer> stopBitsComboBox;
        private JComboBox<String> parityComboBox;
        private JButton refreshButton;
        private JButton confirmButton;
        private JButton cancelButton;
        private boolean confirmed = false;
        
        public SerialConfigDialog(Frame parent, SerialHelper helper) {
            super(parent, "串口配置", true);
            setSize(600, 480);
            setLocationRelativeTo(parent);
            setResizable(false);
            
            initDialog(helper);
        }
        
        private void initDialog(SerialHelper helper) {
            JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
            mainPanel.setBackground(new Color(245, 245, 245));
            
            // 标题
            JLabel titleLabel = new JLabel("请选择串口并配置参数");
            titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);
            
            // 配置表单
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(new Color(245, 245, 245));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(12, 15, 12, 15);
            gbc.anchor = GridBagConstraints.WEST;
            
            // 串口选择
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            JLabel portLabel = new JLabel("串口:");
            portLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            formPanel.add(portLabel, gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            portComboBox = new JComboBox<>();
            portComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            portComboBox.setPreferredSize(new Dimension(320, 36));
            formPanel.add(portComboBox, gbc);
            
            gbc.gridx = 2;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            refreshButton = new JButton("刷新");
            refreshButton.setFont(new Font("微软雅黑", Font.BOLD, 13));
            refreshButton.setPreferredSize(new Dimension(80, 36));
            refreshButton.setBackground(new Color(230, 230, 230));
            refreshButton.setForeground(Color.BLACK);
            refreshButton.setFocusPainted(false);
            refreshButton.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 2));
            refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            refreshButton.addActionListener(e -> refreshPorts());
            formPanel.add(refreshButton, gbc);
            
            // 波特率
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            JLabel baudLabel = new JLabel("波特率:");
            baudLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            formPanel.add(baudLabel, gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            baudRateComboBox = new JComboBox<>(new Integer[]{
                300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 38400, 
                57600, 115200, 230400, 460800, 921600
            });
            baudRateComboBox.setSelectedItem(9600);
            baudRateComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            baudRateComboBox.setPreferredSize(new Dimension(320, 36));
            formPanel.add(baudRateComboBox, gbc);
            
            // 数据位
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            JLabel dataLabel = new JLabel("数据位:");
            dataLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            formPanel.add(dataLabel, gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            dataBitsComboBox = new JComboBox<>(new Integer[]{5, 6, 7, 8});
            dataBitsComboBox.setSelectedItem(8);
            dataBitsComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            dataBitsComboBox.setPreferredSize(new Dimension(320, 36));
            formPanel.add(dataBitsComboBox, gbc);
            
            // 停止位
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            JLabel stopLabel = new JLabel("停止位:");
            stopLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            formPanel.add(stopLabel, gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            stopBitsComboBox = new JComboBox<>(new Integer[]{1, 2});
            stopBitsComboBox.setSelectedItem(1);
            stopBitsComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            stopBitsComboBox.setPreferredSize(new Dimension(320, 36));
            formPanel.add(stopBitsComboBox, gbc);
            
            // 校验位
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            JLabel parityLabel = new JLabel("校验:");
            parityLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            formPanel.add(parityLabel, gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            parityComboBox = new JComboBox<>(new String[]{"无", "奇校验", "偶校验"});
            parityComboBox.setSelectedIndex(0);
            parityComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            parityComboBox.setPreferredSize(new Dimension(320, 36));
            formPanel.add(parityComboBox, gbc);
            
            mainPanel.add(formPanel, BorderLayout.CENTER);
            
            // 按钮区域
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 20));
            buttonPanel.setBackground(new Color(245, 245, 245));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
            
            confirmButton = new JButton("确认连接");
            confirmButton.setPreferredSize(new Dimension(140, 45));
            confirmButton.setFont(new Font("微软雅黑", Font.BOLD, 15));
            confirmButton.setBackground(new Color(200, 240, 220));
            confirmButton.setForeground(Color.BLACK);
            confirmButton.setFocusPainted(false);
            confirmButton.setBorder(BorderFactory.createLineBorder(new Color(80, 160, 120), 2));
            confirmButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            confirmButton.addActionListener(e -> {
                if (connectPort()) {
                    confirmed = true;
                    dispose();
                }
            });
            buttonPanel.add(confirmButton);
            
            cancelButton = new JButton("取消");
            cancelButton.setPreferredSize(new Dimension(140, 45));
            cancelButton.setFont(new Font("微软雅黑", Font.BOLD, 15));
            cancelButton.setBackground(Color.WHITE);
            cancelButton.setForeground(new Color(80, 80, 80));
            cancelButton.setFocusPainted(false);
            cancelButton.setBorder(BorderFactory.createLineBorder(new Color(160, 160, 160), 2));
            cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            cancelButton.addActionListener(e -> {
                confirmed = false;
                dispose();
            });
            buttonPanel.add(cancelButton);
            
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            add(mainPanel);
            
            // 初始刷新串口列表
            refreshPorts();
        }
        
        /**
         * 刷新可用串口列表
         */
        private void refreshPorts() {
            portComboBox.removeAllItems();
            String[] ports = serialHelper.listAvailablePorts();
            
            if (ports.length == 0) {
                portComboBox.addItem("无可用串口");
                confirmButton.setEnabled(false);
            } else {
                for (String port : ports) {
                    portComboBox.addItem(port);
                }
                confirmButton.setEnabled(true);
            }
        }
        
        /**
         * 连接串口
         * @return true表示连接成功，false表示失败
         */
        private boolean connectPort() {
            int selectedIndex = portComboBox.getSelectedIndex();
            
            if (selectedIndex < 0 || portComboBox.getSelectedItem().toString().equals("无可用串口")) {
                JOptionPane.showMessageDialog(this, "请选择有效的串口！", "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            SerialConfig config = new SerialConfig();
            config.setBaudRate((Integer) baudRateComboBox.getSelectedItem());
            config.setDataBits((Integer) dataBitsComboBox.getSelectedItem());
            config.setStopBits((Integer) stopBitsComboBox.getSelectedItem());
            config.setParity(parityComboBox.getSelectedIndex());
            
            if (serialHelper.openPort(selectedIndex, config)) {
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "无法打开串口，可能已被占用！", "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        /**
         * 检查是否确认连接
         * @return true表示用户点击了确认按钮
         */
        public boolean isConfirmed() {
            return confirmed;
        }
    }

}
