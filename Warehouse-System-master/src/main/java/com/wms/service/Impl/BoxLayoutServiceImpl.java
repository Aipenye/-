package com.wms.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.BoxLayout;
import com.wms.mapper.BoxLayoutMapper;
import com.wms.service.BoxLayoutService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BoxLayoutServiceImpl extends ServiceImpl<BoxLayoutMapper, BoxLayout> implements BoxLayoutService {

    @Override
    public BoxLayout getByStorageId(Integer storageId) {
        return lambdaQuery().eq(BoxLayout::getStorageId, storageId).one();
    }

    @Override
    public void saveOrUpdateLayout(Integer storageId, String layoutJson) {
        BoxLayout existing = getByStorageId(storageId);
        if (existing != null) {
            existing.setLayoutJson(layoutJson);
            existing.setUpdatedAt(LocalDateTime.now());
            updateById(existing);
        } else {
            BoxLayout layout = new BoxLayout();
            layout.setStorageId(storageId);
            layout.setLayoutJson(layoutJson);
            layout.setUpdatedAt(LocalDateTime.now());
            save(layout);
        }
    }
}
