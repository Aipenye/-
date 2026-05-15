<template>
    <div>
        <div class="toolbar">
            <el-input v-model="name" placeholder="请输入仓库名" prefix-icon="el-icon-search" size="small" style="width:180px"
                      @keyup.enter.native="loadPost"/>
            <el-button type="primary" size="small" icon="el-icon-search" @click="loadPost" class="round-btn">查询</el-button>
            <el-button size="small" icon="el-icon-refresh" @click="resetParam" class="round-btn">重置</el-button>
            <el-button type="success" size="small" icon="el-icon-plus" @click="add" class="round-btn" style="margin-left:auto">新增仓库</el-button>
        </div>
        <div class="table-card">
        <el-table :data="tableData"
                  :header-cell-style="{ background: '#f0f4fa', color: '#2c3e50', fontWeight:'600' }"
                  stripe>
            <el-table-column prop="id" label="ID" width="60"/>
            <el-table-column prop="name" label="仓库名" width="160"/>
            <el-table-column prop="remark" label="备注" width="160"/>
            <el-table-column label="总容量(cm³)" width="140">
                <template slot-scope="scope">
                    <span v-if="totalVolume(scope.row) > 0">{{ totalVolume(scope.row).toLocaleString() }}</span>
                    <span v-else style="color:#bbb">未设置</span>
                </template>
            </el-table-column>
            <el-table-column label="已使用(cm³)" width="140">
                <template slot-scope="scope">
                    {{ Math.round(scope.row.usedVolume || 0).toLocaleString() }}
                </template>
            </el-table-column>
            <el-table-column label="使用率" width="180">
                <template slot-scope="scope">
                    <template v-if="totalVolume(scope.row) > 0">
                        <el-progress
                            :percentage="usagePercent(scope.row)"
                            :color="usageColor(scope.row)"
                            :stroke-width="10"
                            style="width:160px"/>
                    </template>
                    <span v-else style="color:#bbb">未设置</span>
                </template>
            </el-table-column>
            <el-table-column prop="operate" label="操作" width="310">
                <template slot-scope="scope">
                    <el-button size="small" type="primary" plain @click="mod(scope.row)" class="op-btn">编辑</el-button>
                    <el-button size="small" type="warning" plain @click="visualize(scope.row)" class="op-btn">3D可视化</el-button>
                    <el-button size="small" type="success" plain @click="optimize(scope.row)" class="op-btn">装箱优化</el-button>
                    <el-popconfirm title="确定删除吗？" @confirm="del(scope.row.id)" style="margin-left: 8px;">
                        <el-button slot="reference" size="small" type="danger" plain class="op-btn">删除</el-button>
                    </el-popconfirm>
                </template>
            </el-table-column>
        </el-table>
        <div class="pagination-wrap">
        <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="pageNum"
                :page-sizes="[5, 10, 20, 30]"
                :page-size="pageSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"/>
        </div>
        </div>

        <el-dialog :title="form.id ? '编辑仓库' : '新增仓库'" :visible.sync="centerDialogVisible" width="30%" center>
            <el-form ref="form" :rules="rules" :model="form" label-width="80px">
                <el-form-item label="仓库名" prop="name">
                    <el-col :span="20"><el-input v-model="form.name"></el-input></el-col>
                </el-form-item>
                <el-form-item v-if="form.id" label="长(cm)" prop="length">
                    <el-col :span="20"><el-input v-model.number="form.length" placeholder="仓库长度"></el-input></el-col>
                </el-form-item>
                <el-form-item v-if="form.id" label="高(cm)" prop="height">
                    <el-col :span="20"><el-input v-model.number="form.height" placeholder="仓库高度"></el-input></el-col>
                </el-form-item>
                <el-form-item v-if="form.id" label="宽(cm)" prop="width">
                    <el-col :span="20"><el-input v-model.number="form.width" placeholder="仓库宽度"></el-input></el-col>
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-col :span="20"><el-input type="textarea" v-model="form.remark"></el-input></el-col>
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="centerDialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="save">确 定</el-button>
            </span>
        </el-dialog>

        <!-- 装箱优化弹窗 -->
        <el-dialog
            title="装箱优化 3D 可视化"
            :visible.sync="optimizeDialogVisible"
            width="90%"
            top="3vh"
            :before-close="closeOptimizeDialog">
            <div v-if="optimizeLoading" style="text-align:center;padding:40px;">
                <i class="el-icon-loading" style="font-size:32px;color:#409EFF;"></i>
                <p style="margin-top:12px;color:#666;">正在加载优化界面...</p>
            </div>
            <iframe
                v-show="!optimizeLoading"
                ref="optimizeFrame"
                src="http://localhost:5173"
                width="100%"
                height="680px"
                frameborder="0"
                @load="onIframeLoad"
                style="border-radius:8px;"/>
        </el-dialog>
    </div>
</template>

