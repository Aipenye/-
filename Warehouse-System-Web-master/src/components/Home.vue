<template>
  <div class="home-wrap">
    <template v-if="user.roleId === 2">
      <div class="profile-card">
        <h1 class="welcome">欢迎你！{{ user.name }}</h1>
        <el-descriptions title="个人中心" :column="2" size="40" border>
          <el-descriptions-item>
            <template slot="label"><i class="el-icon-s-custom"></i> 账号</template>{{ user.no }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template slot="label"><i class="el-icon-mobile-phone"></i> 电话</template>{{ user.phone }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template slot="label"><i class="el-icon-location-outline"></i> 性别</template>
            <el-tag :type="user.sex==1?'primary':'danger'" disable-transitions>{{ user.sex==1?'男':'女' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item>
            <template slot="label"><i class="el-icon-tickets"></i> 角色</template>
            <el-tag type="success" disable-transitions>工人</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <DateUtils></DateUtils>
      </div>
    </template>

    <template v-else>
      <div class="dashboard">
        <div class="dash-header">
          <span class="dash-title">📊 仓库数据大屏</span>
          <span class="dash-sub">近5日数据概览 · 实时更新</span>
        </div>

        <div class="chart-grid">
          <!-- 每日入库 -->
          <div class="chart-card">
            <div class="card-title-row">
              <span><i class="el-icon-upload2"></i> 每日入库数量</span>
              <el-button size="mini" type="text" @click="openDetail('in')">查看明细</el-button>
            </div>
            <div ref="chartIn" class="chart-box"></div>
          </div>
          <!-- 每日出库 -->
          <div class="chart-card">
            <div class="card-title-row">
              <span><i class="el-icon-download"></i> 每日出库数量</span>
              <el-button size="mini" type="text" @click="openDetail('out')">查看明细</el-button>
            </div>
            <div ref="chartOut" class="chart-box"></div>
          </div>
          <!-- 完成订单 -->
          <div class="chart-card">
            <div class="card-title-row">
              <span><i class="el-icon-s-order"></i> 完成订单数</span>
              <el-button size="mini" type="text" @click="openDetail('order')">查看明细</el-button>
            </div>
            <div ref="chartOrder" class="chart-box"></div>
          </div>
          <!-- 货物类型分布 -->
          <div class="chart-card">
            <div class="card-title-row">
              <span><i class="el-icon-pie-chart"></i> 货物类型分布</span>
            </div>
            <div ref="chartPie" class="chart-box"></div>
          </div>
          <!-- 仓库使用情况 -->
          <div class="chart-card">
            <div class="card-title-row">
              <span><i class="el-icon-office-building"></i> 仓库使用情况（占用率%）</span>
              <el-button size="mini" type="text" @click="openDetail('storage')">查看明细</el-button>
            </div>
            <div ref="chartStorage" class="chart-box"></div>
          </div>
        </div>
      </div>
    </template>

    <!-- 明细弹窗 -->
    <el-dialog :title="detailTitle" :visible.sync="detailVisible" width="600px">
      <!-- 入库/出库/订单：近30天文字列表 -->
      <template v-if="detailType !== 'storage'">
        <div class="detail-list">
          <div v-for="item in detailData" :key="item.date" class="detail-row">
            <span class="detail-date">{{ item.date }}</span>
            <span class="detail-val">{{ item.value }}</span>
          </div>
          <div v-if="detailData.length === 0" style="text-align:center;color:#bbb;padding:20px">暂无数据</div>
        </div>
      </template>
      <!-- 仓库使用：当前快照 -->
      <template v-else>
        <el-table :data="detailData" size="small" stripe>
          <el-table-column prop="name" label="仓库名"/>
          <el-table-column prop="capacity" label="总容量" width="90"/>
          <el-table-column prop="used" label="已使用" width="90"/>
          <el-table-column label="占用率" width="100">
            <template slot-scope="scope">{{ scope.row.rate }}%</template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import DateUtils from './DateUtils';
import * as echarts from 'echarts';

export default {
  name: 'Home',
  components: { DateUtils },
  data() {
    return {
      user: {},
      detailVisible: false,
      detailType: '',
      detailTitle: '',
      detailData: []
    };
  },
  methods: {
    init() {
      this.user = JSON.parse(sessionStorage.getItem('CurUser'));
      if (this.user.roleId !== 2) {
        this.$nextTick(() => this.loadDashboard());
      }
    },
    loadDashboard() {
      this.$axios.get(this.$httpUrl + '/dashboard/stats').then(res => res.data).then(res => {
        if (res.code === 200) {
          const d = res.data;
          this.renderLine(this.$refs.chartIn, d.dates, d.inData, '入库数量', '#5470c6');
          this.renderLine(this.$refs.chartOut, d.dates, d.outData, '出库数量', '#ee6666');
          this.renderLine(this.$refs.chartOrder, d.dates, d.orderData, '订单数', '#91cc75');
          this.renderPie(this.$refs.chartPie, d.goodsTypeData);
          this.renderStorageBar(this.$refs.chartStorage, d.storageUsage || []);
        }
      });
    },
    openDetail(type) {
      this.detailType = type;
      const titles = { in: '近30天每日入库', out: '近30天每日出库', order: '近30天每日订单数', storage: '仓库使用情况（当前快照）' };
      this.detailTitle = titles[type];
      this.$axios.get(this.$httpUrl + '/dashboard/daily', { params: { type } }).then(res => {
        if (res.data.code === 200) {
          this.detailData = res.data.data;
          this.detailVisible = true;
        }
      });
    },
    renderLine(el, dates, data, name, color) {
      const chart = echarts.init(el);
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 45, right: 20, top: 20, bottom: 35 },
        xAxis: { type: 'category', data: dates, axisLabel: { color: '#666' } },
        yAxis: { type: 'value', minInterval: 1, axisLabel: { color: '#666' } },
        series: [{ name, type: 'line', data, smooth: true, symbol: 'circle', symbolSize: 8,
          lineStyle: { color, width: 3 }, itemStyle: { color }, areaStyle: { color: color + '22' } }]
      });
    },
    renderPie(el, data) {
      const chart = echarts.init(el);
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { bottom: 0, type: 'scroll' },
        series: [{ type: 'pie', radius: ['35%', '65%'], center: ['50%', '45%'],
          data: (data || []).map(d => ({ name: d.name, value: d.value })),
          label: { formatter: '{b}\n{d}%' },
          emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.3)' } } }]
      });
    },
    renderStorageBar(el, data) {
      if (!el || !data || data.length === 0) return;
      const chart = echarts.init(el);
      chart.setOption({
        tooltip: { trigger: 'axis', formatter: params => `${params[0].name}<br/>占用率：${params[0].value}%` },
        grid: { left: 50, right: 20, top: 20, bottom: 60 },
        xAxis: { type: 'category', data: data.map(d => d.name), axisLabel: { color: '#666', rotate: 30, interval: 0 } },
        yAxis: { type: 'value', max: 100, axisLabel: { color: '#666', formatter: '{value}%' } },
        series: [{ type: 'bar', data: data.map(d => d.rate), barMaxWidth: 40,
          itemStyle: { color: params => {
            const v = params.value;
            return v >= 90 ? '#e74c3c' : v >= 70 ? '#e6a817' : '#27ae60';
          }},
          label: { show: true, position: 'top', formatter: '{c}%', color: '#555' } }]
      });
    }
  },
  created() { this.init(); }
};
</script>

