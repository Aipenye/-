package com.wms.agv.ws;

import com.wms.agv.dto.SimStateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class AgvStatePublisher {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void publish(SimStateDTO state) {
        messagingTemplate.convertAndSend("/topic/agv-state", state);
    }
}
