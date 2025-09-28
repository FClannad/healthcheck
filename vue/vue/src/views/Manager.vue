<template>
  <div class="manager-container">
    <!-- 顶部导航栏 -->
    <el-header class="manager-header">
      <!-- 左侧品牌区域 -->
      <div class="manager-header-left">
        <div class="logo">
          <el-icon size="36" color="var(--primary-color)">
            <Star />
          </el-icon>
        </div>
        <div class="title">智慧医疗管理平台</div>
      </div>

      <!-- 中间导航区域 -->
      <div class="manager-header-center">
        <el-menu
          :default-active="router.currentRoute.value.path"
          mode="horizontal"
          router
          class="main-menu"
          :collapse="false"
          :ellipsis="false"
        >
          <!-- 系统首页 -->
          <el-menu-item index="/manager/home" v-if="data.user.role !== 'ADMIN'">
            <el-icon><House /></el-icon>
            <span>系统首页</span>
          </el-menu-item>

          <!-- 数据统计 -->
          <el-menu-item index="/manager/dataAnalysis" v-if="data.user.role === 'ADMIN'">
            <el-icon><PieChart /></el-icon>
            <span>数据统计</span>
          </el-menu-item>

          <!-- 医院简介 -->
          <el-menu-item index="/manager/brief">
            <el-icon><Location /></el-icon>
            <span>医院简介</span>
          </el-menu-item>

          <!-- 健康小贴士 -->
          <el-menu-item index="/manager/userInformation" v-if="data.user.role !== 'ADMIN'">
            <el-icon><Folder /></el-icon>
            <span>健康小贴士</span>
          </el-menu-item>

          <!-- 预约体检 -->
          <el-menu-item index="/manager/userPhysicalExamination" v-if="data.user.role === 'USER'">
            <el-icon><CreditCard /></el-icon>
            <span>预约体检</span>
          </el-menu-item>

          <!-- 体检套餐 -->
          <el-menu-item index="/manager/userExaminationPackage" v-if="data.user.role === 'USER'">
            <el-icon><Wallet /></el-icon>
            <span>体检套餐</span>
          </el-menu-item>

          <!-- AI健康咨询 -->
          <el-menu-item index="/manager/aiConsultation" v-if="data.user.role === 'USER'">
            <el-icon><ChatDotRound /></el-icon>
            <span>AI健康咨询</span>
          </el-menu-item>

          <!-- 日程安排 -->
          <el-menu-item index="/manager/calendar" v-if="data.user.role === 'DOCTOR'">
            <el-icon><Calendar /></el-icon>
            <span>日程安排</span>
          </el-menu-item>

          <!-- 医疗文献 -->
          <el-menu-item index="/manager/doctorLiterature" v-if="data.user.role === 'DOCTOR'">
            <el-icon><Reading /></el-icon>
            <span>医疗文献</span>
          </el-menu-item>

          <!-- 体检预约订单 -->
          <el-menu-item index="/manager/examinationOrder">
            <el-icon><Document /></el-icon>
            <span>体检预约订单</span>
          </el-menu-item>

          <!-- 反馈和建议 -->
          <el-menu-item index="/manager/userFeedback" v-if="data.user.role === 'USER'">
            <el-icon><ChatDotRound /></el-icon>
            <span>反馈和建议</span>
          </el-menu-item>

          <!-- 信息管理子菜单 -->
          <el-sub-menu v-if="data.user.role === 'ADMIN'" index="1">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>信息管理</span>
            </template>
            <el-menu-item index="/manager/examinationType">体检类型</el-menu-item>
            <el-menu-item index="/manager/physicalExamination">体检项目</el-menu-item>
            <el-menu-item index="/manager/examinationPackage">套餐体检项目</el-menu-item>
            <el-menu-item index="/manager/information">健康小知识</el-menu-item>
            <el-menu-item index="/manager/medicalLiterature">医疗文献管理</el-menu-item>
          </el-sub-menu>

          <!-- 系统管理子菜单 -->
          <el-sub-menu v-if="data.user.role === 'ADMIN'" index="2">
            <template #title>
              <el-icon><Tools /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/manager/notice">公告信息管理</el-menu-item>
            <el-menu-item index="/manager/feedBack">用户反馈信息</el-menu-item>
            <el-menu-item index="/manager/title">医生职称信息</el-menu-item>
            <el-menu-item index="/manager/office">医生科室信息</el-menu-item>
          </el-sub-menu>

          <!-- 用户管理子菜单 -->
          <el-sub-menu v-if="data.user.role === 'ADMIN'" index="3">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/manager/admin">管理员信息</el-menu-item>
            <el-menu-item index="/manager/user">用户个人信息</el-menu-item>
            <el-menu-item index="/manager/doctor">医生信息</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>

      <!-- 右侧用户区域 -->
      <div class="manager-header-right">
        <!-- 夜间模式切换 -->
        <div class="theme-toggle" @click="toggleTheme">
          <el-icon size="20">
            <Sunny v-if="!isDarkMode" />
            <Moon v-else />
          </el-icon>
        </div>

        <!-- 通知图标 -->
        <div class="notification-icon">
          <el-icon size="20">
            <Bell />
          </el-icon>
          <div class="notification-badge"></div>
        </div>

        <!-- 用户信息下拉菜单 -->
        <el-dropdown class="user-dropdown" trigger="click">
          <div class="user-info">
            <div class="user-avatar">
              {{ data.user.name ? data.user.name.charAt(0).toUpperCase() : 'U' }}
            </div>
            <div class="user-details">
              <div class="user-name">{{ data.user.name || '用户' }}</div>
              <div class="user-role">{{ getRoleText(data.user.role) }}</div>
            </div>
            <el-icon size="16" color="var(--text-secondary)">
              <ArrowDown />
            </el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/manager/person')">
                <el-icon><User /></el-icon>
                个人资料
              </el-dropdown-item>
              <el-dropdown-item @click="router.push('/manager/password')">
                <el-icon><Lock /></el-icon>
                修改密码
              </el-dropdown-item>
              <el-dropdown-item divided @click="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <!-- 主要内容区域 -->
    <div class="manager-main">
      <div class="manager-main-content">
        <RouterView @updateUser="updateUser" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import router from "@/router/index.js";
