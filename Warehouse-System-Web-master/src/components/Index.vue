<template>
  <el-container style="height:100vh;">
    <el-aside :width="isCollapse ? '64px' : '200px'" class="aside-wrap">
      <Aside :isCollapse="isCollapse"/>
    </el-aside>
    <el-container style="flex-direction:column;overflow:hidden;">
      <el-header height="64px" style="padding:0;">
        <Header @doCollapse="doCollapse" :icon="icon"/>
      </el-header>
      <el-main class="main-wrap">
        <router-view/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import Aside from './Aside'
import Header from './Header'
export default {
  name: 'Index',
  components: { Aside, Header },
  data() {
    return { isCollapse: false, icon: 'el-icon-s-fold' }
  },
  methods: {
    doCollapse() {
      this.isCollapse = !this.isCollapse
      this.icon = this.isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'
    }
  }
}
</script>

<style scoped>
.aside-wrap {
  background: #1a2a4a;
  transition: width 0.25s;
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0,0,0,0.15);
}
.main-wrap {
  background: #f0f4fa;
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}
</style>
