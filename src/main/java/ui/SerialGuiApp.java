package ui;

import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import sensor.BarSensorCard;
import sensor.BidirectionalBarSensorCard;
import sensor.DualSensorCard;
import sensor.SensorDataCard;
import sensor.SensorType;
import sensor.TireTempSensorCard;
import serial.ExcelExporter;
import serial.SerialConfig;
import serial.SerialHelper;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
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
    
    // Excel导出器
    private ExcelExporter excelExporter;
    
    // 是否启用实时保存
    private boolean isRealTimeSaveEnabled = false;

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
    
    // 导出Excel按钮
    private JButton exportExcelButton;
    
    // 实时保存复选框
    private JCheckBox realTimeSaveCheckBox;

    // 传感器数据卡片容器
    private JPanel sensorCardsPanel;

    // 传感器数据存储
    private Map<String, SensorDataCard> sensorCardsMap;
    
    // 胎温卡片存储
    private Map<String, TireTempSensorCard> tireTempCardsMap;
    
    // 轮速卡片存储
    private Map<String, DualSensorCard> wheelSpeedCardsMap;

    // 主分割面板
    private JSplitPane mainSplitPane;

    // 工具栏按钮
    private JButton connectButton;

    // 连接状态标识
    private boolean isConnected = false;

    /**
     * 构造函数
     * 初始化串口助手对象并创建GUI界面
     */
    public SerialGuiApp() {
        serialHelper = new SerialHelper();
        excelExporter = new ExcelExporter();
        sensorCardsMap = new HashMap<>();
        tireTempCardsMap = new HashMap<>();
        wheelSpeedCardsMap = new HashMap<>();
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
        
        // 添加窗口关闭监听器，匿名内部类
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (isConnected) {
                    serialHelper.closePort();
                }
                
                if (excelExporter.getRowCount() > 1) {
                    int choice = JOptionPane.showConfirmDialog(
                        SerialGuiApp.this,
                        "检测到有未保存的数据，是否导出到Excel？\n" +
                        "（共 " + (excelExporter.getRowCount() - 1) + " 条记录）",
                        "确认导出",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    );
                    
                    if (choice == JOptionPane.YES_OPTION) {
                        exportToExcelManual();
                    } else if (choice == JOptionPane.CANCEL_OPTION) {
                        return;
                    }
                }
                
                excelExporter.close();
                
                System.exit(0);
            }
        });

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
     * 包含连接按钮等核心控制功能
     * @return 工具栏面板对象
     */
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(new Color(30, 33, 40));
        toolbar.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        // 左侧：连接控制 + 数据操作
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
        
        // 分隔线
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(2, 40));
        separator.setForeground(new Color(80, 80, 80));
        leftPanel.add(separator);
        
        // 实时保存复选框
        realTimeSaveCheckBox = new JCheckBox("实时保存到Excel");
        realTimeSaveCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        realTimeSaveCheckBox.setForeground(Color.WHITE);
        realTimeSaveCheckBox.setOpaque(false);
        realTimeSaveCheckBox.addActionListener(e -> {
            isRealTimeSaveEnabled = realTimeSaveCheckBox.isSelected();
            if (isRealTimeSaveEnabled) {
                System.out.println("✓ 实时保存已启用（数据将自动记录到Excel）");
            } else {
                System.out.println("○ 实时保存已禁用（数据仍会记录，可手动导出）");
            }
        });
        leftPanel.add(realTimeSaveCheckBox);
        
        // 导出Excel按钮
        exportExcelButton = new JButton(" 导出当前数据到Excel");
        exportExcelButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        exportExcelButton.setBackground(new Color(40, 167, 69));
        exportExcelButton.setForeground(Color.BLACK);
        exportExcelButton.setFocusPainted(false);
        exportExcelButton.setBorder(BorderFactory.createLineBorder(new Color(30, 130, 55), 2));
        exportExcelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportExcelButton.setPreferredSize(new Dimension(200, 40));
        exportExcelButton.setEnabled(false);
        exportExcelButton.addActionListener(e -> exportToExcel());
        leftPanel.add(exportExcelButton);

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
        sensorCardsPanel = new JPanel(new GridLayout(0, 4, 15, 15));
        sensorCardsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        sensorCardsPanel.setBackground(new Color(240, 242, 245));

        JScrollPane cardsScrollPane = new JScrollPane(sensorCardsPanel);
        cardsScrollPane.setBorder(null);
        cardsScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel cardsWrapper = new JPanel(new BorderLayout());
        cardsWrapper.setBackground(new Color(240, 242, 245));
        cardsWrapper.add(cardsScrollPane, BorderLayout.CENTER);

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
        Map<SensorType, Color> colorMap = new HashMap<>();
        colorMap.put(SensorType.TS_VOLTAGE, new Color(220, 53, 69));
        colorMap.put(SensorType.TS_CURRENT, new Color(13, 110, 253));
        colorMap.put(SensorType.BRAKE_PRESSURE, new Color(255, 193, 7));
        colorMap.put(SensorType.THROTTLE, new Color(25, 135, 84));
        colorMap.put(SensorType.SPEED, new Color(111, 66, 193));
        colorMap.put(SensorType.WATER_TEMP, new Color(23, 162, 184));
        colorMap.put(SensorType.COOLANT_TEMP, new Color(0, 191, 255));
        
        colorMap.put(SensorType.TIRE_TEMP_FL_LEFT, new Color(255, 87, 34));
        colorMap.put(SensorType.TIRE_TEMP_FL_CENTER, new Color(255, 87, 34));
        colorMap.put(SensorType.TIRE_TEMP_FL_RIGHT, new Color(255, 87, 34));
        colorMap.put(SensorType.TIRE_TEMP_FR_LEFT, new Color(255, 87, 34));
        colorMap.put(SensorType.TIRE_TEMP_FR_CENTER, new Color(255, 87, 34));
        colorMap.put(SensorType.TIRE_TEMP_FR_RIGHT, new Color(255, 87, 34));
        colorMap.put(SensorType.TIRE_TEMP_RL_LEFT, new Color(255, 87, 34));
        colorMap.put(SensorType.TIRE_TEMP_RL_CENTER, new Color(255, 87, 34));
        colorMap.put(SensorType.TIRE_TEMP_RL_RIGHT, new Color(255, 87, 34));
        colorMap.put(SensorType.TIRE_TEMP_RR_LEFT, new Color(255, 87, 34));
        colorMap.put(SensorType.TIRE_TEMP_RR_CENTER, new Color(255, 87, 34));
        colorMap.put(SensorType.TIRE_TEMP_RR_RIGHT, new Color(255, 87, 34));
        
        colorMap.put(SensorType.MIN_CELL_VOLT, new Color(156, 39, 176));
        colorMap.put(SensorType.CELL_TEMP, new Color(233, 30, 99));
        
        colorMap.put(SensorType.MOTOR_TEMP, new Color(0, 150, 136));
        colorMap.put(SensorType.MOTOR_CTRL_TEMP, new Color(0, 188, 212));
        
        colorMap.put(SensorType.FRONT_WHEEL_SPEED_LEFT, new Color(63, 81, 181));
        colorMap.put(SensorType.FRONT_WHEEL_SPEED_RIGHT, new Color(63, 81, 181));
        colorMap.put(SensorType.POWER_CONSUMPTION, new Color(76, 175, 80));
        
        colorMap.put(SensorType.STEERING_ANGLE, new Color(255, 140, 0));
        colorMap.put(SensorType.SUSPENSION_FL, new Color(186, 85, 211));
        colorMap.put(SensorType.SUSPENSION_FR, new Color(186, 85, 211));
        
        // 创建4个胎温卡片（每个轮胎一个，含左/中/右三个探头）
        TireTempSensorCard flCard = new TireTempSensorCard("左前轮", new Color(255, 87, 34));
        sensorCardsPanel.add(flCard);
        tireTempCardsMap.put("tire_temp_fl", flCard);
        
        TireTempSensorCard frCard = new TireTempSensorCard("右前轮", new Color(255, 87, 34));
        sensorCardsPanel.add(frCard);
        tireTempCardsMap.put("tire_temp_fr", frCard);
        
        TireTempSensorCard rlCard = new TireTempSensorCard("左后轮", new Color(232, 120, 50));
        sensorCardsPanel.add(rlCard);
        tireTempCardsMap.put("tire_temp_rl", rlCard);
        
        TireTempSensorCard rrCard = new TireTempSensorCard("右后轮", new Color(232, 120, 50));
        sensorCardsPanel.add(rrCard);
        tireTempCardsMap.put("tire_temp_rr", rrCard);
        
        // 创建前轮轮速卡片（左前/右前并排）
        DualSensorCard wheelSpeedCard = new DualSensorCard("前轮轮速", "左前轮", "右前轮", "rpm", new Color(63, 81, 181));
        sensorCardsPanel.add(wheelSpeedCard);
        wheelSpeedCardsMap.put("front_wheel_speed", wheelSpeedCard);
        
        // 创建其他传感器卡片（跳过胎温和轮速类型，因为已单独处理）
        for (SensorType type : SensorType.values()) {
            if ("胎温".equals(type.getDisplayType()) || "轮速".equals(type.getDisplayType())) {
                continue;
            }
            
            Color color = colorMap.get(type);
            
            if (color == null) {
                color = Color.GRAY;
                System.out.println("警告: 传感器 " + type.getDisplayName() + " 未定义颜色，使用默认灰色");
            }
            
            if ("进度条".equals(type.getDisplayType())) {
                BarSensorCard card = new BarSensorCard(
                    type.getDisplayName(), 
                    type.getUnit(), 
                    color,
                    0, 100
                );
                sensorCardsPanel.add(card);
                sensorCardsMap.put(type.getKey(), card);
            } else if ("双向进度条".equals(type.getDisplayType())) {
                double min = 0, max = 100;
                
                if (type == SensorType.STEERING_ANGLE) {
                    min = -90;
                    max = 90;
                } else if (type == SensorType.SUSPENSION_FL || type == SensorType.SUSPENSION_FR) {
                    min = -50;
                    max = 50;
                }
                
                BidirectionalBarSensorCard card = new BidirectionalBarSensorCard(
                    type.getDisplayName(), 
                    type.getUnit(), 
                    color,
                    min, max
                );
                sensorCardsPanel.add(card);
                sensorCardsMap.put(type.getKey(), card);
            } else {
                SensorDataCard card = new SensorDataCard(
                    type.getDisplayName(), 
                    type.getUnit(), 
                    color
                );
                sensorCardsPanel.add(card);
                sensorCardsMap.put(type.getKey(), card);
            }
        }
        
        System.out.println("已创建 " + (sensorCardsMap.size() + tireTempCardsMap.size() + wheelSpeedCardsMap.size()) + " 个传感器卡片");
    }

    /**
     * 初始化Excel导出
     * 创建工作表并设置表头
     */
    private void initializeExcelExport() {
        if (excelExporter.getRowCount() == 0) {
            String[] headers = {
                "时间戳", "毫秒时间", 
                "TS电压(V)", "TS电流(A)", 
                "刹车压力(bar)", "油门位置(%)", 
                "车速(km/h)", "水箱温度1(°C)", "水箱温度2(°C)",
                "左前胎温(°C)", "右前胎温(°C)", "左后胎温(°C)", "右后胎温(°C)",
                "最低电芯电压(V)", "电芯温度(°C)",
                "电机温度(°C)", "电机控制器温度(°C)",
                "左前轮速(rpm)", "右前轮速(rpm)", "用电量(Wh)",
                "方向盘角度(°)", "左前悬架(mm)", "右前悬架(mm)"
            };
            
            excelExporter.createSheet("传感器数据", headers);
            
            System.out.println("✓ Excel导出已初始化，文件: " + excelExporter.getOutputFileName());
        }
    }
    
    /**
     * 手动导出Excel（窗口关闭时使用，不重置）
     */
    private void exportToExcelManual() {
        if (excelExporter.getRowCount() == 0) {
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择保存位置");
        fileChooser.setSelectedFile(new java.io.File(excelExporter.getOutputFileName()));
        
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String fileName = fileChooser.getSelectedFile().getAbsolutePath();
            if (!fileName.endsWith(".xlsx")) {
                fileName += ".xlsx";
            }
            
            if (excelExporter.saveToFile(fileName)) {
                JOptionPane.showMessageDialog(this,
                    "数据已成功导出到:\n" + fileName,
                    "导出成功",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "导出失败，请检查文件路径和权限",
                    "导出错误",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * 导出数据到Excel文件
     */
    private void exportToExcel() {
        // 检查是否有数据（表头算1行，所以至少要有2行才有数据）
        if (excelExporter.getRowCount() <= 1) {
            JOptionPane.showMessageDialog(this,
                "暂无传感器数据可导出，请先接收传感器数据",
                "无数据",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择保存位置");
        fileChooser.setSelectedFile(new java.io.File(excelExporter.getOutputFileName()));
        
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String fileName = fileChooser.getSelectedFile().getAbsolutePath();
            if (!fileName.endsWith(".xlsx")) {
                fileName += ".xlsx";
            }
            
            excelExporter.setOutputFileName(fileName);
            
            if (excelExporter.saveToFile(fileName)) {
                JOptionPane.showMessageDialog(this,
                    "数据已成功导出到:\n" + fileName + "\n" +
                    "共导出 " + (excelExporter.getRowCount() - 1) + " 条记录",
                    "导出成功",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // 重置导出器，准备新的会话
                excelExporter.reset();
                initializeExcelExport();
            } else {
                JOptionPane.showMessageDialog(this,
                    "导出失败，请检查文件路径和权限",
                    "导出错误",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * 保存单条传感器数据到Excel
     * @param timestamp 时间戳字符串
     * @param millis 毫秒时间
     * @param values 传感器数值数组
     */
    private void saveSensorDataToExcel(String timestamp, long millis, double[] values) {
        if (excelExporter.getRowCount() == 0) {
            return;
        }
        
        Object[] rowData = new Object[23];
        rowData[0] = timestamp;
        rowData[1] = millis;
        
        for (int i = 0; i < Math.min(values.length, 21); i++) {
            rowData[i + 2] = values[i];
        }
        
        excelExporter.addDataRow(rowData);
    }

    /**
     * 显示连接对话框
     */
    private void showConnectDialog() {
        if (isConnected) {
            disconnectPort();
            return;
        }

        SerialConfigDialog dialog = new SerialConfigDialog(this, serialHelper);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            isConnected = true;
            connectButton.setText(" 断开连接");
            connectButton.setBackground(new Color(220, 53, 69));

            SerialConfig config = serialHelper.getCurrentConfig();
            statusLabel.setText("✓ 已连接: " + config.getPortName() +
                              " [波特率:" + config.getBaudRate() + "]");
            statusLabel.setForeground(new Color(25, 135, 84));

            setupDataListener();
            
            exportExcelButton.setEnabled(true);
            
            // 重置并重新初始化Excel导出器
            excelExporter.reset();
            initializeExcelExport();
            
            isRealTimeSaveEnabled = false;
            realTimeSaveCheckBox.setSelected(false);
            
            System.out.println("✓ 新会话已开始，Excel导出器已初始化");
            System.out.println("提示：所有传感器数据将自动记录，点击'导出当前数据到Excel'即可导出");
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
            String cleanedData = data.replaceAll("[\\r\\n]+", "").trim();
            if (cleanedData.endsWith(",")) {
                cleanedData = cleanedData.substring(0, cleanedData.length() - 1);
            }

            if (cleanedData.isEmpty()) return;

            String[] pairs = cleanedData.split(",");
            
            // 用于存储解析出的传感器数据
            Map<String, Double> sensorValues = new HashMap<>();

            for (String pair : pairs) {
                pair = pair.trim();
                if (pair.isEmpty()) continue;

                String[] keyValue = pair.split(":");

                if (keyValue.length == 2) {
                    String sensorKey = keyValue[0].trim();
                    String valueStr = keyValue[1].trim();
                    valueStr = valueStr.replaceAll("[^0-9.\\-]", "");

                    if (!valueStr.isEmpty()) {
                        try {
                            double value = Double.parseDouble(valueStr);
                            
                            SensorType type = SensorType.fromKey(sensorKey);
                            
                            if (type != null && "胎温".equals(type.getDisplayType())) {
                                updateTireTempCard(sensorKey, value);
                                sensorValues.put(sensorKey, value);
                            } else if (type != null && "轮速".equals(type.getDisplayType())) {
                                updateWheelSpeedCard(sensorKey, value);
                                sensorValues.put(sensorKey, value);
                            } else if (type != null) {
                                if (sensorCardsMap.containsKey(sensorKey)) {
                                    sensorCardsMap.get(sensorKey).updateValue(value);
                                    sensorValues.put(sensorKey, value);
                                }
                            }
                            
                        } catch (NumberFormatException e) {
                            System.out.println("数值格式错误: " + valueStr);
                        }
                    }
                }
            }
            
            // 如果有传感器数据，自动保存到Excel（无论是否勾选实时保存）
            if (!sensorValues.isEmpty() && excelExporter.getRowCount() > 0) {
                String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
                long millis = System.currentTimeMillis();
                
                double[] values = new double[21];
                values[0] = sensorValues.getOrDefault("ts_voltage", 0.0);
                values[1] = sensorValues.getOrDefault("ts_current", 0.0);
                values[2] = sensorValues.getOrDefault("brake_pressure", 0.0);
                values[3] = sensorValues.getOrDefault("throttle", 0.0);
                values[4] = sensorValues.getOrDefault("speed", 0.0);
                values[5] = sensorValues.getOrDefault("water_temp", 0.0);
                values[6] = sensorValues.getOrDefault("coolant_temp", 0.0);
                values[7] = sensorValues.getOrDefault("tire_temp_fl_center", 0.0);
                values[8] = sensorValues.getOrDefault("tire_temp_fr_center", 0.0);
                values[9] = sensorValues.getOrDefault("tire_temp_rl_center", 0.0);
                values[10] = sensorValues.getOrDefault("tire_temp_rr_center", 0.0);
                values[11] = sensorValues.getOrDefault("min_cell_volt", 0.0);
                values[12] = sensorValues.getOrDefault("cell_temp", 0.0);
                values[13] = sensorValues.getOrDefault("motor_temp", 0.0);
                values[14] = sensorValues.getOrDefault("motor_ctrl_temp", 0.0);
                values[15] = sensorValues.getOrDefault("front_wheel_speed_left", 0.0);
                values[16] = sensorValues.getOrDefault("front_wheel_speed_right", 0.0);
                values[17] = sensorValues.getOrDefault("power_consumption", 0.0);
                values[18] = sensorValues.getOrDefault("steering_angle", 0.0);
                values[19] = sensorValues.getOrDefault("suspension_fl", 0.0);
                values[20] = sensorValues.getOrDefault("suspension_fr", 0.0);
                
                saveSensorDataToExcel(timeStamp, millis, values);
            }
            
        } catch (Exception e) {
            System.err.println("数据解析错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 更新胎温卡片数据
     * 根据键名判断属于哪个轮胎的哪个探头位置
     * @param sensorKey 传感器键名
     * @param value 温度值
     */
    private void updateTireTempCard(String sensorKey, double value) {
        String tirePrefix;
        String position;
        
        if (sensorKey.startsWith("tire_temp_fl_")) {
            tirePrefix = "tire_temp_fl";
            position = sensorKey.substring("tire_temp_fl_".length());
        } else if (sensorKey.startsWith("tire_temp_fr_")) {
            tirePrefix = "tire_temp_fr";
            position = sensorKey.substring("tire_temp_fr_".length());
        } else if (sensorKey.startsWith("tire_temp_rl_")) {
            tirePrefix = "tire_temp_rl";
            position = sensorKey.substring("tire_temp_rl_".length());
        } else if (sensorKey.startsWith("tire_temp_rr_")) {
            tirePrefix = "tire_temp_rr";
            position = sensorKey.substring("tire_temp_rr_".length());
        } else {
            return;
        }
        
        TireTempSensorCard card = tireTempCardsMap.get(tirePrefix);
        if (card == null) return;
        
        switch (position) {
            case "left":
                card.updateLeftTemp(value);
                break;
            case "center":
                card.updateCenterTemp(value);
                break;
            case "right":
                card.updateRightTemp(value);
                break;
        }
    }
    
    /**
     * 更新轮速卡片数据
     * 根据键名判断属于左前还是右前轮速
     * @param sensorKey 传感器键名
     * @param value 轮速值
     */
    private void updateWheelSpeedCard(String sensorKey, double value) {
        DualSensorCard card = wheelSpeedCardsMap.get("front_wheel_speed");
        if (card == null) return;
        
        if ("front_wheel_speed_left".equals(sensorKey)) {
            card.updateLeftValue(value);
        } else if ("front_wheel_speed_right".equals(sensorKey)) {
            card.updateRightValue(value);
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
        
        statusLabel.setText("未连接 - 点击\"连接串口\"开始");
        statusLabel.setForeground(Color.BLACK);
        
        exportExcelButton.setEnabled(false);
        
        if (excelExporter.getRowCount() > 1) {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "检测到有测试数据，是否导出到Excel？\n" +
                "（共 " + (excelExporter.getRowCount() - 1) + " 条记录）",
                "确认导出",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (choice == JOptionPane.YES_OPTION) {
                exportToExcelManual();
            }
        }
        
        isRealTimeSaveEnabled = false;
        realTimeSaveCheckBox.setSelected(false);
        excelExporter.reset();
    }

}
