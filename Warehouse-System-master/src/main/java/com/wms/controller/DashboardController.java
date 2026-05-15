package com.wms.controller;

import com.wms.common.Result;
import com.wms.entity.Storage;
import com.wms.service.GoodsService;
import com.wms.service.RecordService;
import com.wms.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired private RecordService recordService;
    @Autowired private GoodsService goodsService;
    @Autowired private StorageService storageService;

    @GetMapping("/stats")
    public Result stats() {
        List<String> dates = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 4; i >= 0; i--) {
            dates.add(LocalDate.now().minusDays(i).format(fmt));
        }

        List<Map<String, Object>> inRaw = recordService.dailyInCount(5);
        List<Map<String, Object>> outRaw = recordService.dailyOutCount(5);
        List<Map<String, Object>> orderRaw = recordService.dailyOrderCount(5);

        List<Long> inData = fillDates(dates, inRaw, fmt);
        List<Long> outData = fillDates(dates, outRaw, fmt);
        List<Long> orderData = fillDates(dates, orderRaw, fmt);

        List<Map<String, Object>> goodsTypeData = goodsService.countByType();

        // 仓库使用情况：按占用率降序取前15
        List<Map<String, Object>> storageUsage = buildStorageUsage();

        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("inData", inData);
        result.put("outData", outData);
        result.put("orderData", orderData);
        result.put("goodsTypeData", goodsTypeData);
        result.put("storageUsage", storageUsage);
        return Result.success(result);
    }

    // 近30天每日数据（文字展示用）
    @GetMapping("/daily")
    public Result daily(@RequestParam String type) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if ("storage".equals(type)) {
            // 返回当前所有仓库快照
            List<Map<String, Object>> list = buildStorageUsage();
            return Result.success(list);
        }
        List<Map<String, Object>> raw;
        if ("in".equals(type)) {
            raw = recordService.dailyInCount(30);
        } else if ("out".equals(type)) {
            raw = recordService.dailyOutCount(30);
        } else {
            raw = recordService.dailyOrderCount(30);
        }
        // 补全近30天，无数据的填0
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Long> map = new HashMap<>();
        for (Map<String, Object> row : raw) {
            String day = row.get("day").toString();
            if (day.length() > 10) day = day.substring(0, 10);
            map.put(day, ((Number) row.get("cnt")).longValue());
        }
        for (int i = 29; i >= 0; i--) {
            String day = LocalDate.now().minusDays(i).format(fmt);
            Map<String, Object> item = new HashMap<>();
            item.put("date", day);
            item.put("value", map.getOrDefault(day, 0L));
            result.add(item);
        }
        return Result.success(result);
    }

    private List<Map<String, Object>> buildStorageUsage() {
        List<Storage> storages = storageService.listAllWithUsedVolume();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Storage s : storages) {
            double len = s.getLength() == null ? 0 : s.getLength();
            double hei = s.getHeight() == null ? 0 : s.getHeight();
            double wid = s.getWidth()  == null ? 0 : s.getWidth();
            double totalVolume = len * hei * wid;
            if (totalVolume <= 0) continue;
            double usedVolume = s.getUsedVolume() == null ? 0 : s.getUsedVolume();
            double rate = Math.min(100.0, Math.round(usedVolume * 1000.0 / totalVolume) / 10.0);
            Map<String, Object> item = new HashMap<>();
            item.put("name", s.getName());
            item.put("capacity", Math.round(totalVolume));
            item.put("used", Math.round(usedVolume));
            item.put("rate", rate);
            list.add(item);
        }
        list.sort((a, b) -> Double.compare((Double) b.get("rate"), (Double) a.get("rate")));
        return list.size() > 15 ? list.subList(0, 15) : list;
    }

    private List<Long> fillDates(List<String> dates, List<Map<String, Object>> raw, DateTimeFormatter fmt) {
        Map<String, Long> map = new HashMap<>();
        for (Map<String, Object> row : raw) {
            String day = row.get("day").toString();
            if (day.length() > 5) {
                day = LocalDate.parse(day.length() > 10 ? day.substring(0, 10) : day).format(fmt);
            }
            map.put(day, ((Number) row.get("cnt")).longValue());
        }
        List<Long> result = new ArrayList<>();
        for (String d : dates) {
            result.add(map.getOrDefault(d, 0L));
        }
        return result;
    }
}
