package serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

/**
 * 串口助手类
 * 封装了串口通信的核心功能，包括串口扫描、打开/关闭、数据收发等操作
 * 基于jSerialComm库实现跨平台串口通信
 */
public class SerialHelper {
    // jSerialComm串口对象实例
    private SerialPort serialPort;
    
    // 当前串口配置
    private SerialConfig currentConfig;

    /**
     * 默认构造函数
     * 初始化串口对象为空，使用默认配置
     */
    public SerialHelper() {
        this.serialPort = null;
        this.currentConfig = new SerialConfig();
    }

    /**
     * 列出所有可用串口
     * 扫描系统中所有可用的串口设备并返回格式化信息
     * @return 串口信息数组，格式为 "索引 -> 端口名 (描述信息)"
     */
    public String[] listAvailablePorts() {
        // 获取系统中所有串口设备
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] portInfo = new String[ports.length];

        // 遍历所有串口，构建格式化字符串
        for (int i = 0; i < ports.length; i++) {
            portInfo[i] = i + " -> " + ports[i].getSystemPortName() +
                         " (" + ports[i].getDescriptivePortName() + ")";
        }

        return portInfo;
    }

    /**
     * 获取串口数量
     * @return 系统中可用串口的总数
     */
    public int getPortCount() {
        return SerialPort.getCommPorts().length;
    }

    /**
     * 打开指定串口（使用默认配置）
     * 根据索引选择串口并使用默认参数打开（波特率9600，8个数据位，1个停止位，无校验）
     * @param portIndex 要打开的串口索引（从listAvailablePorts获取）
     * @return true表示成功打开，false表示失败（索引无效或串口被占用）
     */
    public boolean openPort(int portIndex) {
        return openPort(portIndex, currentConfig);
    }

    /**
     * 打开指定串口（使用自定义配置）
     * 根据索引选择串口并使用指定的通信参数打开
     * @param portIndex 要打开的串口索引（从listAvailablePorts获取）
     * @param config 串口配置对象，包含波特率、数据位、停止位、校验位等参数
     * @return true表示成功打开，false表示失败（索引无效或串口被占用）
     */
    public boolean openPort(int portIndex, SerialConfig config) {
        // 获取所有串口设备列表
        SerialPort[] ports = SerialPort.getCommPorts();

        // 验证索引是否有效
        if (portIndex < 0 || portIndex >= ports.length) {
            System.out.println("无效的串口号！");
            return false;
        }

        // 保存选中的串口对象
        this.serialPort = ports[portIndex];
        
        // 保存配置信息
        this.currentConfig = config;
        this.currentConfig.setPortName(serialPort.getSystemPortName());

        // 转换停止位和校验位为jSerialComm的常量
        int stopBits = convertStopBits(config.getStopBits());
        int parity = convertParity(config.getParity());

        // 配置串口通信参数
        serialPort.setComPortParameters(
                config.getBaudRate(),              // 波特率
                config.getDataBits(),              // 数据位
                stopBits,                          // 停止位
                parity                             // 校验位
        );

        // 尝试打开串口
        if (!serialPort.openPort()) {
            System.out.println("打不开串口，可能被占用");
            return false;
        }

        this.currentConfig.setOpen(true);
        System.out.println("串口打开成功：" + serialPort.getSystemPortName() + 
                          " [波特率:" + config.getBaudRate() + ", 数据位:" + config.getDataBits() + 
                          ", 停止位:" + config.getStopBits() + ", 校验:" + config.getParity() + "]");
        return true;
    }

    /**
     * 转换停止位数值为jSerialComm常量
     * @param stopBits 停止位数值（1或2）
     * @return jSerialComm的停止位常量
     */
    private int convertStopBits(int stopBits) {
        switch (stopBits) {
            case 1:
                return SerialPort.ONE_STOP_BIT;
            case 2:
                return SerialPort.TWO_STOP_BITS;
            default:
                return SerialPort.ONE_STOP_BIT;
        }
    }

    /**
     * 转换校验位数值为jSerialComm常量
     * @param parity 校验位数值（0=无校验, 1=奇校验, 2=偶校验）
     * @return jSerialComm的校验位常量
     */
    private int convertParity(int parity) {
        switch (parity) {
            case 0:
                return SerialPort.NO_PARITY;
            case 1:
                return SerialPort.ODD_PARITY;
            case 2:
                return SerialPort.EVEN_PARITY;
            default:
                return SerialPort.NO_PARITY;
        }
    }

    /**
     * 设置自定义数据监听器
     * 用于监听串口接收到的数据事件
     * @param listener 实现了SerialPortDataListener接口的监听器对象
     */
    public void setDataListener(SerialPortDataListener listener) {
        // 只有在串口存在且已打开时才设置监听器
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.addDataListener(listener);
        }
    }

    /**
     * 设置默认数据监听器
     * 监听串口数据并在控制台打印接收到的内容
     * 适用于简单的命令行模式数据接收
     */
    public void setDefaultListener() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    // 监听数据可用事件
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    // 创建缓冲区读取数据
                    byte[] buffer = new byte[serialPort.bytesAvailable()];
                    serialPort.readBytes(buffer, buffer.length);
                    String data = new String(buffer);
                    System.out.print("收到：" + data);
                }
            });
        }
    }

    /**
     * 关闭串口
     * 如果串口已打开则关闭它，释放系统资源
     */
    public void closePort() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            this.currentConfig.setOpen(false);
            System.out.println("\n串口已关闭");
        }
    }

    /**
     * 检查串口是否打开
     * @return true表示串口已打开且可用，false表示未打开或对象为空
     */
    public boolean isOpen() {
        return serialPort != null && serialPort.isOpen();
    }

    /**
     * 获取当前串口名称
     * @return 串口的系统名称（如COM3），如果未打开则返回"未打开"
     */
    public String getPortName() {
        return serialPort != null ? serialPort.getSystemPortName() : "未打开";
    }

    /**
     * 获取当前串口配置
     * @return 当前使用的串口配置对象
     */
    public SerialConfig getCurrentConfig() {
        return currentConfig;
    }

    /**
     * 获取当前可用字节数
     * 查询串口接收缓冲区中等待读取的字节数量
     * @return 可用字节数，如果串口未打开则返回0
     */
    public int getBytesAvailable() {
        return serialPort != null ? serialPort.bytesAvailable() : 0;
    }
    
    /**
     * 读取字节数据
     * 从串口接收缓冲区读取指定长度的数据到缓冲区
     * @param buffer 用于存储读取数据的字节数组
     * @param length 要读取的最大字节数
     * @return 实际读取的字节数，如果串口未打开则返回0
     */
    public int readBytes(byte[] buffer, int length) {
        return serialPort != null ? serialPort.readBytes(buffer, length) : 0;
    }
    
    /**
     * 发送数据
     * 将字节数组通过串口发送出去
     * @param data 要发送的字节数组数据
     * @return true表示全部发送成功，false表示发送失败或部分发送
     */
    public boolean sendData(byte[] data) {
        if (serialPort != null && serialPort.isOpen()) {
            // 写入数据到串口
            int bytesWritten = serialPort.writeBytes(data, data.length);
            // 检查是否所有字节都成功发送
            return bytesWritten == data.length;
        }
        return false;
    }
}