<script>
    export default {
        name: "StorageManage",
        data() {
            return {
                tableData: [],
                pageSize: 10,
                pageNum: 1,
                total: 0,
                name: '',
                centerDialogVisible: false,
                form: { id: '', name: '', remark: '', capacity: 100, length: 500, height: 300, width: 300 },
                rules: {
                    name: [{ required: true, message: '请输入仓库名', trigger: 'blur' }],
                    length: [
                        { required: true, message: '请输入仓库长度', trigger: 'blur' },
                        { type: 'number', min: 1, message: '长度必须大于0', trigger: 'blur' }
                    ],
                    height: [
                        { required: true, message: '请输入仓库高度', trigger: 'blur' },
                        { type: 'number', min: 1, message: '高度必须大于0', trigger: 'blur' }
                    ],
                    width: [
                        { required: true, message: '请输入仓库宽度', trigger: 'blur' },
                        { type: 'number', min: 1, message: '宽度必须大于0', trigger: 'blur' }
                    ]
                },
                optimizeDialogVisible: false,
                optimizeLoading: false,
                currentStorage: null,
                pendingPayload: null
            }
        },
        methods: {
            visualize(row) {
                const dataUrl = encodeURIComponent(this.$httpUrl + '/visualization/storage/' + row.id)
                window.open('http://localhost:5173/?dataUrl=' + dataUrl + '&strategy=skjolber', '_blank')
            },
            optimize(row) {
                this.currentStorage = row
                this.optimizeLoading = true
                this.pendingPayload = null
                this.$axios.get(this.$httpUrl + '/optimization/goodsForOptimize?storageId=' + row.id)
                    .then(res => res.data)
                    .then(res => {
                        if (res.code == 200) {
                            this.pendingPayload = res.data
                            this.optimizeDialogVisible = true
                        } else {
                            this.$message({ message: res.msg || '获取货物数据失败', type: 'error' })
                            this.optimizeLoading = false
                        }
                    })
                    .catch(() => {
                        this.$message({ message: '请求失败，请检查后端服务', type: 'error' })
                        this.optimizeLoading = false
                    })
            },
            onIframeLoad() {
                if (this.pendingPayload) {
                    this.$refs.optimizeFrame.contentWindow.postMessage(
                        { type: 'WMS_OPTIMIZE', payload: this.pendingPayload },
                        'http://localhost:5173'
                    )
                }
                this.optimizeLoading = false
            },
            closeOptimizeDialog(done) {
                this.pendingPayload = null
                done()
            },
            usageColor(row) {
                const pct = this.usagePercent(row)
                if (pct >= 90) return '#e74c3c'
                if (pct >= 70) return '#e6a817'
                return '#27ae60'
            },
            totalVolume(row) {
                const len = Number(row.length) || 0
                const hei = Number(row.height) || 0
                const wid = Number(row.width)  || 0
                return Math.round(len * hei * wid)
            },
            usagePercent(row) {
                const total = this.totalVolume(row)
                if (total <= 0) return 0
                return Math.min(100, Math.round((row.usedVolume || 0) / total * 100))
            },
            resetForm() { this.$refs.form.resetFields() },
            del(id) {
                this.$axios.get(this.$httpUrl + '/storage/del?id=' + id).then(res => res.data).then(res => {
                    if (res.code == 200) {
                        this.$message({ message: '操作成功！', type: 'success' })
                        this.loadPost()
                    } else {
                        this.$message({ message: '操作失败！', type: 'error' })
                    }
                })
            },
            mod(row) {
                this.centerDialogVisible = true
                this.$nextTick(() => {
                    this.form.id = row.id
                    this.form.name = row.name
                    this.form.remark = row.remark
                    this.form.capacity = row.capacity
                    this.form.length = row.length || 500
                    this.form.height = row.height || 300
                    this.form.width  = row.width  || 300
                })
            },
            add() {
                this.centerDialogVisible = true
                this.$nextTick(() => { this.resetForm() })
            },
            doSave() {
                this.$axios.post(this.$httpUrl + '/storage/save', this.form).then(res => res.data).then(res => {
                    if (res.code == 200) {
                        this.$message({ message: '操作成功！', type: 'success' })
                        this.centerDialogVisible = false
                        this.loadPost()
                        this.resetForm()
                    } else {
                        this.$message({ message: '操作失败！', type: 'error' })
                    }
                })
            },
            doMod() {
                this.$axios.post(this.$httpUrl + '/storage/update', this.form).then(res => res.data).then(res => {
                    if (res.code == 200) {
                        this.$message({ message: '操作成功！', type: 'success' })
                        this.centerDialogVisible = false
                        this.loadPost()
                        this.resetForm()
                    } else {
                        this.$message({ message: '操作失败！', type: 'error' })
                    }
                })
            },
            save() {
                this.$refs.form.validate(valid => {
                    if (valid) {
                        this.form.id ? this.doMod() : this.doSave()
                    }
                })
            },
            handleSizeChange(val) { this.pageNum = 1; this.pageSize = val; this.loadPost() },
            handleCurrentChange(val) { this.pageNum = val; this.loadPost() },
            resetParam() { this.name = '' },
            loadPost() {
                this.$axios.post(this.$httpUrl + '/storage/listPage', {
                    pageSize: this.pageSize, pageNum: this.pageNum, param: { name: this.name }
                }).then(res => res.data).then(res => {
                    if (res.code == 200) { this.tableData = res.data; this.total = res.total }
                    else { alert('获取数据失败') }
                })
            }
        },
        beforeMount() { this.loadPost() }
    }
</script>

<style scoped>
.toolbar {
  display: flex; align-items: center; gap: 10px; flex-wrap: wrap;
  background: #fff; border-radius: 12px; padding: 14px 18px;
  margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.round-btn { border-radius: 20px; padding: 7px 16px; }
.table-card { background: #fff; border-radius: 12px; padding: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.pagination-wrap { margin-top: 14px; display: flex; justify-content: flex-end; }
.op-btn { border-radius: 14px; }
</style>
