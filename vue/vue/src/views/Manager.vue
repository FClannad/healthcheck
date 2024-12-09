<template>
  <!--头部-->
  <div class="manager-container">

    <div id="app">
      <TimeTooltip />
    </div>

    <el-header class="manager-header" >
      <div class="manager-header-left">
        <img src="@/assets/imgs/logo.jpeg" class="logo">
        <div class="title" style="color: #222b40">体检管理系统</div>
      </div>
      <div class="manager-header-center" >
        <el-menu :default-active="router.currentRoute.value.path"
                 mode="horizontal"
                 router
                 style="border: none; background-color: transparent;"
        >
          <el-menu-item index="/manager/home" v-if="data.user.role !== 'ADMIN'" >
            <el-icon><HomeFilled /></el-icon>
            <span style="color: black">系统首页</span>
          </el-menu-item>

          <el-menu-item index="/manager/dataAnalysis"  v-if="data.user.role === 'ADMIN'">
            <el-icon><TrendCharts /></el-icon>
            <span style="color: black">数据统计</span>
          </el-menu-item>

          <el-menu-item index="/manager/brief" >
            <el-icon><Pointer /></el-icon>
            <span style="color: black">医院简介</span>
          </el-menu-item>

          <el-menu-item index="/manager/userInformation" v-if="data.user.role !== 'ADMIN'">
            <el-icon><Collection /></el-icon>
            <span style="color: black">健康小贴士</span>
          </el-menu-item>

          <el-menu-item index="/manager/userPhysicalExamination" v-if="data.user.role === 'USER'">
            <el-icon><Wallet /></el-icon>
            <span style="color: black">预约体检</span>
          </el-menu-item>

          <el-menu-item index="/manager/userExaminationPackage" v-if="data.user.role === 'USER'">
            <el-icon><WalletFilled /></el-icon>
            <span style="color: black">体检套餐</span>
          </el-menu-item>

          <el-menu-item index="/manager/calendar" v-if="data.user.role === 'DOCTOR'">
            <el-icon><Calendar /></el-icon>
            <span style="color: black">日程安排</span>
          </el-menu-item>

          <el-menu-item index="/manager/examinationOrder" >
            <el-icon><Document /></el-icon>
            <span style="color: black">体检预约订单</span>
          </el-menu-item>

          <el-menu-item index="/manager/userFeedback" v-if="data.user.role === 'USER'" >
            <el-icon><ChatDotRound /></el-icon>
            <span style="color: black">反馈和建议</span>
          </el-menu-item>

          <el-sub-menu v-if="data.user.role === 'ADMIN'" index="1"  style="border: none; background-color: transparent; height: 50px">
            <template #title>
              <el-icon><Menu /></el-icon>
              <span style="height: 100% ;color: black">信息管理</span>
            </template>
            <el-menu-item index="/manager/examinationType" >体检类型</el-menu-item>
            <el-menu-item index="/manager/physicalExamination">体检项目</el-menu-item>
            <el-menu-item index="/manager/examinationPackage">套餐体检项目</el-menu-item>
            <el-menu-item index="/manager/information">健康小知识</el-menu-item>
          </el-sub-menu>

          <el-sub-menu v-if="data.user.role === 'ADMIN'" index="2"  style="border: none; background-color: transparent; height: 50px">
            <template #title>
              <el-icon><Menu /></el-icon>
              <span style="color: black">系统管理</span>
            </template>
            <el-menu-item index="/manager/notice">公告信息管理</el-menu-item>
            <el-menu-item index="/manager/feedBack">用户反馈信息</el-menu-item>
            <el-menu-item index="/manager/title">医生职称信息</el-menu-item>
            <el-menu-item index="/manager/office">医生科室信息</el-menu-item>
          </el-sub-menu>

          <el-sub-menu v-if="data.user.role === 'ADMIN'" index="3"  style="border: none; background-color: transparent; height: 50px">
            <template #title>
              <el-icon><Menu /></el-icon>
              <span style="color: black">用户管理</span>
            </template>
            <el-menu-item index="/manager/admin" >管理员信息</el-menu-item>
            <el-menu-item index="/manager/user">用户个人信息</el-menu-item>
            <el-menu-item index="/manager/doctor">医生信息</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>
      <div class="manager-header-right">
        <el-dropdown style="cursor: pointer">
          <div style="padding-right: 20px; display: flex; align-items: center">
            <img style="width: 40px; height: 40px; border-radius: 50%;" :src="data.user.avatar" alt="">
            <span style="margin-left: 5px; color: black">{{ data.user.name }}</span><el-icon color="#fff"><arrow-down /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/manager/person')">个人资料</el-dropdown-item>
              <el-dropdown-item @click="router.push('/manager/password')">修改密码</el-dropdown-item>
              <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <div style="display: flex">
      <div class="manager-main-right" style="background-color: #fff; padding: 10px; flex-grow: 1;">
        <RouterView @updateUser="updateUse" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import router from "@/router/index.js";
