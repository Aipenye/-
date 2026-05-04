<template>
  <div class="worker-page">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-tabs">
        <span class="tab" :class="{active: filterStatus===-1}" @click="setFilter(-1)">全部</span>
        <span class="tab" :class="{active: filterStatus===0}" @click="setFilter(0)">待处理</span>
        <span class="tab" :class="{active: filterStatus===1}" @click="setFilter(1)">已完成</span>
      </div>
      <span class="order-count">共 {{ tableData.length }} 条工单</span>
    </div>

    <el-empty v-if="tableData.length===0" description="暂无工单" style="margin-top:60px"/>

    <div class="card-grid">
      <div class="order-card" v-for="item in tableData" :key="item.id"
           :class="item.status===1 ? 'card-done' : 'card-pending'">
        <div class="card-head">
          <span class="order-id">工单 #{{ item.id }}</span>
          <div style="display:flex;gap:6px;align-items:center;">
            <el-tag :type="item.type===1?'warning':'success'" size="mini" disable-transitions>
              {{ item.type===1 ? '出库' : '入库' }}
            </el-tag>
            <span class="status-badge" :class="item.status===0 ? 'badge-pending' : 'badge-done'">
              {{ item.status===0 ? '待处理' : '已完成' }}
            </span>
          </div>
        </div>
        <div class="card-body">
          <div class="info-row"><i class="el-icon-box"/> <span class="label">货物</span><b>{{ item.goodsName }}</b></div>
          <div class="info-row"><i class="el-icon-office-building"/> <span class="label">目标货架</span><b>{{ item.storageName }}</b></div>
          <div class="info-row"><i class="el-icon-goods"/> <span class="label">数量</span><b>{{ item.count }}</b></div>
          <div class="info-row deadline" :class="isOverdue(item) ? 'overdue' : ''">
            <i class="el-icon-alarm-clock"/> <span class="label">截止时间</span><b>{{ item.deadline | fmtTime }}</b>
            <span v-if="isOverdue(item)" class="overdue-tag">已逾期</span>
          </div>
          <div class="info-row" v-if="item.remark"><i class="el-icon-chat-line-round"/> <span class="label">备注</span>{{ item.remark }}</div>
          <div class="info-row done-time" v-if="item.status===1">
            <i class="el-icon-circle-check"/> <span class="label">完成时间</span>{{ item.finishTime | fmtTime }}
          </div>
        </div>
        <div class="card-foot" v-if="item.status===0">
          <el-button type="success" size="small" class="finish-btn" @click="finish(item)">
            <i class="el-icon-check"/> 确认完成
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'WorkerOrders',
  filters: {
    fmtTime(v) {
      if (!v) return '-'
      return v.replace('T', ' ').substring(0, 16)
    }
  },
  data() {
    return {
      tableData: [],
      filterStatus: -1,
      user: JSON.parse(sessionStorage.getItem('CurUser')) || {}
    }
  },
  created() {
    this.load()
  },
  methods: {
    setFilter(val) {
      this.filterStatus = val
      this.load()
    },
    isOverdue(item) {
      if (!item.deadline || item.status === 1) return false
      return new Date(item.deadline) < new Date()
    },
    load() {
      const params = { workerId: this.user.id }
      if (this.filterStatus !== -1) params.status = this.filterStatus
      this.$axios.get(this.$httpUrl + '/workorder/list', { params }).then(res => {
        if (res.data.code === 200) this.tableData = res.data.data
      })
    },
    finish(item) {
      this.$confirm(
        `确认完成工单 #${item.id}？\n货物「${item.goodsName}」将存入「${item.storageName}」`,
        '确认完成', { confirmButtonText: '确认完成', cancelButtonText: '取消', type: 'success' }
      ).then(() => {
        this.$axios.post(this.$httpUrl + '/workorder/finish', null, { params: { id: item.id } }).then(res => {
          if (res.data.code === 200) {
            this.$message.success('工单已完成，货物记录已更新')
            this.load()
            this.$emit('orderFinished')
          } else {
            this.$message.error('操作失败')
          }
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.worker-page {
  min-height: 100%;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.filter-tabs {
  display: flex;
  gap: 4px;
  background: rgba(255,255,255,0.8);
  border-radius: 24px;
  padding: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.tab {
  padding: 6px 20px;
  border-radius: 20px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.25s;
  user-select: none;
}
.tab:hover { color: #3498db; transform: scale(1.05); }
.tab.active {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(52,152,219,0.35);
}
.order-count { font-size: 13px; color: #999; }

/* 卡片网格 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.order-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.07);
  transition: transform 0.2s, box-shadow 0.2s;
  border-left: 4px solid #e6a817;
}
.order-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}
.card-done {
  border-left-color: #67c23a;
  opacity: 0.85;
}
.card-pending { border-left-color: #e6a817; }

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px 10px;
  border-bottom: 1px solid #f0f0f0;
}
.order-id { font-weight: 700; font-size: 15px; color: #2c3e50; }

.status-badge {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 12px;
  font-weight: 600;
}
.badge-pending { background: #fef3cd; color: #d68910; }
.badge-done { background: #d4edda; color: #27ae60; }

.card-body { padding: 12px 16px; }
.info-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #555;
  margin-bottom: 7px;
  transition: color 0.2s;
}
.info-row:hover { color: #2c3e50; }
.info-row i { color: #3498db; font-size: 14px; flex-shrink: 0; }
.label { color: #999; min-width: 56px; }
.info-row b { color: #2c3e50; }

.deadline.overdue i, .deadline.overdue b { color: #e74c3c; }
.overdue-tag {
  margin-left: 6px;
  font-size: 11px;
  background: #fde8e8;
  color: #e74c3c;
  padding: 1px 6px;
  border-radius: 8px;
}
.done-time { color: #27ae60; }
.done-time i { color: #27ae60; }

.card-foot {
  padding: 10px 16px 14px;
  text-align: right;
}
.finish-btn {
  border-radius: 20px;
  padding: 7px 20px;
  font-weight: 600;
  transition: transform 0.2s, box-shadow 0.2s;
}
.finish-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(103,194,58,0.4);
}
</style>
