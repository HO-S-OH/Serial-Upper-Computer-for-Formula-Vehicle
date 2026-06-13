import ui.SerialGuiApp;

/**
 * 串口读取工具主程序
 * 提供GUI图形界面模式，用于串口数据的接收和发送
 */
public class SerialReader {
    
    /**
     * 程序主入口
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        // 启动GUI图形界面模式
        runGuiMode();
    }
    
    /**
     * 运行GUI模式
     * 在Swing事件分发线程(EDT)中创建并显示GUI窗口
     * 使用系统原生外观主题
     */
    private static void runGuiMode() {
        // 在Swing EDT线程中执行UI操作，确保线程安全
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                // 设置界面外观为系统原生风格（Windows/Mac/Linux）
                javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // 如果设置外观失败，打印异常信息并使用默认外观
                e.printStackTrace();
            }
            // 创建串口助手GUI窗口并设置为可见
            new SerialGuiApp().setVisible(true);
        });
    }
}