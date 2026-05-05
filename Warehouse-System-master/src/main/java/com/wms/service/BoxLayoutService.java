package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.BoxLayout;

public interface BoxLayoutService extends IService<BoxLayout> {
    BoxLayout getByStorageId(Integer storageId);
    void saveOrUpdateLayout(Integer storageId, String layoutJson);
}
