<template>
  <div>
    <div class="toolbar">
      <el-select v-model="filterStatus" placeholder="全部状态" clearable size="small" style="width:120px" @change="load">
        <el-option label="待处理" :value="0"/>
        <el-option label="已完成" :value="1"/>
      </el-select>
      <el-button type="primary" size="small" icon="el-icon-plus" @click="openAdd" class="action-btn">派发工单</el-button>
      <span class="total-tip">共 {{ tableData.length }} 条</span>
    </div>

    <div class="table-card">
      <el-table :data="tableData" style="width:100%"
        :header-cell-style="{background:'#f0f4fa',color:'#2c3e50',fontWeight:'600'}"
        row-class-name="table-row">
        <el-table-column prop="id" label="ID" width="60"/>
        <el-table-column prop="goodsName" label="货物"/>
        <el-table-column prop="storageName" label="目标货架"/>
        <el-table-column prop="count" label="数量" width="70"/>
        <el-table-column label="类型" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.type===1?'warning':'success'" size="small" disable-transitions>
              {{ scope.row.type===1 ? '出库' : '入库' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="指派工人">
          <template slot-scope="scope">
            <span v-if="scope.row.workerName">{{ scope.row.workerName }}</span>
            <el-tag v-else type="info" size="small" disable-transitions>待分配</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="adminName" label="派发人"/>
        <el-table-column label="截止时间" width="155">
          <template slot-scope="scope">{{ scope.row.deadline | fmtTime }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template slot-scope="scope">
            <span class="status-dot" :class="scope.row.status===0?'dot-pending':'dot-done'"/>
            <span :style="{color: scope.row.status===0?'#e6a817':'#27ae60', fontWeight:600}">
              {{ scope.row.status===0?'待处理':'已完成' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注"/>
        <el-table-column label="操作" width="80">
          <template slot-scope="scope">
            <el-button type="danger" size="mini" plain @click="del(scope.row.id)" class="del-btn">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 派发工单弹窗 -->
    <el-dialog title="派发工单" :visible.sync="dialogVisible" width="480px" @close="resetForm" custom-class="wms-dialog">
      <el-form :model="form" :rules="rules" ref="form" label-width="90px">
        <el-form-item label="货物" prop="goodsId">
          <el-select v-model="form.goodsId" placeholder="选择货物" style="width:100%">
            <el-option v-for="g in goodsList" :key="g.id" :label="g.name" :value="g.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="目标货架" prop="storageId">
          <el-select v-model="form.storageId" placeholder="选择货架" style="width:100%">
            <el-option v-for="s in storageList" :key="s.id" :label="s.name" :value="s.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="数量" prop="count">
          <el-input-number v-model="form.count" :min="1" style="width:100%"/>
        </el-form-item>
        <el-form-item label="工单类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="0">入库</el-radio>
            <el-radio :label="1">出库</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker v-model="form.deadline" type="datetime" placeholder="选择截止时间"
                          value-format="yyyy-MM-ddTHH:mm:ss" style="width:100%"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填"/>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submit">确认派发</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'WorkOrderManage',
  filters: {
    fmtTime(v) {
      if (!v) return '-'
      return v.replace('T', ' ').substring(0, 16)
    }
  },
  data() {
    return {
      tableData: [],
      filterStatus: null,
      dialogVisible: false,
      goodsList: [],
      storageList: [],
      form: { goodsId: null, storageId: null, count: 1, type: 0, deadline: null, remark: '' },
      rules: {
        goodsId:   [{ required: true, message: '请选择货物', trigger: 'change' }],
        storageId: [{ required: true, message: '请选择货架', trigger: 'change' }],
        count:     [{ required: true, message: '请填写数量', trigger: 'blur' }],
        type:      [{ required: true, message: '请选择工单类型', trigger: 'change' }],
        deadline:  [{ required: true, message: '请选择截止时间', trigger: 'change' }],
      }
    }
  },
  created() {
    this.load()
    this.loadOptions()
  },
  methods: {
    load() {
      const params = {}
      if (this.filterStatus !== null && this.filterStatus !== '') params.status = this.filterStatus
      this.$axios.get(this.$httpUrl + '/workorder/list', { params }).then(res => {
        if (res.data.code === 200) this.tableData = res.data.data
      })
    },
    loadOptions() {
      this.$axios.get(this.$httpUrl + '/goods/list').then(r => { this.goodsList = r.data.data || r.data })
      this.$axios.get(this.$httpUrl + '/storage/list').then(r => { this.storageList = r.data.data || r.data })
    },
    openAdd() { this.dialogVisible = true },
    resetForm() {
      this.form = { goodsId: null, storageId: null, count: 1, type: 0, deadline: null, remark: '' }
      this.$refs.form && this.$refs.form.clearValidate()
    },
    submit() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const curUser = JSON.parse(sessionStorage.getItem('CurUser'))
        this.$axios.post(this.$httpUrl + '/workorder/save', { ...this.form, adminId: curUser.id }).then(res => {
          if (res.data.code === 200) {
            this.$message.success(res.data.msg || '工单派发成功')
            this.dialogVisible = false
            this.load()
          } else {
            this.$message.error(res.data.msg || '派发失败')
          }
        })
      })
    },
    del(id) {
      this.$confirm('确认删除该工单？', '提示', { type: 'warning' }).then(() => {
        this.$axios.get(this.$httpUrl + '/workorder/del', { params: { id } }).then(res => {
          if (res.data.code === 200) {
            this.$message.success('删除成功')
            this.load()
          }
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  border-radius: 12px;
  padding: 14px 18px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.total-tip { font-size: 13px; color: #999; margin-left: auto; }
.action-btn { border-radius: 20px; padding: 7px 18px; font-weight: 600; }
.table-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.status-dot {
  display: inline-block;
  width: 7px; height: 7px;
  border-radius: 50%;
  margin-right: 5px;
  vertical-align: middle;
}
.dot-pending { background: #e6a817; }
.dot-done { background: #27ae60; }
.del-btn { border-radius: 14px; }
.del-btn:hover { transform: scale(1.08); }
</style>
