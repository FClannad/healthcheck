<template>
  <div class="login-container">

    <div class="background-image"></div>

    <div class="login-box">
      <div class="login-title">请 登 录</div>
      <el-form ref="formRef" :model="data.form" :rules="data.rules">

        <el-form-item prop="username">
          <el-input :prefix-icon="User" size="large" v-model="data.form.username" placeholder="输入账号"></el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input show-password :prefix-icon="Lock" size="large" v-model="data.form.password" placeholder="输入密码"></el-input>
        </el-form-item>

        <el-form-item prop="role">
          <el-select size="large" v-model="data.form.role">
            <el-option value="ADMIN" label="管理员"></el-option>
            <el-option value="DOCTOR" label="医生"></el-option>
            <el-option value="USER" label="用户"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button size="large" type="primary" style="width: 100%" @click="login">登 录</el-button>
        </el-form-item>
        <div style="text-align: right">
          还没有账号？请 <a href="/register">注册</a>
        </div>
      </el-form>
    </div>
  </div>
</template>
<script setup>
import { reactive, ref } from "vue";
import { User, Lock } from "@element-plus/icons-vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";


const data = reactive({
  form: { role: 'USER' },
  rules: {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' }
    ]
  }
})

const formRef = ref()

const login = () => {
  formRef.value.validate(valid => {
    if (valid) {
      // 表示表单校验通过
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
        }
        else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}
</script>

<style scoped>
/* 背景图片 */
.background-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('@/assets/imgs/jing.jpg'); /* 替换为图片路径 */
  background-size: cover; /* 使图片覆盖整个容器 */
  background-position: center;
  z-index: -1; /* 确保图片在所有内容之下 */
}

/* 容器样式 */
.login-container {
  height: 100vh;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  background-color: rgba(0, 0, 0, 0.5);
  perspective: 1000px; /* 透视效果 */
}

/* 登录框样式 */
.login-box {
  width: 350px;
  padding: 30px;
  border-radius: 8px;
  background-color: rgba(255, 255, 255, 0.1);
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  transition: transform 0.1s ease-out; /* 平滑过渡 */
}

/* 标题样式 */
.login-title {
  font-weight: bold;
  font-size: 24px;
  text-align: center;
  margin-bottom: 30px;
  color: #1450aa; /* 蓝色 */
}

/* 表单项和按钮 */
.el-form-item {
  margin-bottom: 20px;
}

.el-button {
  width: 100%;
  background-color: #1450aa;
  border-color: #1450aa;
}

.el-button:hover {
  background-color: #114f8b;
  border-color: #114f8b;
}

a {
  color: #1450aa;
}

a:hover {
  color: #114f8b;
}
</style>