import { ElMessage } from "element-plus";
import {
  Star,
  Bell,
  ArrowDown,
  User,
  Lock,
  SwitchButton,
  Setting,
  Tools,
  UserFilled,
  House,
  PieChart,
  Location,
  Folder,
  CreditCard,
  Wallet,
  Calendar,
  Reading,
  Document,
  ChatDotRound,
  Sunny,
  Moon
} from "@element-plus/icons-vue";

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}')
})

// 夜间模式状态
const isDarkMode = ref(false)

// 初始化主题
onMounted(() => {
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme === 'dark') {
    isDarkMode.value = true
    document.documentElement.classList.add('dark-theme')
  }
})

// 切换主题
const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value
  if (isDarkMode.value) {
    document.documentElement.classList.add('dark-theme')
    localStorage.setItem('theme', 'dark')
  } else {
    document.documentElement.classList.remove('dark-theme')
    localStorage.setItem('theme', 'light')
  }
}

// 获取角色文本
const getRoleText = (role) => {
  const roleMap = {
    'ADMIN': '管理员',
    'DOCTOR': '医生',
    'USER': '用户'
  }
  return roleMap[role] || '未知角色'
}

const logout = () => {
  localStorage.removeItem('xm-user')
  router.push('/login')
  ElMessage.success('已退出登录')
}

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('xm-user') || '{}')
}

if (!data.user.id) {
  logout()
  ElMessage.error('请登录！')
}
</script>

<style scoped>
@import "@/assets/styles/layouts/manager.css";
</style>