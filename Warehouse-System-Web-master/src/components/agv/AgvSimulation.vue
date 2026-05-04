<template>
  <div class="agv-wrap">
    <!-- 控制栏 -->
    <div class="ctrl-bar">
      <span class="page-title">AGV 仿真监控</span>
      <div class="ctrl-btns">
        <el-button type="success"  size="small" :disabled="simRunning"  @click="startSim">启动</el-button>
        <el-button type="warning"  size="small" :disabled="!simRunning" @click="stopSim">暂停</el-button>
        <el-button type="danger"   size="small" @click="resetSim">重置</el-button>
      </div>
      <div class="conn-status" :class="connected ? 'conn-ok' : 'conn-err'">
        {{ connected ? '● 已连接' : '○ 未连接' }}
      </div>
    </div>

    <div class="main-area">
      <!-- 画布 -->
      <div class="canvas-wrap" ref="canvasWrap">
        <canvas ref="canvas" @wheel.prevent="onWheel" @mousedown="onMouseDown"
                @mousemove="onMouseMove" @mouseup="onMouseUp"></canvas>
      </div>

      <!-- 状态面板 -->
      <div class="side-panel">
        <div class="panel-section">
          <div class="section-title">AGV 状态</div>
          <div v-for="agv in agvList" :key="agv.id" class="agv-card" :class="stateClass(agv.state)">
            <div class="agv-id">AGV-{{ agv.id }}</div>
            <div class="agv-info">
              <span class="state-badge" :class="stateClass(agv.state)">{{ stateLabel(agv.state) }}</span>
              <span class="agv-detail">{{ agv.loaded ? '满载' : '空载' }}</span>
            </div>
            <div class="agv-pos">X:{{ agv.x.toFixed(1) }}m  Y:{{ agv.y.toFixed(1) }}m</div>
            <div class="agv-speed">速度: {{ agv.speed.toFixed(2) }} m/s</div>
          </div>
          <div v-if="agvList.length === 0" class="empty-tip">仿真未启动</div>
        </div>

        <div class="panel-section">
          <div class="section-title">工人状态</div>
          <div v-for="w in workerList" :key="w.id" class="worker-card">
            <div class="worker-id">工人-{{ w.id }}</div>
            <div class="worker-mode">{{ w.mode === 'SIMULATED' ? '模拟模式' : '真实模式' }}</div>
            <div class="worker-state" :class="w.state === 'STOPPED' ? 'w-stop' : 'w-walk'">
              {{ w.state === 'STOPPED' ? '拣货中' : '行走中' }}
            </div>
            <div class="worker-pos">X:{{ w.x.toFixed(1) }}m  Y:{{ w.y.toFixed(1) }}m</div>
          </div>
        </div>

        <div class="panel-section legend-section">
          <div class="section-title">图例</div>
          <div class="legend-item"><span class="leg-box shelf"></span>货架</div>
          <div class="legend-item"><span class="leg-box human-zone"></span>人工通道（禁区）</div>
          <div class="legend-item"><span class="leg-box agv-run"></span>AGV 正常行驶</div>
          <div class="legend-item"><span class="leg-box agv-cau"></span>AGV 减速</div>
          <div class="legend-item"><span class="leg-box agv-stop"></span>AGV 急停</div>
          <div class="legend-item"><span class="leg-circle worker-dot"></span>工人</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const STATE_COLORS = {
  IDLE:           '#95a5a6',
  RUNNING:        '#27ae60',
  CAUTION:        '#e6a817',
  EMERGENCY_STOP: '#e74c3c',
  LIFTING:        '#3498db',
  WAITING_LOCK:   '#9b59b6'
}

