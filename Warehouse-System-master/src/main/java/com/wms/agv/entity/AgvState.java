package com.wms.agv.entity;

public enum AgvState {
    IDLE,           // 待机，无任务
    RUNNING,        // 正常行驶
    CAUTION,        // 减速区（工人1~3m）
    EMERGENCY_STOP, // 急停（工人<1m）
    LIFTING,        // 举升作业中
    WAITING_LOCK    // 等待通道锁释放
}
