package serial;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Excel数据导出工具类
 * 用于将串口接收的传感器数据导出为.xlsx格式文件
 * 支持多工作表、数据统计分析等功能
 */
public class ExcelExporter {
    private Workbook workbook;
    private Sheet currentSheet;
    private int rowCount;
    private List<String> columnHeaders;
    private String outputFileName;
    
    /**
     * 构造函数
     * 初始化Excel工作簿并设置默认表头
     */
    public ExcelExporter() {
        this.workbook = new XSSFWorkbook();
        this.columnHeaders = new ArrayList<>();
        this.rowCount = 0;
        this.outputFileName = generateDefaultFileName();
    }
    
    /**
     * 生成默认文件名（基于时间戳）
     * @return 格式化的文件名，如：racing_data_20260613_143025.xlsx
     */
    private String generateDefaultFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return "racing_data_" + sdf.format(new Date()) + ".xlsx";
    }
    
    /**
     * 创建工作表
     * @param sheetName 工作表名称
     * @param headers 列标题数组
     */
    public void createSheet(String sheetName, String[] headers) {
        this.currentSheet = workbook.createSheet(sheetName);
        this.rowCount = 0;
        this.columnHeaders.clear();
        
        if (headers != null && headers.length > 0) {
            for (String header : headers) {
                columnHeaders.add(header);
            }
            
            // 创建表头行
            Row headerRow = currentSheet.createRow(rowCount++);
            CellStyle headerStyle = createHeaderStyle();
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                currentSheet.autoSizeColumn(i);
            }
        }
    }
    
    /**
     * 创建表头样式
     * @return 加粗背景色的单元格样式
     */
    private CellStyle createHeaderStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        
        // 设置背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 设置边框
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    /**
     * 添加一行传感器数据
     * @param data 数据数组（按列顺序）
     */
    public void addDataRow(Object[] data) {
        if (currentSheet == null || data == null || data.length == 0) {
            return;
        }
        
        Row row = currentSheet.createRow(rowCount++);
        CellStyle dataStyle = createDataStyle();
        
        for (int i = 0; i < data.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(dataStyle);
            
            if (data[i] instanceof Number) {
                cell.setCellValue(((Number) data[i]).doubleValue());
            } else if (data[i] instanceof Date) {
                cell.setCellValue((Date) data[i]);
            } else {
                cell.setCellValue(data[i].toString());
            }
        }
        
        // 每100行自动调整一次列宽
        if (rowCount % 100 == 0) {
            for (int i = 0; i < columnHeaders.size(); i++) {
                currentSheet.autoSizeColumn(i);
            }
        }
    }
    
    /**
     * 创建数据单元格样式
     * @return 带边框的普通样式
     */
    private CellStyle createDataStyle() {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    /**
     * 保存到文件
     * @param fileName 输出文件名
     * @return true表示成功，false表示失败
     */
    public boolean saveToFile(String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            workbook.write(fos);
            System.out.println("✓ Excel文件已保存: " + fileName);
            System.out.println("  共保存 " + (rowCount - 1) + " 条数据记录");
            return true;
        } catch (IOException e) {
            System.err.println("✗ 保存Excel文件失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 保存到默认文件
     * @return true表示成功，false表示失败
     */
    public boolean saveToDefaultFile() {
        return saveToFile(outputFileName);
    }
    
    /**
     * 关闭工作簿释放资源
     */
    public void close() {
        try {
            if (workbook != null) {
                workbook.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取当前行数（包含表头）
     * @return 当前总行数
     */
    public int getRowCount() {
        return rowCount;
    }
    
    /**
     * 获取输出文件名
     * @return 当前输出文件名
     */
    public String getOutputFileName() {
        return outputFileName;
    }
    
    /**
     * 设置输出文件名
     * @param fileName 新的文件名
     */
    public void setOutputFileName(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            this.outputFileName = fileName.endsWith(".xlsx") ? fileName : fileName + ".xlsx";
        }
    }
    
    /**
     * 重置导出器（用于新的会话）
     * 创建新的工作簿并重置计数器
     */
    public void reset() {
        try {
            if (this.workbook != null) {
                this.workbook.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        this.workbook = new XSSFWorkbook();
        this.currentSheet = null;
        this.rowCount = 0;
        this.columnHeaders.clear();
        this.outputFileName = generateDefaultFileName();
    }
}
