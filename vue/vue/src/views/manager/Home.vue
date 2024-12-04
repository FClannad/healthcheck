<template>
  <div class="container">
    <!-- 欢迎卡片 -->
    <div class="welcome-card">
      您好！{{ data.user?.name }}，欢迎使用本系统！
    </div>

    <div class="notice-container" >
      <div class="notice-card" >
        <div class="notice-header">系统公告</div>
        <el-timeline style="padding: 0; ">
          <el-timeline-item
              v-for="(item, index) in data.noticeData"
              :key="index"
              :timestamp="item.time"
              class="timeline-item">
            <el-card class="announcement-card" shadow="hover" >
              <div class="announcement-content" >{{ item.content }}</div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  noticeData: []
});

const loadNotice = () => {
  request.get('/notice/selectAll').then(res => {
    if (res.code === '200') {
      data.noticeData = res.data;
    } else {
      ElMessage.error(res.msg);
    }
  });
};

loadNotice();
</script>

<style scoped>

.welcome-card{
  font-size: 20px;
  font-weight: bold;
  color: black;
  margin-bottom: 20px;
  text-align: center;
  text-transform: uppercase;
  animation: fadeIn 1s ease;
  overflow: hidden;
}
/* 整体容器 */
.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  font-family: 'Comic Sans MS', sans-serif;
  background-color: rgba(240, 248, 255, 0.1); /* 透明背景 */
}

/* 公告容器 */
.notice-container {
  display: flex;
  justify-content: center;
  width: 100%;
}

/* 公告卡片 */
.notice-card {
  width: 80%;
  max-width: 600px;
  background-color: #ffffff;
  padding: 20px;
  border-radius: 12px;
  background-color: rgba(240, 248, 255, 0.5); /* 透明背景 */
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  animation: fadeIn 1s ease;
  overflow: hidden;
}

/* 卡片内容 */
.notice-header {
  font-size: 20px;
  font-weight: bold;
  color: black;
  margin-bottom: 20px;
  text-align: center;
  text-transform: uppercase;
}

/* 公告卡片动画效果 */
.announcement-card {
  margin-bottom: 15px;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease; /* 平滑过渡 */
  padding: 15px;
  background-color: #f9f9f9; /* 默认背景 */
}

/* 鼠标悬停效果 */
.announcement-card:hover {
  transform: scale(1.1); /* 放大效果 */
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.2); /* 放大时的阴影效果 */
  background-color: #999999; /* 背景颜色变为明亮黄色 */
  background-color: rgba(240, 248, 255, 0.8); /* 透明背景 */
  z-index: 1; /* 确保卡片在放大时显示在前面 */
}

/* 卡片内容 */
.announcement-content {
  font-size: 14px;
  color: black;
  line-height: 1.5;
  text-align: justify;
}

/* 时间轴项 */
.timeline-item {
  padding: 0;
  margin-bottom: 20px;
}

.timeline-item .el-timeline-item-dot {
  border-color:black; /* 卡通风格的橙色 */
}

/* 弹出效果 */
@keyframes fadeIn {
  0% { opacity: 0; }
  100% { opacity: 1; }
}
.timeline-item .el-timeline-item-tail {
  background-color:black; /* 卡通风格的橙色 */
}

</style>
