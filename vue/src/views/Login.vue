<template>
  <div class="login-page">
    <!-- 左侧视觉区 - 电影感医疗场景 -->
    <div class="login-visual">
      <!-- 背景图片层 -->
      <div class="visual-background">
        <div class="background-image"></div>
        <!-- 渐变叠加层 - 冷暖对比 -->
        <div class="gradient-overlay"></div>
      </div>
      
      <!-- 内容层 -->
      <div class="visual-content">
        <!-- 品牌标识 -->
        <div class="brand-mark">
          <div class="brand-icon">
            <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M24 4V44M4 24H44" stroke="currentColor" stroke-width="4" stroke-linecap="round"/>
              <circle cx="24" cy="24" r="20" stroke="currentColor" stroke-width="2" opacity="0.3"/>
            </svg>
          </div>
          <span class="brand-name">智慧医疗</span>
        </div>
        
        <!-- 主标语 -->
        <div class="visual-headline">
          <h1>科技守护健康</h1>
          <p class="tagline">用心呵护每一次诊疗</p>
        </div>
        
        <!-- 数据指标 -->
        <div class="stats-row">
          <div class="stat-item">
            <span class="stat-number">50,000+</span>
            <span class="stat-label">服务患者</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-number">1,200+</span>
            <span class="stat-label">专业医生</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-number">99.8%</span>
            <span class="stat-label">满意度</span>
          </div>
        </div>
      </div>
      
      <!-- 底部版权 -->
      <div class="visual-footer">
        <p>© 2025 智慧医疗健康管理平台 · 让医疗更智能</p>
      </div>
    </div>

    <!-- 右侧操作区 - 极简白色背景 -->
    <div class="login-form-section">
      <div class="form-wrapper">
        <!-- 移动端品牌标识 -->
        <div class="mobile-brand">
          <div class="mobile-brand-icon">
            <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M24 8V40M8 24H40" stroke="currentColor" stroke-width="4" stroke-linecap="round"/>
            </svg>
          </div>
          <span>智慧医疗</span>
        </div>

        <!-- 欢迎区域 -->
        <div class="welcome-section">
          <h2 class="welcome-title">欢迎回来</h2>
          <p class="welcome-subtitle">登录您的账户以继续使用服务</p>
        </div>

        <!-- 登录表单 -->
        <el-form 
          ref="formRef" 
          :model="data.form" 
          :rules="data.rules" 
          class="login-form"
          @keyup.enter="login"
        >
          <!-- 用户名输入 -->
          <div class="input-group">
            <label class="input-label">用户名</label>
            <el-form-item prop="username">
              <el-input
                v-model="data.form.username"
                placeholder="请输入您的用户名"
                size="large"
                class="minimal-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </div>

          <!-- 密码输入 -->
          <div class="input-group">
            <label class="input-label">密码</label>
            <el-form-item prop="password">
              <el-input
                v-model="data.form.password"
                type="password"
                placeholder="请输入您的密码"
                size="large"
                show-password
                class="minimal-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </div>

          <!-- 身份选择 -->
          <div class="input-group">
            <label class="input-label">登录身份</label>
            <el-form-item prop="role">
              <el-select
                v-model="data.form.role"
                placeholder="请选择您的身份"
                size="large"
                class="minimal-select"
              >
                <el-option
                  v-for="role in roles"
                  :key="role.value"
                  :label="role.label"
                  :value="role.value"
                >
                  <div class="role-option-item">
                    <el-icon><component :is="role.icon" /></el-icon>
                    <span>{{ role.label }}</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </div>

          <!-- 辅助选项 -->
          <div class="form-auxiliary">
            <el-checkbox v-model="data.rememberMe" class="remember-checkbox">
              记住我
            </el-checkbox>
            <a href="javascript:;" class="forgot-password">忘记密码？</a>
          </div>

          <!-- 登录按钮 -->
          <el-form-item class="submit-group">
            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="data.loading"
              @click="login"
            >
              {{ data.loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>

          <!-- 注册引导 -->
          <div class="register-guide">
            <span class="guide-text">还没有账号？</span>
            <router-link to="/register" class="register-link">立即注册</router-link>
          </div>
        </el-form>

        <!-- 底部信息 -->
        <div class="form-footer">
          <p class="footer-text">登录即表示您同意我们的 <a href="#">服务条款</a> 和 <a href="#">隐私政策</a></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { User, Lock, Setting, UserFilled } from "@element-plus/icons-vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";
import router from "@/router/index.js";

// 角色选项
const roles = [
  { value: 'USER', label: '普通用户', icon: UserFilled },
  { value: 'DOCTOR', label: '医生', icon: User },
  { value: 'ADMIN', label: '管理员', icon: Setting }
]

const data = reactive({
  form: { role: 'USER', username: '', password: '' },
  loading: false,
  rememberMe: false,
  rules: {
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' }
    ],
    role: [
      { required: true, message: '请选择登录角色', trigger: 'change' }
    ]
  }
})

const formRef = ref()

const login = () => {
  formRef.value.validate(valid => {
    if (valid) {
      data.loading = true
      console.log('发送的登录请求数据:', data.form);

      request.post('/login', data.form).then(res => {
        if (res.code === '200') {
          ElMessage.success('登录成功')
          // 存储用户信息到浏览器的缓存
          localStorage.setItem('xm-user', JSON.stringify(res.data))

          setTimeout(() => {
            if (res.data.role === 'ADMIN') {
              location.href = '/manager/dataAnalysis'
            } else {
              location.href = '/manager/home'
            }
          }, 500)
        } else {
          ElMessage.error(res.msg)
        }
      }).catch(error => {
        console.error('登录失败:', error)
        ElMessage.error('登录失败，请检查网络连接')
      }).finally(() => {
        data.loading = false
      })
    }
  })
}
</script>

<style scoped>
/* ========================================
   登录页面 - 电影感双栏布局
   左侧：高质量医疗场景视觉区
   右侧：极简白色表单操作区
   ======================================== */

.login-page {
  display: flex;
  min-height: 100vh;
  background: #ffffff;
}

/* ==================== 左侧视觉区 ==================== */

.login-visual {
  flex: 1.2;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px;
  overflow: hidden;
}

/* 背景图片层 */
.visual-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
}

