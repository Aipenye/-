package com.wms.agv.dto;

import lombok.Data;
import java.util.List;

@Data
public class AgvDTO {
    private int id;
    private double x;
    private double y;
    private double angle;
    private double speed;
    private String state;
    private boolean loaded;
    private List<double[]> path;
    private int currentOrderId;
    private int targetSlot;
}
