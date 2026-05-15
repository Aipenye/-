package com.wms.agv.entity;

public enum AgvState {
    IDLE,           // 待机，停在停车区
    RUNNING,        // 正常行驶
    CAUTION,        // 减速区
    EMERGENCY_STOP, // 急停
    REVERSING,      // 倒车避障中
    LIFTING,        // 举升作业中
    WAITING_LOCK,   // 等待通道锁释放
    RETURNING,      // 任务完成，返回停车区
    EXITING_PARK,   // 驶出停车位（向正北行驶到出入口点）
    PARKING         // 倒车入库（从出入口点向正南倒入停车位）
}
