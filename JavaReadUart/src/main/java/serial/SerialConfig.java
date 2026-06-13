package serial;

/**
 * 串口配置类
 * 用于封装串口通信的各项参数配置，包括端口名称、波特率、数据位、停止位、校验位等
 */
public class SerialConfig {
    private String portName;    // 串口名称（如 COM1, /dev/ttyUSB0）
    private int baudRate;    // 波特率（每秒传输的比特数，常用值：9600, 115200等）
    private int dataBits;    // 数据位（每个字符的数据位数，通常为8）
    private int stopBits;    // 停止位（用于标识一个字符结束的位数，通常为1）
    private int parity;    // 校验位（奇偶校验方式：0=无校验, 1=奇校验, 2=偶校验）
    private boolean isOpen;    // 串口打开状态标识
    
    /**
     * 默认构造函数
     * 使用常用默认参数：波特率9600，8个数据位，1个停止位，无校验
     */
    public SerialConfig() {
        this.baudRate = 9600;      // 默认波特率9600bps
        this.dataBits = 8;         // 默认8个数据位
        this.stopBits = 1;         // 默认1个停止位
        this.parity = 0;           // 默认无校验
        this.isOpen = false;       // 初始状态为未打开
    }
    
    /**
     * 带参数的构造函数
     * @param portName 串口名称
     * @param baudRate 波特率
     */
    public SerialConfig(String portName, int baudRate) {
        this.portName = portName;  // 设置串口名称
        this.baudRate = baudRate;  // 设置波特率
        this.dataBits = 8;         // 默认8个数据位
        this.stopBits = 1;         // 默认1个停止位
        this.parity = 0;           // 默认无校验
        this.isOpen = false;       // 初始状态为未打开
    }
    
    /**
     * 获取串口名称
     * @return 串口名称字符串
     */
    public String getPortName() {
        return portName;
    }
    
    /**
     * 设置串口名称
     * @param portName 要设置的串口名称
     */
    public void setPortName(String portName) {
        this.portName = portName;
    }
    
    /**
     * 获取波特率
     * @return 当前波特率值
     */
    public int getBaudRate() {
        return baudRate;
    }
    
    /**
     * 设置波特率
     * @param baudRate 要设置的波特率值
     */
    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }
    
    /**
     * 获取数据位数量
     * @return 数据位数量（通常为7或8）
     */
    public int getDataBits() {
        return dataBits;
    }
    
    /**
     * 设置数据位数量
     * @param dataBits 要设置的数据位数量
     */
    public void setDataBits(int dataBits) {
        this.dataBits = dataBits;
    }
    
    /**
     * 获取停止位数量
     * @return 停止位数量（1或2）
     */
    public int getStopBits() {
        return stopBits;
    }
    
    /**
     * 设置停止位数量
     * @param stopBits 要设置的停止位数量
     */
    public void setStopBits(int stopBits) {
        this.stopBits = stopBits;
    }
    
    /**
     * 获取校验方式
     * @return 校验值（0=无校验, 1=奇校验, 2=偶校验）
     */
    public int getParity() {
        return parity;
    }
    
    /**
     * 设置校验方式
     * @param parity 校验值（0=无校验, 1=奇校验, 2=偶校验）
     */
    public void setParity(int parity) {
        this.parity = parity;
    }
    
    /**
     * 检查串口是否已打开
     * @return true表示串口已打开，false表示未打开
     */
    public boolean isOpen() {
        return isOpen;
    }
    
    /**
     * 设置串口打开状态
     * @param open true表示串口已打开，false表示未打开
     */
    public void setOpen(boolean open) {
        isOpen = open;
    }
    
    /**
     * 返回配置信息的字符串表示
     * @return 包含所有配置参数的格式化字符串
     */
    @Override
    public String toString() {
        return "SerialConfig{" +
                "portName='" + portName + '\'' +
                ", baudRate=" + baudRate +
                ", dataBits=" + dataBits +
                ", stopBits=" + stopBits +
                ", parity=" + parity +
                ", isOpen=" + isOpen +
                '}';
    }
}