export default {
  name: 'AgvSimulation',
  data() {
    return {
      connected: false,
      simRunning: false,
      agvList: [],
      workerList: [],
      // 地图静态数据
      mapData: null,
      // 画布变换
      scale: 1,
      offsetX: 0,
      offsetY: 0,
      dragging: false,
      dragStart: null,
      // 动画帧
      animFrame: null,
      stompClient: null,
      // 静态层离屏 canvas
      staticCanvas: null
    }
  },
  mounted() {
    this.initCanvas()
    this.loadMapData()
    this.connectWs()
    window.addEventListener('resize', this.onResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.onResize)
    if (this.stompClient) this.stompClient.deactivate()
    if (this.animFrame) cancelAnimationFrame(this.animFrame)
  },
  methods: {
    // ── 初始化 ──────────────────────────────────────────────
    initCanvas() {
      const wrap = this.$refs.canvasWrap
      const canvas = this.$refs.canvas
      canvas.width  = wrap.clientWidth
      canvas.height = wrap.clientHeight
      // 初始缩放：让 80m 宽度适配画布
      this.scale = (canvas.width - 40) / 80
      this.offsetX = 20
      this.offsetY = canvas.height - 20
    },
    onResize() {
      const wrap = this.$refs.canvasWrap
      const canvas = this.$refs.canvas
      canvas.width  = wrap.clientWidth
      canvas.height = wrap.clientHeight
      this.staticCanvas = null // 强制重绘静态层
      this.drawFrame()
    },

    // ── 地图数据 ─────────────────────────────────────────────
    loadMapData() {
      this.$axios.get(this.$httpUrl + '/agv/map').then(res => {
        if (res.data.code === 200) {
          this.mapData = res.data.data
          this.buildStaticLayer()
          this.drawFrame()
        }
      })
    },

    // ── WebSocket ────────────────────────────────────────────
    connectWs() {
      const client = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8002/ws-agv'),
        reconnectDelay: 3000,
        onConnect: () => {
          this.connected = true
          client.subscribe('/topic/agv-state', msg => {
            const state = JSON.parse(msg.body)
            this.simRunning  = state.running
            this.agvList     = state.agvs    || []
            this.workerList  = state.workers || []
            this.drawFrame()
          })
        },
        onDisconnect: () => { this.connected = false }
      })
      client.activate()
      this.stompClient = client
    },

    // ── 仿真控制 ─────────────────────────────────────────────
    startSim() {
      this.$axios.post(this.$httpUrl + '/agv/start').then(() => { this.simRunning = true })
    },
    stopSim() {
      this.$axios.post(this.$httpUrl + '/agv/stop').then(() => { this.simRunning = false })
    },
    resetSim() {
      this.$axios.post(this.$httpUrl + '/agv/reset').then(() => {
        this.simRunning = false
        this.agvList = []; this.workerList = []
        this.drawFrame()
      })
    },

    // ── 渲染 ─────────────────────────────────────────────────
    buildStaticLayer() {
      if (!this.mapData) return
      const c = document.createElement('canvas')
      c.width  = this.$refs.canvas.width
      c.height = this.$refs.canvas.height
      const ctx = c.getContext('2d')
      this.drawStatic(ctx)
      this.staticCanvas = c
    },

    drawFrame() {
      const canvas = this.$refs.canvas
      if (!canvas) return
      const ctx = canvas.getContext('2d')
      ctx.clearRect(0, 0, canvas.width, canvas.height)

      // 背景
      ctx.fillStyle = '#f8f9fa'
      ctx.fillRect(0, 0, canvas.width, canvas.height)

      // 静态层
      if (this.staticCanvas) {
        ctx.drawImage(this.staticCanvas, 0, 0)
      } else if (this.mapData) {
        this.drawStatic(ctx)
      }

      // 动态层
      this.drawDynamic(ctx)
    },

    drawStatic(ctx) {
      if (!this.mapData) return
      const { shelves, humanZones, widthM, heightM } = this.mapData

      // 仓库地面
      ctx.fillStyle = '#ecf0f1'
      ctx.fillRect(...this.rect(0, 0, widthM, heightM))

      // 仓库边框
      ctx.strokeStyle = '#2c3e50'
      ctx.lineWidth = 2
      ctx.strokeRect(...this.rect(0, 0, widthM, heightM))

      // 人工通道（浅红禁区）
      ctx.fillStyle = 'rgba(231,76,60,0.12)'
      for (const z of humanZones) {
        ctx.fillRect(...this.rect(z[0], z[1], z[2] - z[0], z[3] - z[1]))
      }

      // 主干道标记（浅蓝）
      ctx.fillStyle = 'rgba(52,152,219,0.10)'
      // 水平主干道
      ctx.fillRect(...this.rect(0, 18.25, 80, 3.5))
      // 垂直主干道
      ctx.fillRect(...this.rect(38.25, 0, 3.5, 40))

      // 货架
      ctx.fillStyle = '#7f8c8d'
      ctx.strokeStyle = '#5d6d7e'
      ctx.lineWidth = 1
      for (const s of shelves) {
        const r = this.rect(s[0], s[1], s[2] - s[0], s[3] - s[1])
        ctx.fillRect(...r)
        ctx.strokeRect(...r)
      }

      // 坐标轴刻度
      this.drawGrid(ctx, widthM, heightM)
    },

    drawGrid(ctx, w, h) {
      ctx.strokeStyle = 'rgba(0,0,0,0.08)'
      ctx.lineWidth = 0.5
      ctx.fillStyle = '#888'
      ctx.font = `${Math.max(9, this.scale * 0.8)}px sans-serif`
      ctx.textAlign = 'center'
      for (let x = 0; x <= w; x += 10) {
        const [px, py] = this.toCanvas(x, 0)
        ctx.beginPath(); ctx.moveTo(px, py); ctx.lineTo(px, this.toCanvas(x, h)[1]); ctx.stroke()
        ctx.fillText(x + 'm', px, py + 12)
      }
      ctx.textAlign = 'right'
      for (let y = 0; y <= h; y += 10) {
        const [px, py] = this.toCanvas(0, y)
        ctx.beginPath(); ctx.moveTo(px, py); ctx.lineTo(this.toCanvas(w, y)[0], py); ctx.stroke()
        ctx.fillText(y + 'm', px - 4, py + 4)
      }
    },

    drawDynamic(ctx) {
      // 路径
      for (const agv of this.agvList) {
        if (agv.path && agv.path.length > 1) {
          ctx.strokeStyle = 'rgba(52,152,219,0.4)'
          ctx.lineWidth = 1
          ctx.setLineDash([4, 4])
          ctx.beginPath()
          const [sx, sy] = this.toCanvas(agv.x, agv.y)
          ctx.moveTo(sx, sy)
          for (const pt of agv.path) {
            const [px, py] = this.toCanvas(pt[0], pt[1])
            ctx.lineTo(px, py)
          }
          ctx.stroke()
          ctx.setLineDash([])
        }
      }

      // 传感器扇形
      for (const agv of this.agvList) {
        this.drawSensorArc(ctx, agv)
      }

      // AGV 本体
      for (const agv of this.agvList) {
        this.drawAgv(ctx, agv)
      }

      // 工人
      for (const w of this.workerList) {
        this.drawWorker(ctx, w)
      }
    },

    drawSensorArc(ctx, agv) {
      const [cx, cy] = this.toCanvas(agv.x, agv.y)
      const r = 5 * this.scale
      const angleRad = (agv.angle * Math.PI) / 180
      const halfFov  = (105 * Math.PI) / 180
      ctx.beginPath()
      ctx.moveTo(cx, cy)
      // canvas y 轴向下，需翻转 y
      ctx.arc(cx, cy, r, -angleRad - halfFov, -angleRad + halfFov)
      ctx.closePath()
      const color = agv.state === 'EMERGENCY_STOP' ? 'rgba(231,76,60,0.15)'
                  : agv.state === 'CAUTION'         ? 'rgba(230,168,23,0.15)'
                  : 'rgba(39,174,96,0.10)'
      ctx.fillStyle = color
      ctx.fill()
    },

    drawAgv(ctx, agv) {
      const [cx, cy] = this.toCanvas(agv.x, agv.y)
      const w = 1.2 * this.scale
      const h = 0.8 * this.scale
      const color = STATE_COLORS[agv.state] || '#95a5a6'

      ctx.save()
      ctx.translate(cx, cy)
      ctx.rotate(-agv.angle * Math.PI / 180)

      // 车身
      ctx.fillStyle = color
      ctx.strokeStyle = '#2c3e50'
      ctx.lineWidth = 1
      ctx.fillRect(-w / 2, -h / 2, w, h)
      ctx.strokeRect(-w / 2, -h / 2, w, h)

      // 朝向箭头
      ctx.fillStyle = '#fff'
      ctx.beginPath()
      ctx.moveTo(w / 2, 0)
      ctx.lineTo(w / 2 - 4, -3)
      ctx.lineTo(w / 2 - 4, 3)
      ctx.closePath()
      ctx.fill()

      // ID 标签
      ctx.rotate(agv.angle * Math.PI / 180)
      ctx.fillStyle = '#2c3e50'
      ctx.font = `bold ${Math.max(8, this.scale * 0.7)}px sans-serif`
      ctx.textAlign = 'center'
      ctx.fillText(agv.id, 0, -h / 2 - 4)

      ctx.restore()
    },

    drawWorker(ctx, w) {
      const [cx, cy] = this.toCanvas(w.x, w.y)
      const r = 0.3 * this.scale
      ctx.beginPath()
      ctx.arc(cx, cy, Math.max(4, r), 0, Math.PI * 2)
      ctx.fillStyle = w.state === 'STOPPED' ? '#e74c3c' : '#f39c12'
      ctx.fill()
      ctx.strokeStyle = '#c0392b'
      ctx.lineWidth = 1
      ctx.stroke()

      ctx.fillStyle = '#2c3e50'
      ctx.font = `${Math.max(8, this.scale * 0.6)}px sans-serif`
      ctx.textAlign = 'center'
      ctx.fillText('W' + w.id, cx, cy - Math.max(4, r) - 3)
    },

    // ── 坐标转换 ─────────────────────────────────────────────
    toCanvas(mx, my) {
      // 仓库坐标 y 向上，canvas y 向下
      return [
        this.offsetX + mx * this.scale,
        this.offsetY - my * this.scale
      ]
    },
    rect(x, y, w, h) {
      const [cx, cy] = this.toCanvas(x, y + h)
      return [cx, cy, w * this.scale, h * this.scale]
    },

    // ── 缩放/平移 ────────────────────────────────────────────
    onWheel(e) {
      const factor = e.deltaY < 0 ? 1.1 : 0.9
      const canvas = this.$refs.canvas
      const rect   = canvas.getBoundingClientRect()
      const mx = e.clientX - rect.left
      const my = e.clientY - rect.top
      this.offsetX = mx - (mx - this.offsetX) * factor
      this.offsetY = my - (my - this.offsetY) * factor
      this.scale  *= factor
      this.staticCanvas = null
      this.drawFrame()
    },
    onMouseDown(e) { this.dragging = true; this.dragStart = { x: e.clientX, y: e.clientY } },
    onMouseMove(e) {
      if (!this.dragging) return
      this.offsetX += e.clientX - this.dragStart.x
      this.offsetY += e.clientY - this.dragStart.y
      this.dragStart = { x: e.clientX, y: e.clientY }
      this.staticCanvas = null
      this.drawFrame()
    },
    onMouseUp() { this.dragging = false },

    // ── 辅助 ─────────────────────────────────────────────────
    stateLabel(s) {
      return { IDLE:'待机', RUNNING:'行驶', CAUTION:'减速', EMERGENCY_STOP:'急停',
               LIFTING:'举升', WAITING_LOCK:'等待' }[s] || s
    },
    stateClass(s) {
      return { IDLE:'s-idle', RUNNING:'s-run', CAUTION:'s-cau',
               EMERGENCY_STOP:'s-stop', LIFTING:'s-lift', WAITING_LOCK:'s-wait' }[s] || ''
    }
  }
}
</script>

