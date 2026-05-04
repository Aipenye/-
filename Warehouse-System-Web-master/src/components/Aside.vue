<template>
  <el-menu
    background-color="#1a2a4a"
    text-color="rgba(255,255,255,0.75)"
    active-text-color="#ffffff"
    style="height:100%;border-right:none;"
    default-active="/Home"
    :collapse="isCollapse"
    :collapse-transition="false"
    router
  >
    <!-- Logo区 -->
    <div class="aside-logo" :class="{collapsed: isCollapse}">
      <span class="logo-icon">🏭</span>
      <span class="logo-text" v-if="!isCollapse">仓库管理系统</span>
    </div>

    <el-menu-item index="/Home" class="menu-item">
      <i class="el-icon-s-home"></i>
      <span slot="title">首页</span>
    </el-menu-item>

    <el-menu-item
      :index="'/'+item.menuclick"
      v-for="(item,i) in menu"
      :key="i"
      class="menu-item"
    >
      <i :class="item.menuicon"></i>
      <span slot="title">{{item.menuname}}</span>
    </el-menu-item>

    <!-- AGV 仿真（管理员专属，硬编码） -->
    <el-menu-item index="/AgvSim" class="menu-item agv-item">
      <i class="el-icon-truck"></i>
      <span slot="title">AGV 仿真</span>
    </el-menu-item>
  </el-menu>
</template>

<script>
export default {
  name: 'Aside',
  computed: {
    menu() { return this.$store.state.menu }
  },
  props: { isCollapse: Boolean }
}
</script>

<style scoped>
.aside-logo {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
  overflow: hidden;
  white-space: nowrap;
}
.aside-logo.collapsed { justify-content: center; padding: 0; }
.logo-icon { font-size: 22px; flex-shrink: 0; }
.logo-text {
  font-size: 15px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
}

/* 菜单项 */
.menu-item {
  transition: background 0.2s, transform 0.15s, padding-left 0.2s !important;
  border-left: 3px solid transparent;
  margin: 2px 0;
}
.menu-item:hover {
  background: rgba(52,152,219,0.18) !important;
  border-left-color: #3498db;
  transform: translateX(3px);
}
/* Element UI active */
.el-menu-item.is-active {
  background: rgba(52,152,219,0.28) !important;
  border-left-color: #3498db !important;
  color: #fff !important;
  font-weight: 600;
}
.agv-item {
  border-top: 1px solid rgba(255,255,255,0.06);
  margin-top: 4px !important;
}
</style>