import {ElMessage} from "element-plus";
import TimeTooltip from '@/views/manager/TimeTooltip.vue';

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}')
})

const logout = () => {
  localStorage.removeItem('xm-user')
  router.push('/login')
}

const updateUser = () => {
  data.user =  JSON.parse(localStorage.getItem('xm-user') || '{}')
}

if (!data.user.id) {
  logout()
  ElMessage.error('请登录！')
}
</script>

<style scoped>
@import "@/assets/css/manager.css";

/* 修改 el-menu 的背景颜色 */
.el-menu-item {
  color: #ecf0f1;  /* 修改菜单项默认文字颜色 */
}


#app {
  position: relative;
  width: 100%;
  height: 100%;
  background-color: #f0f0f0;
  z-index: 9999;  /* 确保它位于页面的最上层 */
}

.manager-container{
  position: relative;
  width: 100%;
  height: 100%; /* 高度设为视口高度，也可以根据需要调整 */
  overflow: hidden;
}


.manager-main-right {
  background-image: url('@/assets/imgs/background3.jpg');
  background-size: cover;
  animation: backgroundBrightness 10s ease-in-out infinite;
  padding: 10px;
  min-height: 800px;
  flex-grow: 1;
}

.manager-header {
  background-image: url('@/assets/imgs/background3.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  height: 80px;
  color: black;
  padding: 10px;
}

.manager-header-left {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 10px;
  background-image: url('@/assets/imgs/background3.jpg'); /* 背景图片 */
  background-size: cover; /* 背景覆盖整个区域 */
  background-position: center; /* 背景居中 */
  width: 220px;
  height: 80px; /* 根据需要调整高度 */
}

.logo {
  width: 60px; /* 控制 logo 大小 */
  height: 60px; /* 控制 logo 大小 */
  border-radius: 50%; /* 让 logo 成圆形 */
  border: 3px solid rgba(255, 255, 255, 0.8); /* 给 logo 增加边框 */
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); /* 增加轻微阴影 */
  margin-right: 20px; /* logo 与标题之间的间距 */
  opacity: 0.9; /* 稍微调整透明度，避免 logo 遮挡背景 */
  transition: transform 0.3s ease; /* 平滑过渡效果 */
}

.manager-header-left:hover .logo {
  transform: scale(1.15); /* 鼠标悬停时放大 logo */
}

.title {
  font-size: 24px;
  color: white;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3); /* 给标题添加阴影，以提高可读性 */
}

@media (max-width: 768px) {
  .logo {
    width: 50px;
    height: 50px;
  }
  .title {
    font-size: 20px;
    color: black;
  }
}

@media (max-width: 480px) {
  .logo {
    width: 40px;
    height: 40px;
  }
  .title {
    font-size: 18px;
  }
}



</style>