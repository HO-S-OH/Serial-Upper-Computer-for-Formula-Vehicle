package ui;

import serial.SerialConfig;
import serial.SerialHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * 串口配置对话框
 * 用于选择和配置串口参数，包括串口号、波特率、数据位、停止位、校验位
 */
public class SerialConfigDialog extends JDialog {
    private JComboBox<String> portComboBox;
    private JComboBox<Integer> baudRateComboBox;
    private JComboBox<Integer> dataBitsComboBox;
    private JComboBox<Integer> stopBitsComboBox;
    private JComboBox<String> parityComboBox;
    private JButton refreshButton;
    private JButton confirmButton;
    private JButton cancelButton;
    private boolean confirmed = false;
    
    private SerialHelper serialHelper;
    
    /**
     * 构造函数
     * @param parent 父窗口
     * @param helper 串口助手对象
     */
    public SerialConfigDialog(Frame parent, SerialHelper helper) {
        super(parent, "串口配置", true);
        this.serialHelper = helper;
        
        setSize(600, 480);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        initUI();
    }
    
    /**
     * 初始化UI
     */
    private void initUI() {
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
        mainPanel.add(createFormPanel(), BorderLayout.CENTER);
        
        // 按钮区域
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // 初始刷新串口列表
        refreshPorts();
    }
    
    /**
     * 创建配置表单
     */
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 245, 245));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 串口选择
        addLabelAndComponent(formPanel, gbc, "串口:", 0, 0, createPortPanel());
        
        // 波特率
        addLabelAndComponent(formPanel, gbc, "波特率:", 0, 1, createBaudRateComboBox());
        
        // 数据位
        addLabelAndComponent(formPanel, gbc, "数据位:", 0, 2, createDataBitsComboBox());
        
        // 停止位
        addLabelAndComponent(formPanel, gbc, "停止位:", 0, 3, createStopBitsComboBox());
        
        // 校验位
        addLabelAndComponent(formPanel, gbc, "校验:", 0, 4, createParityComboBox());
        
        return formPanel;
    }
    
    /**
     * 添加标签和组件
     */
    private void addLabelAndComponent(JPanel panel, GridBagConstraints gbc, 
                                     String labelText, int x, int y, JComponent component) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(label, gbc);
        
        gbc.gridx = x + 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(component, gbc);
    }
    
    /**
     * 创建串口选择面板
     */
    private JPanel createPortPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        
        portComboBox = new JComboBox<>();
        portComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        portComboBox.setPreferredSize(new Dimension(230, 36));
        panel.add(portComboBox, BorderLayout.CENTER);
        
        refreshButton = new JButton("刷新");
        refreshButton.setFont(new Font("微软雅黑", Font.BOLD, 13));
        refreshButton.setPreferredSize(new Dimension(80, 36));
        refreshButton.setBackground(new Color(230, 230, 230));
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 2));
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> refreshPorts());
        panel.add(refreshButton, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * 创建波特率下拉框
     */
    private JComboBox<Integer> createBaudRateComboBox() {
        baudRateComboBox = new JComboBox<>(new Integer[]{
            300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 38400, 
            57600, 115200, 230400, 460800, 921600
        });
        baudRateComboBox.setSelectedItem(9600);
        baudRateComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        baudRateComboBox.setPreferredSize(new Dimension(320, 36));
        return baudRateComboBox;
    }
    
    /**
     * 创建数据位下拉框
     */
    private JComboBox<Integer> createDataBitsComboBox() {
        dataBitsComboBox = new JComboBox<>(new Integer[]{5, 6, 7, 8});
        dataBitsComboBox.setSelectedItem(8);
        dataBitsComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        dataBitsComboBox.setPreferredSize(new Dimension(320, 36));
        return dataBitsComboBox;
    }
    
    /**
     * 创建停止位下拉框
     */
    private JComboBox<Integer> createStopBitsComboBox() {
        stopBitsComboBox = new JComboBox<>(new Integer[]{1, 2});
        stopBitsComboBox.setSelectedItem(1);
        stopBitsComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        stopBitsComboBox.setPreferredSize(new Dimension(320, 36));
        return stopBitsComboBox;
    }
    
    /**
     * 创建校验位下拉框
     */
    private JComboBox<String> createParityComboBox() {
        parityComboBox = new JComboBox<>(new String[]{"无", "奇校验", "偶校验"});
        parityComboBox.setSelectedIndex(0);
        parityComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        parityComboBox.setPreferredSize(new Dimension(320, 36));
        return parityComboBox;
    }
    
    /**
     * 创建按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 20));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // 确认按钮
        confirmButton = new JButton("确认连接");
        confirmButton.setPreferredSize(new Dimension(140, 45));
        confirmButton.setFont(new Font("微软雅黑", Font.BOLD, 15));
        confirmButton.setBackground(new Color(200, 240, 220));
        confirmButton.setForeground(Color.BLACK);
        confirmButton.setFocusPainted(false);
        confirmButton.setBorder(BorderFactory.createLineBorder(new Color(80, 160, 120), 2));
        confirmButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmButton.addActionListener(this::onConfirm);
        buttonPanel.add(confirmButton);
        
        // 取消按钮
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
        
        return buttonPanel;
    }
    
    /**
     * 确认按钮事件
     */
    private void onConfirm(ActionEvent e) {
        if (connectPort()) {
            confirmed = true;
            dispose();
        }
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
     * @return true表示连接成功
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