<style scoped>
.agv-wrap { height: 100%; display: flex; flex-direction: column; background: #f0f4fa; }

.ctrl-bar {
  height: 52px; background: #fff; border-bottom: 1px solid #e0e6f0;
  display: flex; align-items: center; gap: 16px; padding: 0 20px;
  flex-shrink: 0; box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.page-title { font-size: 16px; font-weight: 700; color: #2c3e50; margin-right: 8px; }
.ctrl-btns  { display: flex; gap: 8px; }
.conn-status { margin-left: auto; font-size: 12px; font-weight: 600; padding: 3px 10px; border-radius: 10px; }
.conn-ok  { color: #27ae60; background: rgba(39,174,96,0.1); }
.conn-err { color: #e74c3c; background: rgba(231,76,60,0.1); }

.main-area { flex: 1; display: flex; overflow: hidden; }

.canvas-wrap {
  flex: 1; position: relative; overflow: hidden;
  background: #fff; border-right: 1px solid #e0e6f0;
}
.canvas-wrap canvas { display: block; cursor: grab; }
.canvas-wrap canvas:active { cursor: grabbing; }

.side-panel {
  width: 220px; overflow-y: auto; padding: 12px;
  background: #fff; flex-shrink: 0;
}
.panel-section { margin-bottom: 16px; }
.section-title {
  font-size: 12px; font-weight: 700; color: #7f8c8d;
  text-transform: uppercase; letter-spacing: 0.5px;
  margin-bottom: 8px; padding-bottom: 4px;
  border-bottom: 1px solid #ecf0f1;
}

.agv-card {
  background: #f8f9fa; border-radius: 8px; padding: 8px 10px;
  margin-bottom: 8px; border-left: 3px solid #95a5a6;
}
.agv-card.s-run  { border-left-color: #27ae60; }
.agv-card.s-cau  { border-left-color: #e6a817; }
.agv-card.s-stop { border-left-color: #e74c3c; }
.agv-card.s-lift { border-left-color: #3498db; }
.agv-card.s-wait { border-left-color: #9b59b6; }

.agv-id    { font-size: 13px; font-weight: 700; color: #2c3e50; }
.agv-info  { display: flex; align-items: center; gap: 6px; margin: 3px 0; }
.state-badge {
  font-size: 10px; padding: 1px 6px; border-radius: 8px;
  background: #ecf0f1; color: #7f8c8d;
}
.state-badge.s-run  { background: rgba(39,174,96,0.15);  color: #27ae60; }
.state-badge.s-cau  { background: rgba(230,168,23,0.15); color: #e6a817; }
.state-badge.s-stop { background: rgba(231,76,60,0.15);  color: #e74c3c; }
.state-badge.s-lift { background: rgba(52,152,219,0.15); color: #3498db; }
.state-badge.s-wait { background: rgba(155,89,182,0.15); color: #9b59b6; }
.agv-detail { font-size: 11px; color: #95a5a6; }
.agv-pos, .agv-speed { font-size: 11px; color: #7f8c8d; }

.worker-card {
  background: #f8f9fa; border-radius: 8px; padding: 8px 10px;
  margin-bottom: 8px; border-left: 3px solid #f39c12;
}
.worker-id    { font-size: 13px; font-weight: 700; color: #2c3e50; }
.worker-mode  { font-size: 10px; color: #95a5a6; }
.worker-state { font-size: 11px; font-weight: 600; margin: 2px 0; }
.w-walk { color: #27ae60; }
.w-stop { color: #e74c3c; }
.worker-pos   { font-size: 11px; color: #7f8c8d; }

.empty-tip { font-size: 12px; color: #bdc3c7; text-align: center; padding: 8px 0; }

.legend-section { }
.legend-item { display: flex; align-items: center; gap: 8px; font-size: 12px; color: #555; margin-bottom: 5px; }
.leg-box    { width: 14px; height: 14px; border-radius: 2px; flex-shrink: 0; }
.leg-circle { width: 12px; height: 12px; border-radius: 50%; flex-shrink: 0; }
.shelf      { background: #7f8c8d; }
.human-zone { background: rgba(231,76,60,0.25); border: 1px solid #e74c3c; }
.agv-run    { background: #27ae60; }
.agv-cau    { background: #e6a817; }
.agv-stop   { background: #e74c3c; }
.worker-dot { background: #f39c12; }
</style>
