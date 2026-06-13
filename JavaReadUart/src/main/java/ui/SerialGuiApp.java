package ui;

import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import sensor.BarSensorCard;
import sensor.SensorDataCard;
import sensor.SensorType;
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
     * 包含连接按钮等核心控制功能
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
        // 定义传感器颜色映射
        Map<SensorType, Color> colorMap = new HashMap<>();
        colorMap.put(SensorType.TS_VOLTAGE, new Color(220, 53, 69));
        colorMap.put(SensorType.TS_CURRENT, new Color(13, 110, 253));
        colorMap.put(SensorType.BRAKE_PRESSURE, new Color(255, 193, 7));
        colorMap.put(SensorType.THROTTLE, new Color(25, 135, 84));
        colorMap.put(SensorType.SPEED, new Color(111, 66, 193));
        colorMap.put(SensorType.WATER_TEMP, new Color(23, 162, 184));
        
        // 遍历所有传感器类型，自动创建卡片
        for (SensorType type : SensorType.values()) {
            Color color = colorMap.get(type);
            
            if (color == null) {
                // 如果没有定义颜色，使用默认灰色
                color = Color.GRAY;
                System.out.println("警告: 传感器 " + type.getDisplayName() + " 未定义颜色，使用默认灰色");
            }
            
            if ("进度条".equals(type.getDisplayType())) {
                // 创建带进度条的卡片
                BarSensorCard card = new BarSensorCard(
                    type.getDisplayName(), 
                    type.getUnit(), 
                    color,
                    0, 100  // 默认范围0-100，可以根据需要调整
                );
                sensorCardsPanel.add(card);
                sensorCardsMap.put(type.getKey(), card);
            } else {
                // 创建普通卡片
                SensorDataCard card = new SensorDataCard(
                    type.getDisplayName(), 
                    type.getUnit(), 
                    color
                );
                sensorCardsPanel.add(card);
                sensorCardsMap.put(type.getKey(), card);
            }
        }
        
        System.out.println("已创建 " + sensorCardsMap.size() + " 个传感器卡片");
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
            // 连接成功
            isConnected = true;
            connectButton.setText(" 断开连接");
            connectButton.setBackground(new Color(220, 53, 69));

            SerialConfig config = serialHelper.getCurrentConfig();
            statusLabel.setText("✓ 已连接: " + config.getPortName() +
                              " [波特率:" + config.getBaudRate() + "]");
            statusLabel.setForeground(new Color(25, 135, 84));

            // 设置数据监听器
            setupDataListener();
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
                    String sensorKey = keyValue[0].trim();
                    String valueStr = keyValue[1].trim();

                    System.out.println("  传感器类型: [" + sensorKey + "]");
                    System.out.println("  原始数值: [" + valueStr + "]");

                    // 清理数值字符串：只保留数字、小数点、负号
                    valueStr = valueStr.replaceAll("[^0-9.\\-]", "");
                    System.out.println("  清理后数值: [" + valueStr + "]");

                    if (!valueStr.isEmpty()) {
                        try {
                            double value = Double.parseDouble(valueStr);
                            System.out.println("  解析数值: " + value);

                            // ========== 使用枚举验证传感器类型 ==========
                            SensorType type = SensorType.fromKey(sensorKey);
                            
                            if (type != null) {
                                // 找到了对应的传感器类型
                                System.out.println("  ✓ 识别传感器: " + type.getDisplayName());
                                
                                // 更新对应的传感器卡片
                                if (sensorCardsMap.containsKey(sensorKey)) {
                                    sensorCardsMap.get(sensorKey).updateValue(value);
                                    System.out.println("  ✓ 更新成功: " + type.getDisplayName());
                                } else {
                                    System.out.println("  ✗ 卡片不存在: " + sensorKey);
                                }
                            } else {
                                // 未知的传感器类型
                                System.out.println("  ✗ 未知传感器: " + sensorKey);
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
        
        statusLabel.setText("未连接 - 点击\"连接串口\"开始");
        statusLabel.setForeground(Color.BLACK);
    }

}
