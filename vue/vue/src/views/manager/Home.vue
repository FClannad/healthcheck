<template>
  <div class="container">
    <!-- 欢迎卡片 -->
    <div class="welcome-card">
      <el-icon class="welcome-icon"><User /></el-icon>
      您好！{{ data.user?.name }}，欢迎使用本系统！
      <div class="welcome-time">{{ currentTime }}</div>
    </div>

    <div class="notice-container">
      <div class="notice-card">
        <div class="notice-header">
          <el-icon><Bell /></el-icon>
          系统公告
          <el-button
            type="text"
            size="small"
            @click="loadNotice"
            :loading="loading"
            class="refresh-btn">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="3" animated />
        </div>

        <!-- 空状态 -->
        <div v-else-if="!data.noticeData.length" class="empty-container">
          <el-empty description="暂无公告" />
        </div>

        <!-- 公告列表 -->
        <el-timeline v-else style="padding: 0;">
          <el-timeline-item
              v-for="(item, index) in data.noticeData"
              :key="index"
              :timestamp="item.time"
              class="timeline-item">
            <el-card class="announcement-card" shadow="hover">
              <div class="announcement-content">{{ item.content }}</div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";
import { User, Bell, Refresh } from '@element-plus/icons-vue';

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  noticeData: []
});

const loading = ref(false);
const currentTime = ref('');

// 更新当前时间
const updateTime = () => {
  const now = new Date();
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

// 加载公告
const loadNotice = async () => {
  loading.value = true;
  try {
    const res = await request.get('/notice/selectAll');
    if (res.code === '200') {
      data.noticeData = res.data || [];
      ElMessage.success('公告加载成功');
    } else {
      ElMessage.error(res.msg || '加载公告失败');
      data.noticeData = [];
    }
  } catch (error) {
    console.error('加载公告失败:', error);
    ElMessage.error('网络错误，请稍后重试');
    data.noticeData = [];
  } finally {
    loading.value = false;
  }
};

// 时间更新定时器
let timeInterval = null;

onMounted(() => {
  updateTime();
  timeInterval = setInterval(updateTime, 1000);
  loadNotice();
});

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval);
  }
});
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
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  position: relative;
}

.welcome-icon {
  font-size: 24px;
  margin-right: 8px;
  vertical-align: middle;
}

.welcome-time {
  font-size: 14px;
  font-weight: normal;
  margin-top: 8px;
  opacity: 0.9;
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
  color: #333;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.notice-header .el-icon {
  margin-right: 8px;
  color: #409eff;
}

.refresh-btn {
  color: #666;
  font-size: 12px;
}

.refresh-btn:hover {
  color: #409eff;
}

/* 加载状态 */
.loading-container {
  padding: 20px 0;
}

/* 空状态 */
.empty-container {
  padding: 40px 0;
  text-align: center;
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
