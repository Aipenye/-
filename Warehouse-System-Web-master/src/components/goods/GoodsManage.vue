<template>
    <div>
        <div class="toolbar">
            <el-input v-model="name" placeholder="请输入物品名" prefix-icon="el-icon-search" size="small" style="width:180px"
                      @keyup.enter.native="loadPost"/>
            <el-select v-model="storage" placeholder="仓库" clearable size="small" style="width:130px">
                <el-option v-for="item in storageData" :key="item.id" :label="item.name" :value="item.id"/>
            </el-select>
            <el-select v-model="goodstype" placeholder="分类" clearable size="small" style="width:130px">
                <el-option v-for="item in goodstypeData" :key="item.id" :label="item.name" :value="item.id"/>
            </el-select>
            <el-button type="primary" size="small" icon="el-icon-search" @click="loadPost" class="round-btn">查询</el-button>
            <el-button size="small" icon="el-icon-refresh" @click="resetParam" class="round-btn">重置</el-button>
            <el-button type="success" size="small" icon="el-icon-plus" @click="addGoods" class="round-btn" style="margin-left:auto">新增物品</el-button>
        </div>
        <div class="table-card">
        <el-table :data="tableData"
                  :header-cell-style="{ background: '#f0f4fa', color: '#2c3e50', fontWeight:'600' }"
                  stripe>
            <el-table-column prop="id" label="ID" width="60"/>
            <el-table-column prop="name" label="物品名" width="160"/>
            <el-table-column prop="storage" label="仓库" width="160" :formatter="formatStorage"/>
            <el-table-column prop="goodstype" label="分类" width="160" :formatter="formatGoodstype"/>
            <el-table-column prop="count" label="数量" width="100"/>
            <el-table-column prop="remark" label="备注"/>
            <el-table-column label="操作" width="120">
                <template slot-scope="scope">
                    <el-button size="small" type="primary" plain @click="openDimDialog(scope.row)" class="op-btn">设置尺寸</el-button>
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

        <!-- 新增物品对话框 -->
        <el-dialog title="新增物品" :visible.sync="addDialogVisible" width="35%" center>
            <el-form ref="addForm" :rules="addRules" :model="addForm" label-width="80px">
                <el-form-item label="物品名" prop="name">
                    <el-col :span="20"><el-input v-model="addForm.name" placeholder="请输入物品名"></el-input></el-col>
                </el-form-item>
                <el-form-item label="分类" prop="goodstype">
                    <el-col :span="20">
                        <el-select v-model="addForm.goodstype" placeholder="请选择物品分类" style="width:100%">
                            <el-option v-for="item in goodstypeData" :key="item.id" :label="item.name" :value="item.id"/>
                        </el-select>
                    </el-col>
                </el-form-item>
                <el-form-item label="长(cm)" prop="length">
                    <el-col :span="20"><el-input v-model.number="addForm.length" placeholder="物品长度"></el-input></el-col>
                </el-form-item>
                <el-form-item label="宽(cm)" prop="width">
                    <el-col :span="20"><el-input v-model.number="addForm.width" placeholder="物品宽度"></el-input></el-col>
                </el-form-item>
                <el-form-item label="高(cm)" prop="height">
                    <el-col :span="20"><el-input v-model.number="addForm.height" placeholder="物品高度"></el-input></el-col>
                </el-form-item>
                <el-form-item label="备注">
                    <el-col :span="20"><el-input type="textarea" v-model="addForm.remark" placeholder="备注（可选）"></el-input></el-col>
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="addDialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="saveGoods">确 定</el-button>
            </span>
        </el-dialog>

        <!-- 设置尺寸对话框 -->
        <el-dialog title="设置货物尺寸" :visible.sync="dimDialogVisible" width="30%" center>
            <el-form :model="dimForm" label-width="80px">
                <el-form-item label="货物名">
                    <span>{{ dimForm.name }}</span>
                </el-form-item>
                <el-form-item label="长(cm)">
                    <el-col :span="20"><el-input v-model.number="dimForm.length" placeholder="长度"></el-input></el-col>
                </el-form-item>
                <el-form-item label="高(cm)">
                    <el-col :span="20"><el-input v-model.number="dimForm.height" placeholder="高度"></el-input></el-col>
                </el-form-item>
                <el-form-item label="宽(cm)">
                    <el-col :span="20"><el-input v-model.number="dimForm.width" placeholder="宽度"></el-input></el-col>
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="dimDialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="saveDim">确 定</el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
    export default {
        name: "GoodsManage",
        data() {
            return {
                user: JSON.parse(sessionStorage.getItem('CurUser')),
                storageData: [],
                goodstypeData: [],
                tableData: [],
                pageSize: 10,
                pageNum: 1,
                total: 0,
                name: '',
                storage: '',
                goodstype: '',
                dimDialogVisible: false,
                dimForm: { id: '', name: '', length: 1, height: 1, width: 1 },
                addDialogVisible: false,
                addForm: { name: '', goodstype: '', length: null, width: null, height: null, remark: '' },
                addRules: {
                    name:      [{ required: true, message: '请输入物品名', trigger: 'blur' }],
                    goodstype: [{ required: true, message: '请选择物品分类', trigger: 'change' }],
                    length: [{ required: true, message: '请输入长度', trigger: 'blur' },
                             { type: 'number', min: 0.01, message: '长度必须大于0', trigger: 'blur' }],
                    width:  [{ required: true, message: '请输入宽度', trigger: 'blur' },
                             { type: 'number', min: 0.01, message: '宽度必须大于0', trigger: 'blur' }],
                    height: [{ required: true, message: '请输入高度', trigger: 'blur' },
                             { type: 'number', min: 0.01, message: '高度必须大于0', trigger: 'blur' }]
                }
            }
        },
        methods: {
            addGoods() {
                this.addForm = { name: '', goodstype: '', length: null, width: null, height: null, remark: '' }
                this.$nextTick(() => { this.$refs.addForm && this.$refs.addForm.resetFields() })
                this.addDialogVisible = true
            },
            saveGoods() {
                this.$refs.addForm.validate(valid => {
                    if (!valid) {
                        this.$message({ message: '请完整填写必填项', type: 'warning' })
                        return
                    }
                    this.$axios.post(this.$httpUrl + '/goods/save', this.addForm).then(res => res.data).then(res => {
                        if (res.code == 200) {
                            this.$message({ message: '新增成功！', type: 'success' })
                            this.addDialogVisible = false
                            this.loadPost()
                        } else {
                            this.$message({ message: '新增失败！', type: 'error' })
                        }
                    }).catch(err => {
                        this.$message({ message: '新增失败：' + (err.message || '请求异常'), type: 'error' })
                    })
                })
            },
            openDimDialog(row) {
                this.dimForm = { id: row.id, name: row.name, length: row.length || 1, height: row.height || 1, width: row.width || 1 }
                this.dimDialogVisible = true
            },
            saveDim() {
                this.$axios.post(this.$httpUrl + '/goods/update', this.dimForm).then(res => res.data).then(res => {
                    if (res.code == 200) {
                        this.$message({ message: '尺寸设置成功！', type: 'success' })
                        this.dimDialogVisible = false
                        this.loadPost()
                    } else {
                        this.$message({ message: '操作失败！', type: 'error' })
                    }
                })
            },
            formatStorage(row) {
                let temp = this.storageData.find(item => item.id == row.storage)
                return temp && temp.name
            },
            formatGoodstype(row) {
                let temp = this.goodstypeData.find(item => item.id == row.goodstype)
                return temp && temp.name
            },
            handleSizeChange(val) { this.pageNum = 1; this.pageSize = val; this.loadPost() },
            handleCurrentChange(val) { this.pageNum = val; this.loadPost() },
            resetParam() { this.name = ''; this.storage = ''; this.goodstype = '' },
            loadPost() {
                this.$axios.post(this.$httpUrl + '/goods/listPage', {
                    pageSize: this.pageSize,
                    pageNum: this.pageNum,
                    param: { name: this.name, goodstype: this.goodstype + '', storage: this.storage + '' }
                }).then(res => res.data).then(res => {
                    if (res.code == 200) { this.tableData = res.data; this.total = res.total }
                    else { alert('获取数据失败') }
                })
            },
            loadStorage() {
                this.$axios.get(this.$httpUrl + '/storage/list').then(res => res.data).then(res => {
                    if (res.code == 200) this.storageData = res.data
                })
            },
            loadGoodstype() {
                this.$axios.get(this.$httpUrl + '/goodstype/list').then(res => res.data).then(res => {
                    if (res.code == 200) this.goodstypeData = res.data
                })
            }
        },
        beforeMount() {
            this.loadStorage()
            this.loadGoodstype()
            this.loadPost()
        }
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
</style>
