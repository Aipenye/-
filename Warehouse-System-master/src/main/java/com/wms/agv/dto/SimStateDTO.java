package com.wms.agv.dto;

import lombok.Data;
import java.util.List;

@Data
public class SimStateDTO {
    private long timestamp;
    private boolean running;
    private List<AgvDTO>    agvs;
    private List<WorkerDTO> workers;
}
