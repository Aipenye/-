<template>
  <div class="header-wrap">
    <div class="header-left">
      <i :class="icon" class="collapse-btn" @click="collapse"></i>
      <span class="sys-title">仓库管理系统</span>
    </div>
    <div class="header-right">
      <el-dropdown>
        <div class="user-block">
          <div class="user-avatar">{{ user.name ? user.name[0] : 'A' }}</div>
          <div class="user-info">
            <span class="user-name">{{ user.name }}</span>
            <span class="user-role">{{ roleLabel }}</span>
          </div>
          <i class="el-icon-arrow-down" style="font-size:12px;color:#999;margin-left:4px;"/>
        </div>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item @click.native="toUser">
            <i class="el-icon-user"/> 个人中心
          </el-dropdown-item>
          <el-dropdown-item divided @click.native="logout">
            <i class="el-icon-switch-button"/> 退出登录
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Header',
  data() {
    return {
      user: JSON.parse(sessionStorage.getItem('CurUser')) || {}
    }
  },
  computed: {
    roleLabel() {
      const map = { 0: '超级管理员', 1: '管理员', 2: '工人' }
      return map[this.user.roleId] || '用户'
    }
  },
  props: { icon: String },
  methods: {
    toUser() { this.$router.push('/Profile') },
    logout() {
      this.$confirm('确定退出登录？', '提示', { type: 'warning' }).then(() => {
        this.$message.success('已退出登录')
        this.$router.push('/')
        sessionStorage.clear()
      }).catch(() => {})
    },
    collapse() { this.$emit('doCollapse') }
  },
  created() {
    this.$router.push('/Home')
  }
}
</script>

<style scoped>
.header-wrap {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px 0 16px;
  background: #fff;
  box-shadow: 0 1px 6px rgba(0,0,0,0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}
.collapse-btn {
  font-size: 20px;
  color: #555;
  cursor: pointer;
  transition: color 0.2s, transform 0.2s;
  padding: 4px;
  border-radius: 6px;
}
.collapse-btn:hover { color: #3498db; transform: scale(1.15); background: #f0f7ff; }
.sys-title {
  font-size: 17px;
  font-weight: 700;
  color: #2c3e50;
  letter-spacing: 1px;
}

.header-right { display: flex; align-items: center; }
.user-block {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 24px;
  transition: background 0.2s;
}
.user-block:hover { background: #f0f4fa; }
.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.user-info { line-height: 1.3; }
.user-name { display: block; font-size: 13px; font-weight: 600; color: #2c3e50; }
.user-role { display: block; font-size: 11px; color: #999; }
</style>
