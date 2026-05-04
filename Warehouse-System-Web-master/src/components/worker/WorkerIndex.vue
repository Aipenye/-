<template>
  <div class="worker-layout">
    <div class="worker-header">
      <div class="header-left">
        <span class="logo">🏭</span>
        <div>
          <div class="sys-name">仓库管理系统</div>
          <div class="sys-sub">工人工作台</div>
        </div>
      </div>
      <div class="header-center">
        <div class="status-area">
          <span class="status-label" :class="statusClass">{{ statusText }}</span>
          <span v-if="workStatus === 3" class="rest-timer">{{ restTimeDisplay }}</span>
        </div>
        <div class="status-btns">
          <el-button v-if="workStatus === 0" type="success" size="small" round @click="clockIn">上班</el-button>
          <template v-if="workStatus === 1">
            <el-button type="warning" size="small" round @click="startRest">开始休息</el-button>
            <el-button type="info" size="small" round @click="clockOut">下班</el-button>
          </template>
          <el-button v-if="workStatus === 2" type="info" size="small" round @click="clockOut">下班</el-button>
          <el-button v-if="workStatus === 3" type="primary" size="small" round @click="endRest">结束休息</el-button>
        </div>
      </div>
      <div class="header-right">
        <div class="user-avatar">{{ user.name ? user.name[0] : 'W' }}</div>
        <div class="user-info">
          <div class="user-name">{{ user.name }}</div>
          <div class="user-role">工人</div>
        </div>
        <el-button type="text" class="logout-btn" @click="logout">
          <i class="el-icon-switch-button"/> 退出
        </el-button>
      </div>
    </div>
    <div class="worker-main">
      <router-view @orderFinished="onOrderFinished"/>
    </div>

    <el-dialog title="休息提醒" :visible.sync="restDialogVisible" width="360px"
               :close-on-click-modal="false" :show-close="false">
      <div style="text-align:center;font-size:15px;padding:10px 0">
        <i class="el-icon-alarm-clock" style="font-size:36px;color:#e6a817;display:block;margin-bottom:12px"/>
        您已连续休息 30 分钟
        <div style="font-size:13px;color:#999;margin-top:8px">
          今日已休息 {{ todayRestMinutes }} 分钟 / 共 120 分钟
        </div>
      </div>
      <span slot="footer">
        <el-button @click="endRest">结束休息</el-button>
        <el-button type="primary" @click="continueRest">继续休息</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'WorkerIndex',
  data() {
    return {
      user: JSON.parse(sessionStorage.getItem('CurUser')) || {},
      workStatus: 0,
      todayRestMinutes: 0,
      restSeconds: 0,
      restTimer: null,
      restDialogVisible: false
    }
  },
  computed: {
    statusText() {
      return ['未上班', '空闲', '工作中', '休息中'][this.workStatus] || '未知'
    },
    statusClass() {
      return ['st-off', 'st-idle', 'st-working', 'st-resting'][this.workStatus] || ''
    },
    restTimeDisplay() {
      const m = Math.floor(this.restSeconds / 60)
      const s = this.restSeconds % 60
      return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
    }
  },
  created() {
    this.fetchStatus()
  },
  beforeDestroy() {
    this.clearRestTimer()
  },
  methods: {
    fetchStatus() {
      this.$axios.get(this.$httpUrl + '/user/workerStatus', { params: { id: this.user.id } })
        .then(res => {
          if (res.data.code === 200) {
            this.workStatus = res.data.data.workStatus
            this.todayRestMinutes = res.data.data.todayRestMinutes || 0
          }
        })
    },
    setStatus(status) {
      return this.$axios.post(this.$httpUrl + '/user/updateStatus', { id: this.user.id, workStatus: status })
    },
    clockIn() {
      this.setStatus(1).then(res => {
        if (res.data.code === 200) {
          this.workStatus = 1
          this.$message.success('已上班，状态：空闲')
        } else {
          this.$message.error('操作失败')
        }
      })
    },
    clockOut() {
      this.$confirm('确定下班？当前未完成工单将退回待分配池。', '提示', { type: 'warning' })
        .then(() => {
          this.clearRestTimer()
          this.setStatus(0).then(res => {
            if (res.data.code === 200) {
              this.workStatus = 0
              this.$message.success('已下班')
            } else {
              this.$message.error('操作失败')
            }
          })
        }).catch(() => {})
    },
    startRest() {
      const remaining = 120 - this.todayRestMinutes
      if (remaining <= 0) {
        this.$message.warning('今日休息时间已用完')
        return
      }
      this.setStatus(3).then(res => {
        if (res.data.code === 200) {
          this.workStatus = 3
          this.restSeconds = Math.min(30, remaining) * 60
          this.startRestTimer()
          this.$message.success('休息开始，剩余可休息 ' + remaining + ' 分钟')
        } else {
          this.$message.error('操作失败')
        }
      })
    },
    endRest() {
      this.restDialogVisible = false
      this.clearRestTimer()
      this.setStatus(1).then(res => {
        if (res.data.code === 200) {
          this.workStatus = 1
          this.$message.success('休息结束，状态：空闲')
        } else {
          this.$message.error('操作失败')
        }
      })
    },
    continueRest() {
      const remaining = 120 - this.todayRestMinutes
      if (remaining <= 0) {
        this.$message.warning('今日休息时间已用完，强制结束休息')
        this.endRest()
        return
      }
      this.restDialogVisible = false
      this.restSeconds = Math.min(30, remaining) * 60
      this.startRestTimer()
    },
    startRestTimer() {
      this.clearRestTimer()
      this.restTimer = setInterval(() => {
        this.restSeconds--
        if (this.restSeconds <= 0) {
          this.clearRestTimer()
          this.$axios.get(this.$httpUrl + '/user/addRestMinutes', {
            params: { id: this.user.id, minutes: 30 }
          }).then(res => {
            if (res.data.code === 200) {
              this.todayRestMinutes = res.data.data || (this.todayRestMinutes + 30)
            }
          })
          this.restDialogVisible = true
        }
      }, 1000)
    },
    clearRestTimer() {
      if (this.restTimer) {
        clearInterval(this.restTimer)
        this.restTimer = null
      }
    },
    onOrderFinished() {
      this.fetchStatus()
    },
    logout() {
      this.$confirm('确定退出登录？', '提示', { type: 'warning' }).then(() => {
        this.clearRestTimer()
        sessionStorage.clear()
        this.$router.push('/')
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.worker-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f0f4fa;
}

.worker-header {
  height: 64px;
  background: linear-gradient(135deg, #1a2a4a 0%, #2c4a7c 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.25);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.logo { font-size: 28px; }
.sys-name { font-size: 16px; font-weight: 700; color: #fff; letter-spacing: 1px; }
.sys-sub { font-size: 11px; color: rgba(255,255,255,0.55); margin-top: 1px; }

.header-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.status-area {
  display: flex;
  align-items: center;
  gap: 10px;
}
.status-label {
  font-size: 13px;
  font-weight: 700;
  padding: 3px 12px;
  border-radius: 12px;
}
.st-off { background: rgba(255,255,255,0.15); color: rgba(255,255,255,0.7); }
.st-idle { background: rgba(39,174,96,0.25); color: #67c23a; }
.st-working { background: rgba(52,152,219,0.25); color: #79bbff; }
.st-resting { background: rgba(230,168,23,0.25); color: #e6a817; }
.rest-timer { font-size: 18px; font-weight: 700; color: #e6a817; font-variant-numeric: tabular-nums; }
.status-btns { display: flex; gap: 8px; }

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: #fff; font-size: 15px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 2px 6px rgba(52,152,219,0.4);
}
.user-info { line-height: 1.3; }
.user-name { font-size: 13px; color: #fff; font-weight: 600; }
.user-role { font-size: 11px; color: rgba(255,255,255,0.5); }

.logout-btn {
  color: rgba(255,255,255,0.7) !important;
  font-size: 13px;
  margin-left: 8px;
  padding: 6px 12px;
  border-radius: 20px;
  border: 1px solid rgba(255,255,255,0.2) !important;
  transition: all 0.25s;
}
.logout-btn:hover {
  color: #fff !important;
  background: rgba(255,255,255,0.12) !important;
  transform: scale(1.05);
}

.worker-main {
  flex: 1;
  overflow-y: auto;
  padding: 24px 28px;
}
</style>
