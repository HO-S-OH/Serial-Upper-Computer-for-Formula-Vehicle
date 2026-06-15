package sensor;

/**
 * 传感器类型枚举
 * 定义所有支持的传感器类型及其对应的键名
 */
public enum SensorType {
    TS_VOLTAGE("ts_voltage", "TS电压", "V", "普通"),
    TS_CURRENT("ts_current", "TS电流", "A", "普通"),
    BRAKE_PRESSURE("brake_pressure", "刹车压力", "Bar", "普通"),
    THROTTLE("throttle", "电门开度", "%", "进度条"),
    SPEED("speed", "车速", "km/h", "普通"),
    WATER_TEMP("water_temp", "水温", "°C", "普通"),
    
    TIRE_TEMP_FL("tire_temp_fl", "左前胎温", "°C", "普通"),
    TIRE_TEMP_FR("tire_temp_fr", "右前胎温", "°C", "普通"),
    TIRE_TEMP_RL("tire_temp_rl", "左后胎温", "°C", "普通"),
    TIRE_TEMP_RR("tire_temp_rr", "右后胎温", "°C", "普通"),
    
    MIN_CELL_VOLT("min_cell_volt", "最低电芯电压", "V", "普通"),
    CELL_TEMP("cell_temp", "电芯温度", "°C", "普通"),
    
    MOTOR_TEMP("motor_temp", "电机温度", "°C", "普通"),
    MOTOR_CTRL_TEMP("motor_ctrl_temp", "电机控制器温度", "°C", "普通"),
    
    FRONT_WHEEL_SPEED("front_wheel_speed", "前轮轮速", "rpm", "普通"),
    POWER_CONSUMPTION("power_consumption", "用电量", "Wh", "普通"),
    
    STEERING_ANGLE("steering_angle", "方向盘角度", "°", "双向进度条"),
    SUSPENSION_FL("suspension_fl", "左前悬架位移", "mm", "双向进度条"),
    SUSPENSION_FR("suspension_fr", "右前悬架位移", "mm", "双向进度条"),
    COOLANT_TEMP("coolant_temp", "水箱温度", "°C", "普通");

    private String key;
    private String displayName;
    private String unit;
    private String displayType;
    
    SensorType(String key, String displayName, String unit, String displayType) {
        this.key = key;
        this.displayName = displayName;
        this.unit = unit;
        this.displayType = displayType;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public String getDisplayType() {
        return displayType;
    }
    
    /**
     * 根据键名获取传感器类型
     * @param key 数据键名
     * @return 对应的传感器类型，如果不存在返回null
     */
    public static SensorType fromKey(String key) {
        for (SensorType type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }
        return null;
    }
}
