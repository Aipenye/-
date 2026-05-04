<template>
  <div class="profile-wrap">
    <div class="profile-card">
      <div class="avatar-area">
        <div class="avatar-circle">{{ user.name ? user.name[0] : '?' }}</div>
        <div class="user-no">账号：{{ user.no }}</div>
      </div>

      <el-divider>基本信息</el-divider>
      <el-form :model="profileForm" :rules="profileRules" ref="profileForm" label-width="80px">
        <el-form-item label="昵称" prop="name">
          <el-input v-model="profileForm.name" placeholder="请输入昵称"/>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model.number="profileForm.age" placeholder="请输入年龄"/>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="profileForm.sex">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="profileForm.phone" placeholder="请输入电话号码"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveProfile" class="save-btn">保存信息</el-button>
        </el-form-item>
      </el-form>

      <el-divider>修改密码</el-divider>
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdForm" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码"/>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码"/>
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码"/>
        </el-form-item>
        <el-form-item>
          <el-button type="warning" @click="savePassword" class="save-btn">修改密码</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UserProfile',
  data() {
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.pwdForm.newPassword) {
        callback(new Error('两次密码不一致'))
      } else {
        callback()
      }
    }
    return {
      user: JSON.parse(sessionStorage.getItem('CurUser')) || {},
      profileForm: { name: '', age: '', sex: 1, phone: '' },
      profileRules: {
        name: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
        age: [{ type: 'number', min: 1, max: 150, message: '请输入有效年龄', trigger: 'blur' }],
        phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入有效手机号', trigger: 'blur' }]
      },
      pwdForm: { oldPassword: '', newPassword: '', confirmPassword: '' },
      pwdRules: {
        oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [{ required: true, min: 6, message: '新密码至少6位', trigger: 'blur' }],
        confirmPassword: [{ required: true, validator: validateConfirm, trigger: 'blur' }]
      }
    }
  },
  created() {
    this.profileForm.name = this.user.name
    this.profileForm.age = this.user.age
    this.profileForm.sex = this.user.sex
    this.profileForm.phone = this.user.phone
  },
  methods: {
    saveProfile() {
      this.$refs.profileForm.validate(valid => {
        if (!valid) return
        this.$axios.post(this.$httpUrl + '/user/updateProfile', {
          id: this.user.id,
          name: this.profileForm.name,
          age: this.profileForm.age,
          sex: this.profileForm.sex,
          phone: this.profileForm.phone
        }).then(res => {
          if (res.data.code === 200) {
            this.$message.success('信息已更新')
            // 更新 sessionStorage
            const updated = { ...this.user, ...this.profileForm }
            sessionStorage.setItem('CurUser', JSON.stringify(updated))
            this.user = updated
          } else {
            this.$message.error('更新失败')
          }
        })
      })
    },
    savePassword() {
      this.$refs.pwdForm.validate(valid => {
        if (!valid) return
        this.$axios.post(this.$httpUrl + '/user/updatePassword', {
          id: this.user.id,
          oldPassword: this.pwdForm.oldPassword,
          newPassword: this.pwdForm.newPassword
        }).then(res => {
          if (res.data.code === 200) {
            this.$message.success('密码修改成功，请重新登录')
            this.$refs.pwdForm.resetFields()
          } else {
            this.$message.error(res.data.msg || '原密码错误')
          }
        })
      })
    }
  }
}
</script>

<style scoped>
.profile-wrap {
  display: flex; justify-content: center; padding: 30px 16px;
  background: #f4f6fa; min-height: 100%;
}
.profile-card {
  background: #fff; border-radius: 16px; padding: 32px 40px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08); width: 100%; max-width: 520px;
}
.avatar-area { text-align: center; margin-bottom: 8px; }
.avatar-circle {
  width: 72px; height: 72px; border-radius: 50%;
  background: linear-gradient(135deg, #1a3a5c, #2980b9);
  color: #fff; font-size: 28px; font-weight: 700;
  display: inline-flex; align-items: center; justify-content: center;
  margin-bottom: 8px;
}
.user-no { color: #999; font-size: 13px; }
.save-btn { border-radius: 20px; padding: 8px 28px; }
</style>