<style scoped>
.home-wrap {
  height: 100%; background: #f4f6fa; padding: 16px;
  box-sizing: border-box; overflow-y: auto;
}
.profile-card { text-align: center; }
.welcome { font-size: 36px; margin-bottom: 20px; color: #333; }
.el-descriptions { width: 90%; margin: 0 auto; }

.dashboard { min-height: 100%; }
.dash-header {
  display: flex; align-items: baseline; gap: 12px;
  margin-bottom: 16px; padding-bottom: 12px;
  border-bottom: 2px solid #e0e6f0;
}
.dash-title { font-size: 20px; font-weight: 700; color: #2c3e50; }
.dash-sub { font-size: 13px; color: #95a5a6; }

.chart-grid { display: grid; grid-template-columns: 1fr; gap: 20px; }
.chart-card {
  background: #fff; border-radius: 12px; padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.07);
}
.card-title-row {
  display: flex; justify-content: space-between; align-items: center;
  font-size: 15px; font-weight: 600; color: #555; margin-bottom: 12px;
}
.card-title-row i { margin-right: 6px; }
.chart-box { width: 100%; height: 360px; }

.detail-list { max-height: 420px; overflow-y: auto; }
.detail-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 12px; border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}
.detail-row:last-child { border-bottom: none; }
.detail-date { color: #666; }
.detail-val { font-weight: 600; color: #2c3e50; }
</style>
