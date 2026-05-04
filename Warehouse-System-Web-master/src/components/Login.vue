<template>
  <div class="login-body">
    <!-- 仓库背景 -->
    <div class="warehouse-bg">
      <!-- 天花板灯光 -->
      <div class="ceiling-lights">
        <div class="light" v-for="i in 4" :key="i"></div>
      </div>
      <!-- 货架 -->
      <div class="shelves">
        <div class="shelf" v-for="s in 3" :key="s">
          <div class="shelf-box" v-for="b in 4" :key="b" :class="'box-color-'+(((s-1)*4+b)%5)"></div>
        </div>
      </div>
      <!-- 地板 -->
      <div class="floor"></div>
      <!-- 浮动货物粒子 -->
      <div class="particle" v-for="p in 8" :key="p" :style="particleStyle(p)"></div>
    </div>

    <!-- 卡通工人（左侧） -->
    <div class="worker-container">
      <!--
        SVG 绘制顺序决定层叠（后绘制 = 在上层）：
        腿 → 身体 → 上臂(箱子后面) → 箱子 → 下臂/手(箱子前面) → 脖子 → 头
      -->
      <svg class="worker-svg" viewBox="0 0 140 220" xmlns="http://www.w3.org/2000/svg">

        <!-- ① 腿部（用 <g> 包裹，方便 CSS 设置旋转轴） -->
        <g class="leg-left-group">
          <rect x="48" y="152" width="16" height="44" rx="6" fill="#3a7bc8"/>
          <ellipse cx="56" cy="196" rx="12" ry="6" fill="#2a2a2a"/>
        </g>
        <g class="leg-right-group">
          <rect x="76" y="152" width="16" height="44" rx="6" fill="#3a7bc8"/>
          <ellipse cx="84" cy="196" rx="12" ry="6" fill="#2a2a2a"/>
        </g>

        <!-- ② 身体 -->
        <rect x="44" y="74" width="52" height="82" rx="8" fill="#4a90d9"/>
        <!-- 工装背带（箱子上方可见部分） -->
        <line x1="58" y1="76" x2="54" y2="98" stroke="#2c5f8a" stroke-width="4" stroke-linecap="round"/>
        <line x1="82" y1="76" x2="86" y2="98" stroke="#2c5f8a" stroke-width="4" stroke-linecap="round"/>

        <!-- ③ 上臂（绘制在箱子之前，视觉上在箱子后面） -->
        <path d="M44,82 C28,88 22,112 23,134" stroke="#f4a460" stroke-width="11" fill="none" stroke-linecap="round"/>
        <path d="M96,82 C112,88 118,112 117,134" stroke="#f4a460" stroke-width="11" fill="none" stroke-linecap="round"/>

        <!-- ④ 货物箱子（盖住上臂中段，视觉上抱在胸前） -->
        <rect x="26" y="90" width="88" height="62" rx="5" fill="#e8a040" stroke="#c07820" stroke-width="2"/>
        <rect x="26" y="90" width="88" height="11" rx="3" fill="#c07820"/>
        <line x1="70" y1="90" x2="70" y2="152" stroke="#c07820" stroke-width="1.5"/>
        <line x1="26" y1="121" x2="114" y2="121" stroke="#c07820" stroke-width="1.5"/>
        <text x="70" y="139" text-anchor="middle" font-size="17" fill="#7a4010" font-weight="bold" font-family="Arial,sans-serif">WMS</text>
        <rect x="30" y="95" width="76" height="5" rx="2" fill="rgba(255,255,255,0.25)"/>

        <!-- ⑤ 下臂/手（绘制在箱子之后，视觉上托在箱子底部） -->
        <path d="M23,134 C20,150 30,156 44,153" stroke="#f4a460" stroke-width="11" fill="none" stroke-linecap="round"/>
        <path d="M117,134 C120,150 110,156 96,153" stroke="#f4a460" stroke-width="11" fill="none" stroke-linecap="round"/>

        <!-- ⑥ 脖子 -->
        <rect x="62" y="64" width="16" height="14" rx="4" fill="#f4a460"/>

        <!-- ⑦ 头部 -->
        <circle cx="70" cy="42" r="22" fill="#f4a460"/>
        <!-- 头发 -->
        <path d="M48,38 Q52,18 70,16 Q88,18 92,38" fill="#5c3a1e"/>
        <!-- 安全帽 -->
        <path d="M46,34 Q70,12 94,34" fill="#ffcc00" stroke="#e6a800" stroke-width="2"/>
        <rect x="44" y="32" width="52" height="7" rx="3" fill="#ffcc00" stroke="#e6a800" stroke-width="1"/>
        <!-- 帽檐高光 -->
        <rect x="46" y="33" width="48" height="3" rx="2" fill="rgba(255,255,255,0.3)"/>
        <!-- 眉毛 -->
        <path d="M56,43 Q61,39 66,42" stroke="#5c3a1e" stroke-width="2.5" fill="none" stroke-linecap="round"/>
        <path d="M74,42 Q79,39 84,43" stroke="#5c3a1e" stroke-width="2.5" fill="none" stroke-linecap="round"/>
        <!-- 眼睛（睁开） -->
        <g v-if="!eyesClosed">
          <circle cx="62" cy="49" r="5" fill="white"/>
          <circle cx="62" cy="49" r="3" fill="#333"/>
          <circle cx="63.5" cy="47.5" r="1" fill="white"/>
          <circle cx="78" cy="49" r="5" fill="white"/>
          <circle cx="78" cy="49" r="3" fill="#333"/>
          <circle cx="79.5" cy="47.5" r="1" fill="white"/>
        </g>
        <!-- 眼睛（闭上） -->
        <g v-if="eyesClosed">
          <path d="M57,49 Q62,54 67,49" stroke="#5c3a1e" stroke-width="2.5" fill="none" stroke-linecap="round"/>
          <path d="M73,49 Q78,54 83,49" stroke="#5c3a1e" stroke-width="2.5" fill="none" stroke-linecap="round"/>
        </g>
        <!-- 鼻子 -->
        <circle cx="70" cy="55" r="2" fill="#e8956d"/>
        <!-- 微笑 -->
        <path d="M63,61 Q70,67 77,61" stroke="#c07040" stroke-width="2" fill="none" stroke-linecap="round"/>
        <!-- 腮红 -->
        <ellipse cx="54" cy="58" rx="5" ry="3" fill="#ffaaaa" opacity="0.6"/>
        <ellipse cx="86" cy="58" rx="5" ry="3" fill="#ffaaaa" opacity="0.6"/>
      </svg>
      <div class="worker-shadow"></div>
    </div>

    <!-- 卡片 -->
    <div class="login-card">
      <div class="login-header">
        <div class="login-icon">🏭</div>
        <h2 class="login-title">仓库管理系统</h2>
        <p class="login-subtitle">{{ mode === 'login' ? 'Warehouse Management System' : 'Register New Account' }}</p>
      </div>

      <!-- 登录表单 -->
      <el-form v-if="mode === 'login'" :model="loginForm" :rules="loginRules" ref="loginForm" class="card-form">
        <el-form-item prop="no">
          <el-input v-model="loginForm.no" size="medium" prefix-icon="el-icon-user" placeholder="请输入账号" autocomplete="off"/>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" show-password size="medium"
            prefix-icon="el-icon-lock" placeholder="请输入密码" autocomplete="off"
            @focus="onPasswordFocus" @blur="onPasswordBlur" @keyup.enter.native="confirm"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="full-btn" :disabled="confirm_disabled" @click="confirm">登 录</el-button>
        </el-form-item>
      </el-form>

      <!-- 注册表单 -->
      <el-form v-if="mode === 'register'" :model="regForm" :rules="regRules" ref="regForm" class="card-form">
        <el-form-item prop="phone">
          <div style="display:flex;gap:8px">
            <el-input v-model="regForm.phone" size="medium" prefix-icon="el-icon-mobile-phone" placeholder="请输入手机号" style="flex:1"/>
            <el-button size="medium" type="primary" plain :disabled="smsDisabled" @click="sendCode" style="white-space:nowrap;min-width:96px">
              {{ smsDisabled ? countdown + 's后重发' : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="code">
          <el-input v-model="regForm.code" size="medium" prefix-icon="el-icon-key" placeholder="6位验证码" maxlength="6"/>
        </el-form-item>
        <el-form-item prop="name">
          <el-input v-model="regForm.name" size="medium" prefix-icon="el-icon-user" placeholder="请输入用户名"/>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="regForm.password" type="password" show-password size="medium" prefix-icon="el-icon-lock" placeholder="请输入密码"/>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="regForm.confirmPassword" type="password" show-password size="medium" prefix-icon="el-icon-lock" placeholder="再次输入密码"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="full-btn" :loading="regLoading" @click="handleRegister">注 册</el-button>
        </el-form-item>
      </el-form>

      <div class="switch-tip">
        <span v-if="mode === 'login'">没有账号？<a @click="switchMode('register')">立即注册</a></span>
        <span v-else>已有账号？<a @click="switchMode('login')">立即登录</a></span>
      </div>
    </div>

    <!-- 滑块验证弹窗 -->
    <el-dialog title="安全验证" :visible.sync="captchaVisible" width="320px" :close-on-click-modal="false">
      <div class="captcha-area">
        <p style="margin:0 0 16px;color:#666;font-size:13px">拖动滑块到最右侧完成验证</p>
        <div class="puzzle-track">
          <div class="puzzle-bg"><span>{{ sliderDone ? '验证通过 ✅' : '← 拖动滑块' }}</span></div>
          <div class="puzzle-slider" :style="{ left: sliderLeft + 'px' }" @mousedown="startDrag" @touchstart="startDrag">
            <i class="el-icon-d-arrow-right"></i>
          </div>
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
  name: 'Login',
  data() {
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.regForm.password) callback(new Error('两次密码不一致'));
      else callback();
    };
    return {
      mode: 'login',
      // 登录
      confirm_disabled: false,
      eyesClosed: false,
      loginForm: { no: '', password: '' },
      loginRules: {
        no: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
      },
      // 注册
      regLoading: false,
      smsDisabled: false,
      countdown: 60,
      captchaVisible: false,
      sliderLeft: 0,
      sliderDone: false,
      dragging: false,
      dragStartX: 0,
      regForm: { phone: '', code: '', name: '', password: '', confirmPassword: '' },
      regRules: {
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ],
        code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
        name: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '至少6位', trigger: 'blur' }],
        confirmPassword: [{ required: true, message: '请再次输入密码', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }],
      }
    };
  },
  methods: {
    switchMode(m) {
      this.mode = m;
      this.$nextTick(() => {
        if (m === 'login') this.$refs.loginForm && this.$refs.loginForm.clearValidate();
        else this.$refs.regForm && this.$refs.regForm.clearValidate();
      });
    },
    onPasswordFocus() { this.eyesClosed = true; },
    onPasswordBlur() { this.eyesClosed = false; },
    particleStyle(i) {
      const positions = [
        { left: '5%', top: '20%', delay: '0s', duration: '3s' },
        { left: '10%', top: '60%', delay: '0.5s', duration: '4s' },
        { left: '20%', top: '40%', delay: '1s', duration: '3.5s' },
        { left: '75%', top: '25%', delay: '0.3s', duration: '4.5s' },
        { left: '85%', top: '55%', delay: '1.2s', duration: '3s' },
        { left: '90%', top: '70%', delay: '0.7s', duration: '5s' },
        { left: '65%', top: '15%', delay: '1.5s', duration: '3.8s' },
        { left: '15%', top: '80%', delay: '0.2s', duration: '4.2s' },
      ];
      const p = positions[i - 1];
      return { left: p.left, top: p.top, animationDelay: p.delay, animationDuration: p.duration };
    },
    confirm() {
      this.confirm_disabled = true;
      this.$refs.loginForm.validate((valid) => {
        if (!valid) { this.confirm_disabled = false; return; }
        this.$axios.post(this.$httpUrl + '/user/login', this.loginForm).then(res => res.data).then(res => {
          if (res.code == 200) {
            const user = res.data.user;
            sessionStorage.setItem('CurUser', JSON.stringify(user));
            if (user.roleId === 2) {
              this.$router.replace('/Worker/orders');
            } else {
              this.$store.commit('setMenu', res.data.menu);
              this.$router.replace('/Index');
            }
          } else {
            this.confirm_disabled = false;
            this.$message.error('用户名或密码错误！');
          }
        });
      });
    },
    sendCode() {
      this.$refs.regForm.validateField('phone', (err) => {
        if (err) return;
        this.$axios.post(this.$httpUrl + '/sms/sendCode', { phone: this.regForm.phone }).then(res => res.data).then(res => {
          if (res.code === 200) {
            this.$message.success('验证码已发送，请注意查收');
            this.smsDisabled = true;
            this.countdown = 60;
            const timer = setInterval(() => {
              this.countdown--;
              if (this.countdown <= 0) { clearInterval(timer); this.smsDisabled = false; }
            }, 1000);
          } else {
            this.$message.error(res.msg || '发送失败');
          }
        });
      });
    },
    handleRegister() {
      this.$refs.regForm.validate((valid) => {
        if (!valid) return;
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
        const maxLeft = 220;
        this.sliderLeft = Math.max(0, Math.min(x, maxLeft));
        if (this.sliderLeft >= maxLeft) {
          this.sliderDone = true;
          this.dragging = false;
          document.removeEventListener('mousemove', onMove);
          document.removeEventListener('mouseup', onUp);
        }
      };
      const onUp = () => {
        if (!this.sliderDone) this.sliderLeft = 0;
        this.dragging = false;
        document.removeEventListener('mousemove', onMove);
        document.removeEventListener('mouseup', onUp);
      };
      document.addEventListener('mousemove', onMove);
      document.addEventListener('mouseup', onUp);
    },
    submitRegister() {
      this.regLoading = true;
      this.$axios.post(this.$httpUrl + '/sms/register', {
        phone: this.regForm.phone,
        code: this.regForm.code,
        name: this.regForm.name,
        password: this.regForm.password
      }).then(res => res.data).then(res => {
        this.regLoading = false;
        this.captchaVisible = false;
        if (res.code === 200) {
          this.$message.success('注册成功，请登录');
          this.switchMode('login');
        } else {
          this.$message.error(res.msg || '注册失败');
        }
      }).catch(() => { this.regLoading = false; this.captchaVisible = false; });
    }
  }
}
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
  animation: flicker 4s infinite;
}
.light:nth-child(2)::after { animation-delay: 0.3s; }
.light:nth-child(3)::after { animation-delay: 0.7s; }
.light:nth-child(4)::after { animation-delay: 1.1s; }
@keyframes flicker {
  0%, 95%, 100% { opacity: 1; }
  96% { opacity: 0.7; }
  97% { opacity: 1; }
  98% { opacity: 0.6; }
}
.shelves { position: absolute; right: 30px; top: 80px; display: flex; gap: 20px; }
.shelf {
  display: flex; flex-direction: column; gap: 6px;
  background: #5a4030; padding: 8px 6px;
  border-radius: 4px; border: 2px solid #3a2a1a; position: relative;
}
.shelf::before {
  content: '';
  position: absolute;
  left: -8px; top: 0; bottom: 0;
  width: 8px; background: #3a2a1a;
  border-radius: 4px 0 0 4px;
}
.shelf-box {
  width: 45px; height: 30px;
  border-radius: 3px; border: 1.5px solid rgba(0,0,0,0.3);
  position: relative;
  animation: boxFloat 3s ease-in-out infinite;
}
.shelf-box::after {
  content: '';
  position: absolute;
  top: 3px; left: 3px; right: 3px; height: 6px;
  background: rgba(255,255,255,0.2); border-radius: 2px;
}
.box-color-0 { background: #e8a040; animation-delay: 0s; }
.box-color-1 { background: #e05040; animation-delay: 0.4s; }
.box-color-2 { background: #40a0e0; animation-delay: 0.8s; }
.box-color-3 { background: #60c060; animation-delay: 1.2s; }
.box-color-4 { background: #a060c0; animation-delay: 1.6s; }
@keyframes boxFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}
.floor {
  position: absolute; bottom: 0; left: 0; right: 0;
  height: 30%; background: #8b7355; border-top: 3px solid #6a5240;
}
.particle {
  position: absolute; width: 8px; height: 8px;
  background: rgba(255,220,100,0.7); border-radius: 2px;
  animation: floatUp linear infinite;
}
.particle::before { content: '📦'; font-size: 12px; position: absolute; top: -4px; left: -4px; }
@keyframes floatUp {
  0% { transform: translateY(0) rotate(0deg); opacity: 0.8; }
  100% { transform: translateY(-80px) rotate(360deg); opacity: 0; }
}

/* 工人动画 */
.worker-container {
  position: absolute; left: 8%; bottom: 28%; z-index: 5;
  animation: workerBob 2s ease-in-out infinite;
}
.worker-svg { width: 130px; height: 220px; filter: drop-shadow(4px 8px 6px rgba(0,0,0,0.4)); }
.worker-shadow {
  width: 90px; height: 14px;
  background: rgba(0,0,0,0.3); border-radius: 50%;
  margin: -8px auto 0;
  animation: shadowPulse 2s ease-in-out infinite;
}
.worker-svg .leg-left-group {
  transform-box: fill-box; transform-origin: center top;
  animation: walkLeg 0.65s ease-in-out infinite alternate;
}
.worker-svg .leg-right-group {
  transform-box: fill-box; transform-origin: center top;
  animation: walkLeg 0.65s ease-in-out infinite alternate-reverse;
}
@keyframes walkLeg { from { transform: rotate(-22deg); } to { transform: rotate(22deg); } }
@keyframes workerBob { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-8px); } }
@keyframes shadowPulse {
  0%, 100% { transform: scaleX(1); opacity: 0.3; }
  50% { transform: scaleX(0.8); opacity: 0.2; }
}

/* 卡片 */
.login-card {
  position: relative;
  z-index: 10;
  width: 380px;
  background: rgba(255,255,255,0.95);
  border-radius: 20px;
  padding: 32px 36px 28px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.5), 0 0 0 1px rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
  animation: cardAppear 0.6s ease-out;
}
@keyframes cardAppear {
  from { opacity: 0; transform: translateY(30px) scale(0.95); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
.login-header { text-align: center; margin-bottom: 24px; }
.login-icon {
  font-size: 42px; margin-bottom: 8px;
  animation: iconSpin 6s linear infinite; display: inline-block;
}
@keyframes iconSpin {
  0%, 80%, 100% { transform: rotate(0deg); }
  85% { transform: rotate(-15deg); }
  90% { transform: rotate(15deg); }
  95% { transform: rotate(-5deg); }
}
.login-title { font-size: 22px; font-weight: 700; color: #2c3e50; margin: 0 0 4px; }
.login-subtitle { font-size: 12px; color: #95a5a6; margin: 0; letter-spacing: 1px; }

/* 表单 — 去掉 el-form-item 默认左偏移 */
.card-form { margin-top: 8px; }
.card-form >>> .el-form-item__content { margin-left: 0 !important; }

.full-btn {
  width: 100%;
  height: 42px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  border-radius: 21px;
  background: linear-gradient(135deg, #3498db, #2980b9);
  border: none;
  transition: all 0.3s;
}
.full-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #2980b9, #1a6fa8);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(52,152,219,0.4);
}

.switch-tip { text-align: center; margin-top: 12px; font-size: 13px; color: #888; }
.switch-tip a { color: #3498db; cursor: pointer; font-weight: 600; }
.switch-tip a:hover { text-decoration: underline; }

/* 滑块验证 */
.captcha-area { padding: 0 10px; }
.puzzle-track {
  position: relative; width: 268px; height: 44px;
  background: #f0f0f0; border-radius: 22px;
  border: 1px solid #ddd; overflow: hidden; margin: 0 auto;
}
.puzzle-bg {
  position: absolute; inset: 0;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; color: #bbb; user-select: none;
}
.puzzle-slider {
  position: absolute; top: 2px;
  width: 40px; height: 40px;
  background: linear-gradient(135deg, #3498db, #2980b9);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: white; font-size: 16px;
  cursor: grab; box-shadow: 0 2px 8px rgba(52,152,219,0.4);
  user-select: none;
}
.puzzle-slider:active { cursor: grabbing; }
</style>