.background-image {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  /* 使用本地医疗场景图片 - 专业医生形象 */
  background-image: url('@/assets/imgs/doctorn.jpeg');
  background-size: cover;
  background-position: center top;
  filter: brightness(1) saturate(1.1);
}

/* 渐变叠加层 - 冷暖色调对比（电影感） */
.gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  /* 深蓝色科技感（左下）+ 暖橙色人文关怀（右上）的电影级渐变 */
  background:
    /* 主渐变：冷暖对比 */
    linear-gradient(
      145deg,
      rgba(10, 25, 47, 0.75) 0%,
      rgba(20, 50, 90, 0.55) 35%,
      rgba(45, 90, 140, 0.35) 60%,
      rgba(255, 160, 80, 0.15) 100%
    ),
    /* 底部加深，增加层次感 */
    linear-gradient(
      to top,
      rgba(10, 20, 40, 0.6) 0%,
      transparent 40%
    ),
    /* 顶部微光效果 */
    linear-gradient(
      to bottom,
      rgba(100, 150, 200, 0.1) 0%,
      transparent 30%
    );
}

/* 内容层 */
.visual-content {
  position: relative;
  z-index: 1;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #ffffff;
}

/* 品牌标识 */
.brand-mark {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 80px;
}

.brand-icon {
  width: 42px;
  height: 42px;
  color: #ffffff;
}

.brand-name {
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 2px;
  color: rgba(255, 255, 255, 0.95);
}

/* 主标语 */
.visual-headline {
  margin-bottom: 60px;
}

.visual-headline h1 {
  font-size: 52px;
  font-weight: 700;
  line-height: 1.2;
  margin: 0 0 20px 0;
  letter-spacing: -1px;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.tagline {
  font-size: 20px;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
  letter-spacing: 1px;
}

/* 数据指标 */
.stats-row {
  display: flex;
  align-items: center;
  gap: 32px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: -0.5px;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 1px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
}

/* 底部版权 */
.visual-footer {
  position: relative;
  z-index: 1;
}

.visual-footer p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
  margin: 0;
  letter-spacing: 0.5px;
}

/* ==================== 右侧表单区 ==================== */

.login-form-section {
  flex: 0.8;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background: #ffffff;
  min-width: 480px;
}

.form-wrapper {
  width: 100%;
  max-width: 380px;
}

/* 移动端品牌标识 */
.mobile-brand {
  display: none;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 40px;
}

.mobile-brand-icon {
  width: 36px;
  height: 36px;
  color: #1e3a5f;
}

.mobile-brand span {
  font-size: 22px;
  font-weight: 700;
  color: #1e3a5f;
  letter-spacing: 1px;
}

/* 欢迎区域 */
.welcome-section {
  margin-bottom: 48px;
}

.welcome-title {
  font-size: 32px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 12px 0;
  letter-spacing: -0.5px;
}

.welcome-subtitle {
  font-size: 15px;
  color: #64748b;
  margin: 0;
  line-height: 1.6;
}

/* 表单样式 */
.login-form {
  width: 100%;
}

/* 输入组 */
.input-group {
  margin-bottom: 28px;
}

.input-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 10px;
  letter-spacing: 0.3px;
}

.input-group :deep(.el-form-item) {
  margin-bottom: 0;
}

/* 极简输入框样式 */
.minimal-input :deep(.el-input__wrapper) {
  padding: 0 16px;
  height: 52px;
  border-radius: 10px;
  box-shadow: none;
  border: 1.5px solid #e2e8f0;
  background: #f8fafc;
  transition: all 0.25s ease;
}

.minimal-input :deep(.el-input__wrapper:hover) {
  border-color: #94a3b8;
  background: #ffffff;
}

.minimal-input :deep(.el-input__wrapper.is-focus) {
  border-color: #1e3a5f;
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(30, 58, 95, 0.08);
}

