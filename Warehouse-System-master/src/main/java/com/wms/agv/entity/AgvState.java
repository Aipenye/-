package com.wms.agv.entity;

public enum AgvState {
    IDLE,           // 待机，停在停车区
    RUNNING,        // 正常行驶
    CAUTION,        // 减速区
    EMERGENCY_STOP, // 急停
    REVERSING,      // 倒车避障中
    LIFTING,        // 举升作业中
    WAITING_LOCK,   // 等待通道锁释放
    RETURNING       // 任务完成，返回停车区
}
