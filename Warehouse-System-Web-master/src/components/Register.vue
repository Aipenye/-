<template>
  <div class="login-body">
    <!-- 复用仓库背景 -->
    <div class="warehouse-bg">
      <div class="ceiling-lights">
        <div class="light" v-for="i in 4" :key="i"></div>
      </div>
      <div class="shelves">
        <div class="shelf" v-for="s in 3" :key="s">
          <div class="shelf-box" v-for="b in 4" :key="b" :class="'box-color-'+(((s-1)*4+b)%5)"></div>
        </div>
      </div>
      <div class="floor"></div>
    </div>

    <!-- 注册卡片 -->
    <div class="register-card">
      <div class="login-header">
        <div class="login-icon">📋</div>
        <h2 class="login-title">用户注册</h2>
        <p class="login-subtitle">Register New Account</p>
      </div>

      <el-form :model="form" :rules="rules" ref="regForm" label-width="80px" class="reg-form">
        <!-- 手机号 -->
        <el-form-item label="手机号" prop="phone">
          <div style="display:flex;gap:8px">
            <el-input v-model="form.phone" placeholder="请输入手机号" size="medium" style="width:160px"
              prefix-icon="el-icon-mobile-phone"/>
            <el-button size="medium" type="primary" plain :disabled="smsDisabled" @click="sendCode"
              style="white-space:nowrap;min-width:100px">
              {{ smsDisabled ? countdown + 's后重发' : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <!-- 验证码 -->
        <el-form-item label="验证码" prop="code">
          <el-input v-model="form.code" placeholder="6位验证码" size="medium" style="width:268px"
            prefix-icon="el-icon-key" maxlength="6"/>
        </el-form-item>

        <!-- 用户名 -->
        <el-form-item label="用户名" prop="name">
          <el-input v-model="form.name" placeholder="请输入用户名" size="medium" style="width:268px"
            prefix-icon="el-icon-user"/>
        </el-form-item>

        <!-- 密码 -->
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码"
            size="medium" style="width:268px" prefix-icon="el-icon-lock"/>
        </el-form-item>

        <!-- 确认密码 -->
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入密码"
            size="medium" style="width:268px" prefix-icon="el-icon-lock"/>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="reg-btn" @click="handleRegister" :loading="loading">
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="to-login">
        已有账号？<span @click="$router.push('/')">立即登录</span>
      </div>
    </div>

    <!-- 人机验证弹窗 -->
    <el-dialog title="安全验证" :visible.sync="captchaVisible" width="320px" :close-on-click-modal="false">
      <div class="captcha-area">
        <p style="margin:0 0 16px;color:#666">请完成下方验证后继续注册</p>
        <div class="captcha-puzzle">
          <div class="puzzle-track">
            <div class="puzzle-bg">
              <span>{{ puzzleText }}</span>
            </div>
            <div class="puzzle-slider" :style="{ left: sliderLeft + 'px' }"
              @mousedown="startDrag" @touchstart="startDrag">
              <i class="el-icon-d-arrow-right"></i>
            </div>
          </div>
          <p class="puzzle-hint">{{ sliderDone ? '✅ 验证通过' : '← 拖动滑块到最右侧' }}</p>
        </div>
      </div>
      <span slot="footer">
        <el-button @click="captchaVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!sliderDone" @click="submitRegister">确认注册</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'Register',
  data() {
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.form.password) {
        callback(new Error('两次输入的密码不一致'));
      } else {
        callback();
      }
    };
    return {
      loading: false,
      smsDisabled: false,
      countdown: 60,
      captchaVisible: false,
      sliderLeft: 0,
      sliderDone: false,
      dragging: false,
      dragStartX: 0,
      puzzleText: '请将滑块拖动到最右侧完成验证',
      form: {
        phone: '',
        code: '',
        name: '',
        password: '',
        confirmPassword: ''
      },
      rules: {
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ],
        code: [
          { required: true, message: '请输入验证码', trigger: 'blur' },
          { len: 6, message: '验证码为6位数字', trigger: 'blur' }
        ],
        name: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码至少6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: validateConfirm, trigger: 'blur' }
        ]
      }
    };
  },
  methods: {
    sendCode() {
      this.$refs.regForm.validateField('phone', (err) => {
        if (err) return;
        this.$axios.post(this.$httpUrl + '/sms/sendCode', { phone: this.form.phone })
          .then(res => res.data)
          .then(res => {
            if (res.code === 200) {
              this.$message.success('验证码已发送，请注意查收');
              this.startCountdown();
            } else {
              this.$message.error(res.msg || '发送失败');
            }
          });
      });
    },
    startCountdown() {
      this.smsDisabled = true;
      this.countdown = 60;
      const timer = setInterval(() => {
        this.countdown--;
        if (this.countdown <= 0) {
          clearInterval(timer);
          this.smsDisabled = false;
        }
      }, 1000);
    },
    handleRegister() {
      this.$refs.regForm.validate((valid) => {
        if (!valid) return;
        // 密码一致性二次提示
        if (this.form.password !== this.form.confirmPassword) {
          this.$message.error('两次输入的密码不一致，请重新输入');
          return;
        }
        // 打开人机验证
        this.sliderLeft = 0;
        this.sliderDone = false;
        this.captchaVisible = true;
      });
    },
    startDrag(e) {
      this.dragging = true;
      this.dragStartX = e.clientX || e.touches[0].clientX;
      const onMove = (ev) => {
        if (!this.dragging) return;
        const x = (ev.clientX || ev.touches[0].clientX) - this.dragStartX;
        const maxLeft = 220; // track宽度 - slider宽度
        this.sliderLeft = Math.max(0, Math.min(x, maxLeft));
        if (this.sliderLeft >= maxLeft) {
          this.sliderDone = true;
          this.dragging = false;
          document.removeEventListener('mousemove', onMove);
          document.removeEventListener('mouseup', onUp);
          document.removeEventListener('touchmove', onMove);
          document.removeEventListener('touchend', onUp);
        }
      };
      const onUp = () => {
        if (!this.sliderDone) this.sliderLeft = 0;
        this.dragging = false;
        document.removeEventListener('mousemove', onMove);
        document.removeEventListener('mouseup', onUp);
        document.removeEventListener('touchmove', onMove);
        document.removeEventListener('touchend', onUp);
      };
      document.addEventListener('mousemove', onMove);
      document.addEventListener('mouseup', onUp);
      document.addEventListener('touchmove', onMove);
      document.addEventListener('touchend', onUp);
    },
    submitRegister() {
      this.loading = true;
      this.$axios.post(this.$httpUrl + '/sms/register', {
        phone: this.form.phone,
        code: this.form.code,
        name: this.form.name,
        password: this.form.password
      }).then(res => res.data).then(res => {
        this.loading = false;
        this.captchaVisible = false;
        if (res.code === 200) {
          this.$message.success('注册成功，请登录');
          this.$router.push('/');
        } else {
          this.$message.error(res.msg || '注册失败');
        }
      }).catch(() => {
        this.loading = false;
        this.captchaVisible = false;
      });
    }
  }
};
</script>

