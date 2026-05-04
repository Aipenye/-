package com.wms.controller;

import com.wms.common.Result;
import com.wms.entity.Goods;
import com.wms.entity.Record;
import com.wms.entity.Storage;
import com.wms.entity.User;
import com.wms.entity.WorkOrder;
import com.wms.service.GoodsService;
import com.wms.service.RecordService;
import com.wms.service.StorageService;
import com.wms.service.UserService;
import com.wms.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/workorder")
public class WorkOrderController {

    @Autowired private WorkOrderService workOrderService;
    @Autowired private GoodsService goodsService;
    @Autowired private RecordService recordService;
    @Autowired private StorageService storageService;
    @Autowired private UserService userService;

    @PostMapping("/save")
    public Result save(@RequestBody WorkOrder workOrder) {
        Goods goods = goodsService.getById(workOrder.getGoodsId());
        Storage storage = storageService.getById(workOrder.getStorageId());

        if (workOrder.getType() != null && workOrder.getType() == 1) {
            // 出库：检查货物库存
            int stock = (goods == null || goods.getCount() == null) ? 0 : goods.getCount();
            if (stock < workOrder.getCount()) {
                return Result.fail("货物库存不足，当前库存：" + stock);
            }
        } else {
            // 入库：检查仓库剩余容量
            if (storage != null && storage.getCapacity() != null && storage.getCapacity() > 0) {
                int used = (storage.getUsed() == null) ? 0 : storage.getUsed();
                int remaining = storage.getCapacity() - used;
                if (remaining < workOrder.getCount()) {
                    return Result.fail("仓库容量不足，剩余容量：" + remaining);
                }
            }
        }

        workOrder.setStatus(0);
        workOrder.setCreateTime(LocalDateTime.now());
        workOrder.setWorkerId(null);

        User worker = findIdleWorker();
        if (worker != null) {
            workOrder.setWorkerId(worker.getId());
            workOrderService.save(workOrder);
            worker.setWorkStatus(2);
            userService.updateById(worker);
            return Result.success("工单已派发给工人：" + worker.getName());
        } else {
            workOrderService.save(workOrder);
            return Result.success("暂无空闲工人，工单已进入缓冲池");
        }
    }

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) Integer workerId,
                       @RequestParam(required = false) Integer status) {
        List<WorkOrder> list = workOrderService.listWithDetail(workerId, status);
        return Result.success(list);
    }

    @PostMapping("/finish")
    public Result finish(@RequestParam Integer id) {
        WorkOrder workOrder = workOrderService.getById(id);
        if (workOrder == null) return Result.fail();

        int delta = (workOrder.getType() != null && workOrder.getType() == 1)
                ? -workOrder.getCount() : workOrder.getCount();

        Goods goods = goodsService.getById(workOrder.getGoodsId());
        if (goods != null) {
            goods.setStorage(workOrder.getStorageId());
            goods.setCount(goods.getCount() + delta);
            goodsService.updateById(goods);
        }

        Storage storage = storageService.getById(workOrder.getStorageId());
        if (storage != null) {
            int used = (storage.getUsed() == null ? 0 : storage.getUsed()) + delta;
            storage.setUsed(Math.max(0, used));
            storageService.updateById(storage);
        }

        Record record = new Record();
        record.setGoods(workOrder.getGoodsId());
        record.setUserid(workOrder.getWorkerId());
        record.setAdminId(workOrder.getAdminId());
        record.setCount(delta);
        record.setCreatetime(LocalDateTime.now());
        record.setRemark("工单#" + workOrder.getId() + (delta > 0 ? " 完成入库" : " 完成出库"));
        recordService.save(record);

        workOrder.setStatus(1);
        workOrder.setFinishTime(LocalDateTime.now());
        workOrderService.updateById(workOrder);

        if (workOrder.getWorkerId() != null) {
            User worker = userService.getById(workOrder.getWorkerId());
            if (worker != null) {
                WorkOrder pending = workOrderService.lambdaQuery()
                        .isNull(WorkOrder::getWorkerId)
                        .eq(WorkOrder::getStatus, 0)
                        .orderByAsc(WorkOrder::getCreateTime)
                        .last("LIMIT 1")
                        .one();
                if (pending != null) {
                    pending.setWorkerId(worker.getId());
                    workOrderService.updateById(pending);
                    worker.setWorkStatus(2);
                } else {
                    worker.setWorkStatus(1);
                }
                userService.updateById(worker);
            }
        }

        return Result.success();
    }

    @GetMapping("/del")
    public Result del(@RequestParam Integer id) {
        return workOrderService.removeById(id) ? Result.success() : Result.fail();
    }

    private User findIdleWorker() {
        List<User> idleWorkers = userService.lambdaQuery()
                .eq(User::getWorkStatus, 1)
                .eq(User::getRoleId, 2)
                .list();
        if (idleWorkers.isEmpty()) return null;
        LocalDate today = LocalDate.now();
        return idleWorkers.stream().min((a, b) -> {
            long ca = workOrderService.lambdaQuery()
                    .eq(WorkOrder::getWorkerId, a.getId())
                    .eq(WorkOrder::getStatus, 1)
                    .ge(WorkOrder::getFinishTime, today.atStartOfDay())
                    .count();
            long cb = workOrderService.lambdaQuery()
                    .eq(WorkOrder::getWorkerId, b.getId())
                    .eq(WorkOrder::getStatus, 1)
                    .ge(WorkOrder::getFinishTime, today.atStartOfDay())
                    .count();
            return Long.compare(ca, cb);
        }).orElse(null);
    }
}