.minimal-input :deep(.el-input__inner) {
  font-size: 15px;
  color: #1e293b;
}

.minimal-input :deep(.el-input__inner::placeholder) {
  color: #94a3b8;
}

.input-icon {
  font-size: 18px;
  color: #94a3b8;
}

/* 极简选择框样式 */
.minimal-select {
  width: 100%;
}

.minimal-select :deep(.el-select__wrapper) {
  padding: 0 16px;
  height: 52px;
  border-radius: 10px;
  box-shadow: none;
  border: 1.5px solid #e2e8f0;
  background: #f8fafc;
  transition: all 0.25s ease;
}

.minimal-select :deep(.el-select__wrapper:hover) {
  border-color: #94a3b8;
  background: #ffffff;
}

.minimal-select :deep(.el-select__wrapper.is-focused) {
  border-color: #1e3a5f;
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(30, 58, 95, 0.08);
}

.minimal-select :deep(.el-select__placeholder) {
  font-size: 15px;
  color: #94a3b8;
}

.minimal-select :deep(.el-select__selected-item) {
  font-size: 15px;
  color: #1e293b;
}

/* 角色选项样式 */
.role-option-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0;
  font-size: 14px;
  color: #374151;
}

.role-option-item .el-icon {
  font-size: 16px;
  color: #1e3a5f;
}

/* 辅助选项 */
.form-auxiliary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.remember-checkbox :deep(.el-checkbox__label) {
  font-size: 14px;
  color: #64748b;
}

.remember-checkbox :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #1e3a5f;
  border-color: #1e3a5f;
}

.forgot-password {
  font-size: 14px;
  color: #1e3a5f;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s ease;
}

.forgot-password:hover {
  color: #3b82f6;
}

/* 提交按钮 */
.submit-group {
  margin-bottom: 24px;
}

.submit-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #1e3a5f 0%, #2d5a87 100%);
  border: none;
  letter-spacing: 2px;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  background: linear-gradient(135deg, #2d5a87 0%, #3b82f6 100%);
  transform: translateY(-1px);
  box-shadow: 0 8px 24px rgba(30, 58, 95, 0.25);
}

.submit-btn:active {
  transform: translateY(0);
}

/* 注册引导 */
.register-guide {
  text-align: center;
  margin-bottom: 40px;
}

.guide-text {
  font-size: 14px;
  color: #64748b;
}

.register-link {
  font-size: 14px;
  color: #1e3a5f;
  font-weight: 600;
  text-decoration: none;
  margin-left: 6px;
  transition: color 0.2s ease;
}

.register-link:hover {
  color: #3b82f6;
}

/* 底部信息 */
.form-footer {
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid #f1f5f9;
}

.footer-text {
  font-size: 12px;
  color: #94a3b8;
  margin: 0;
  line-height: 1.8;
}

.footer-text a {
  color: #64748b;
  text-decoration: none;
  transition: color 0.2s ease;
}

.footer-text a:hover {
  color: #1e3a5f;
}

/* ==================== 表单验证错误样式 ==================== */

:deep(.el-form-item.is-error) .minimal-input .el-input__wrapper {
  border-color: #ef4444;
}

:deep(.el-form-item.is-error) .minimal-select .el-select__wrapper {
  border-color: #ef4444;
}

:deep(.el-form-item__error) {
  font-size: 12px;
  color: #ef4444;
  margin-top: 8px;
  padding-left: 2px;
}

/* ==================== 响应式设计 ==================== */

@media (max-width: 1200px) {
  .login-visual {
    flex: 1;
    padding: 40px;
  }
  
  .login-form-section {
    flex: 1;
    min-width: 420px;
    padding: 40px;
  }
  
  .visual-headline h1 {
    font-size: 42px;
  }
}

@media (max-width: 1024px) {
  .login-visual {
    display: none;
  }
  
  .login-form-section {
    flex: none;
    width: 100%;
    min-width: auto;
    min-height: 100vh;
    padding: 32px 24px;
    /* 移动端添加淡雅背景 */
    background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
  }
  
  .form-wrapper {
    max-width: 400px;
  }
  
  .mobile-brand {
    display: flex;
  }
  
  .welcome-section {
    text-align: center;
    margin-bottom: 40px;
  }
  
  .welcome-title {
    font-size: 28px;
  }
}

@media (max-width: 480px) {
  .login-form-section {
    padding: 24px 20px;
  }
  
  .form-wrapper {
    max-width: none;
  }
  
  .welcome-title {
    font-size: 24px;
  }
  
  .welcome-subtitle {
    font-size: 14px;
  }
  
  .input-group {
    margin-bottom: 24px;
  }
  
  .minimal-input :deep(.el-input__wrapper),
  .minimal-select :deep(.el-select__wrapper) {
    height: 48px;
  }
  
  .submit-btn {
    height: 48px;
    font-size: 15px;
  }
}

/* ==================== 动画优化 ==================== */

@media (prefers-reduced-motion: reduce) {
  .submit-btn:hover {
    transform: none;
  }
}
</style>
