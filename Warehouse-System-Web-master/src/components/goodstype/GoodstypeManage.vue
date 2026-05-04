<template>
    <div>
        <div class="toolbar">
            <el-input v-model="name" placeholder="请输入分类名" prefix-icon="el-icon-search" size="small" style="width:180px"
                      @keyup.enter.native="loadPost"/>
            <el-button type="primary" size="small" icon="el-icon-search" @click="loadPost" class="round-btn">查询</el-button>
            <el-button size="small" icon="el-icon-refresh" @click="resetParam" class="round-btn">重置</el-button>
            <el-button type="success" size="small" icon="el-icon-plus" @click="add" class="round-btn" style="margin-left:auto">新增分类</el-button>
        </div>
        <div class="table-card">
        <el-table :data="tableData"
                  :header-cell-style="{ background: '#f0f4fa', color: '#2c3e50', fontWeight:'600' }"
                  stripe>
            <el-table-column prop="id" label="ID" width="60"/>
            <el-table-column prop="name" label="分类名" width="200"/>
            <el-table-column prop="remark" label="备注"/>
            <el-table-column prop="operate" label="操作" width="160">
                <template slot-scope="scope">
                    <el-button size="small" type="primary" plain @click="mod(scope.row)" class="op-btn">编辑</el-button>
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

        <el-dialog title="分类维护" :visible.sync="centerDialogVisible" width="30%" center>
            <el-form ref="form" :rules="rules" :model="form" label-width="80px">
                <el-form-item label="分类名" prop="name">
                    <el-col :span="20"><el-input v-model="form.name"></el-input></el-col>
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
    </div>
</template>

<script>
    export default {
        name: "GoodstypeManage",
        data() {
            return {
                tableData: [],
                pageSize:10,
                pageNum:1,
                total:0,
                name:'',
                centerDialogVisible:false,
                form:{
                    id:'',
                    name:'',
                    remark:''
                },
                rules: {
                    name: [
                        {required: true, message: '请输入分类名', trigger: 'blur'}
                    ]
                }
            }
        },
        methods:{
            resetForm() {
                this.$refs.form.resetFields();
            },
            del(id){
                console.log(id)

                this.$axios.get(this.$httpUrl+'/goodstype/del?id='+id).then(res=>res.data).then(res=>{
                    console.log(res)
                    if(res.code==200){

                        this.$message({
                            message: '操作成功！',
                            type: 'success'
                        });
                        this.loadPost()
                    }else{
                        this.$message({
                            message: '操作失败！',
                            type: 'error'
                        });
                    }

                })
            },
            mod(row){
                this.centerDialogVisible = true
                this.$nextTick(()=>{
                    //赋值到表单
                    this.form.id = row.id
                    this.form.name = row.name
                    this.form.remark = row.remark
                })
            },
            add(){

                this.centerDialogVisible = true
                this.$nextTick(()=>{
                    this.resetForm()
                })

            },
            doSave(){
                this.$axios.post(this.$httpUrl+'/goodstype/save',this.form).then(res=>res.data).then(res=>{
                    console.log(res)
                    if(res.code==200){

                        this.$message({
                            message: '操作成功！',
                            type: 'success'
                        });
                        this.centerDialogVisible = false
                        this.loadPost()
                        this. resetForm()
                    }else{
                        this.$message({
                            message: '操作失败！',
                            type: 'error'
                        });
                    }

                })
            },
            doMod(){
                this.$axios.post(this.$httpUrl+'/goodstype/update',this.form).then(res=>res.data).then(res=>{
                    console.log(res)
                    if(res.code==200){

                        this.$message({
                            message: '操作成功！',
                            type: 'success'
                        });
                        this.centerDialogVisible = false
                        this.loadPost()
                        this. resetForm()
                    }else{
                        this.$message({
                            message: '操作失败！',
                            type: 'error'
                        });
                    }

                })
            },
            save(){
                this.$refs.form.validate((valid) => {
                    if (valid) {
                        if(this.form.id){
                            this.doMod();
                        }else{
                            this.doSave();
                        }
                    } else {
                        console.log('error submit!!');
                        return false;
                    }
                });

            },
            handleSizeChange(val) {
                console.log(`每页 ${val} 条`);
                this.pageNum=1
                this.pageSize=val
                this.loadPost()
            },
            handleCurrentChange(val) {
                console.log(`当前页: ${val}`);
                this.pageNum=val
                this.loadPost()
            },
            resetParam(){
                this.name=''
            },
            loadPost(){
                this.$axios.post(this.$httpUrl+'/goodstype/listPage',{
                    pageSize:this.pageSize,
                    pageNum:this.pageNum,
                    param:{
                        name:this.name
                    }
                }).then(res=>res.data).then(res=>{
                    console.log(res)
                    if(res.code==200){
                        this.tableData=res.data
                        this.total=res.total
                    }else{
                        alert('获取数据失败')
                    }

                })
            }
        },
        beforeMount() {
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
.op-btn { border-radius: 14px; }
</style>