<style scoped>
.login-body {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: #1a2a4a;
  display: flex;
  align-items: center;
  justify-content: center;
}
.warehouse-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, #1a2a4a 0%, #2a3f6f 40%, #3a5080 70%, #8b7355 70%, #7a6245 100%);
}
.ceiling-lights {
  position: absolute;
  top: 0; left: 0; right: 0;
  display: flex;
  justify-content: space-around;
  padding: 0 80px;
}
.light { width: 4px; height: 60px; background: #aaa; position: relative; }
.light::after {
  content: '';
  position: absolute;
  bottom: 0; left: 50%;
  transform: translateX(-50%);
  width: 40px; height: 16px;
  background: #fffde7;
  border-radius: 50%;
  box-shadow: 0 0 30px 15px rgba(255,253,180,0.5), 0 0 80px 40px rgba(255,253,100,0.15);
}
.shelves { position: absolute; right: 30px; top: 80px; display: flex; gap: 20px; }
.shelf {
  display: flex; flex-direction: column; gap: 6px;
  background: #5a4030; padding: 8px 6px;
  border-radius: 4px; border: 2px solid #3a2a1a; position: relative;
}
.shelf-box { width: 45px; height: 30px; border-radius: 3px; border: 1.5px solid rgba(0,0,0,0.3); }
.box-color-0 { background: #e8a040; }
.box-color-1 { background: #e05040; }
.box-color-2 { background: #40a0e0; }
.box-color-3 { background: #60c060; }
.box-color-4 { background: #a060c0; }
.floor { position: absolute; bottom: 0; left: 0; right: 0; height: 30%; background: #8b7355; border-top: 3px solid #6a5240; }

.register-card {
  position: relative;
  z-index: 10;
  width: 420px;
  background: rgba(255,255,255,0.96);
  border-radius: 20px;
  padding: 30px 36px 24px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.5);
  animation: cardAppear 0.5s ease-out;
}
@keyframes cardAppear {
  from { opacity: 0; transform: translateY(24px); }
  to { opacity: 1; transform: translateY(0); }
}
.login-header { text-align: center; margin-bottom: 20px; }
.login-icon { font-size: 36px; display: inline-block; margin-bottom: 6px; }
.login-title { font-size: 20px; font-weight: 700; color: #2c3e50; margin: 0 0 4px; }
.login-subtitle { font-size: 12px; color: #95a5a6; margin: 0; letter-spacing: 1px; }

.reg-btn {
  width: 100%;
  height: 40px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 4px;
  border-radius: 20px;
  background: linear-gradient(135deg, #3498db, #2980b9);
  border: none;
  margin-left: -80px;
  width: calc(268px + 80px);
}
.to-login { text-align: center; margin-top: 14px; font-size: 13px; color: #888; }
.to-login span { color: #3498db; cursor: pointer; font-weight: 600; }
.to-login span:hover { text-decoration: underline; }

/* 滑块验证 */
.captcha-area { padding: 0 10px; }
.puzzle-track {
  position: relative;
  width: 268px;
  height: 44px;
  background: #f0f0f0;
  border-radius: 22px;
  border: 1px solid #ddd;
  overflow: hidden;
  margin: 0 auto;
}
.puzzle-bg {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #bbb;
  user-select: none;
}
.puzzle-slider {
  position: absolute;
  top: 2px;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #3498db, #2980b9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  cursor: grab;
  box-shadow: 0 2px 8px rgba(52,152,219,0.4);
  transition: background 0.2s;
  user-select: none;
}
.puzzle-slider:active { cursor: grabbing; }
.puzzle-hint { text-align: center; font-size: 12px; color: #999; margin: 10px 0 0; }
</style>
