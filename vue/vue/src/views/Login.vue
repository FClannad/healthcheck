<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="login-background">
      <div class="background-shape shape-1"></div>
      <div class="background-shape shape-2"></div>
      <div class="background-shape shape-3"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- 品牌区域 -->
      <div class="brand-section">
        <div class="brand-logo">
          <el-icon size="48" color="var(--primary-color)">
            <Star />
          </el-icon>
        </div>
        <h1 class="brand-title">智慧医疗健康管理平台</h1>
        <p class="brand-subtitle">专业 · 智能 · 可信赖</p>
      </div>

      <!-- 表单区域 -->
      <div class="form-section">
        <el-form ref="formRef" :model="data.form" :rules="data.rules" class="login-form">
          <!-- 用户名输入 -->
          <el-form-item prop="username" class="form-item">
            <el-input
              v-model="data.form.username"
              placeholder="请输入用户名"
              size="large"
              class="login-input"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 密码输入 -->
          <el-form-item prop="password" class="form-item">
            <el-input
              v-model="data.form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              class="login-input"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 角色选择 -->
          <el-form-item prop="role" class="form-item">
            <el-select
              v-model="data.form.role"
              placeholder="请选择登录角色"
              size="large"
              class="login-select"
            >
              <el-option value="ADMIN" label="管理员">
                <div class="role-option">
                  <el-icon><Setting /></el-icon>
                  <span>管理员</span>
                </div>
              </el-option>
              <el-option value="DOCTOR" label="医生">
                <div class="role-option">
                  <el-icon><Star /></el-icon>
                  <span>医生</span>
                </div>
              </el-option>
              <el-option value="USER" label="用户">
                <div class="role-option">
                  <el-icon><User /></el-icon>
                  <span>用户</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>

          <!-- 登录按钮 -->
          <el-form-item class="form-item">
            <el-button
              type="primary"
              size="large"
              class="login-button"
              :loading="data.loading"
              @click="login"
            >
              <span v-if="!data.loading">登录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>

          <!-- 注册链接 -->
          <div class="register-link">
            还没有账号？<router-link to="/register" class="link">立即注册</router-link>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 页脚信息 -->
    <div class="login-footer">
      <p>&copy; 2025 智慧医疗健康管理平台. All rights reserved.</p>
    </div>
  </div>
</template>
<script setup>
import { reactive, ref } from "vue";
import { User, Lock, Setting, Star } from "@element-plus/icons-vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";


const data = reactive({
  form: { role: 'USER' },
  loading: false,
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
@import '@/assets/styles/pages/login.css';
</style>