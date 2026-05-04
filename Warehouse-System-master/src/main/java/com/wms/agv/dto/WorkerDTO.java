package com.wms.agv.dto;

import lombok.Data;

@Data
public class WorkerDTO {
    private int id;
    private double x;
    private double y;
    private double angle;
    private String mode;
    private String state;
}
