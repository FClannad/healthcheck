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
/* ==================== 首页样式 ==================== */

/* 整体容器 */
.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-lg);
  min-height: calc(100vh - 64px);
}

/* 欢迎卡片 */
.welcome-card {
  width: 100%;
  max-width: 800px;
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  margin-bottom: var(--spacing-xl);
  text-align: center;
  animation: fadeIn 0.6s ease-out;
  padding: var(--spacing-xl);
  background: var(--gradient-primary);
  color: var(--text-inverse);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
  position: relative;
  overflow: hidden;
}

.welcome-card::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 60%);
  animation: shimmer 3s infinite;
}

@keyframes shimmer {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.welcome-icon {
  font-size: 28px;
  margin-right: var(--spacing-sm);
  vertical-align: middle;
}

.welcome-time {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-normal);
  margin-top: var(--spacing-sm);
  opacity: 0.9;
}

/* 公告容器 */
.notice-container {
  display: flex;
  justify-content: center;
  width: 100%;
}

/* 公告卡片 */
.notice-card {
  width: 100%;
  max-width: 800px;
  background-color: var(--background-white);
  padding: var(--spacing-xl);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-md);
  animation: fadeIn 0.6s ease-out 0.2s both;
  border: 1px solid var(--border-light);
}

/* 公告头部 */
.notice-header {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-lg);
  display: flex;
  align-items: center;
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--border-light);
}

.notice-header .el-icon {
  margin-right: var(--spacing-sm);
  color: var(--primary-color);
  font-size: 20px;
}

.refresh-btn {
  margin-left: auto;
  color: var(--text-secondary);
  font-size: var(--font-size-xs);
  transition: all var(--duration-fast) var(--ease-out);
}

.refresh-btn:hover {
  color: var(--primary-color);
}

/* 加载状态 */
.loading-container {
  padding: var(--spacing-xl) 0;
}

/* 空状态 */
.empty-container {
  padding: var(--spacing-xxl) 0;
  text-align: center;
}

/* 公告卡片 */
.announcement-card {
  margin-bottom: var(--spacing-md);
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-sm);
  transition: all var(--duration-fast) var(--ease-out);
  background-color: var(--background-light);
  border: 1px solid var(--border-light);
}

.announcement-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-lighter);
}

/* 公告内容 */
.announcement-content {
  font-size: var(--font-size-sm);
  color: var(--text-primary);
  line-height: var(--line-height-relaxed);
}

/* 时间轴项 */
.timeline-item {
  padding: 0;
  margin-bottom: var(--spacing-md);
}

/* 动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ==================== 白天模式白蓝主题 ==================== */

/* 白天模式欢迎卡片 - 蓝色渐变 */
:global(html:not(.dark-theme)) .welcome-card,
:global(.light-theme) .welcome-card {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 50%, #69c0ff 100%);
  box-shadow: 0 8px 24px rgba(24, 144, 255, 0.25);
}

:global(html:not(.dark-theme)) .welcome-card::before,
:global(.light-theme) .welcome-card::before {
  background: radial-gradient(circle, rgba(255,255,255,0.2) 0%, transparent 60%);
}

/* 白天模式公告卡片 - 白蓝色调 */
:global(html:not(.dark-theme)) .notice-card,
:global(.light-theme) .notice-card {
  background: #ffffff;
  border: 1px solid #e6f0fa;
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.08);
}

:global(html:not(.dark-theme)) .notice-header,
:global(.light-theme) .notice-header {
  color: #1e293b;
  border-bottom: 1px solid #e6f0fa;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  margin: calc(var(--spacing-xl) * -1);
  margin-bottom: var(--spacing-lg);
  padding: var(--spacing-lg) var(--spacing-xl);
  border-radius: var(--border-radius-lg) var(--border-radius-lg) 0 0;
}

:global(html:not(.dark-theme)) .notice-header .el-icon,
:global(.light-theme) .notice-header .el-icon {
  color: #1890ff;
}

/* 白天模式公告项卡片 */
:global(html:not(.dark-theme)) .announcement-card,
:global(.light-theme) .announcement-card {
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid #e6f0fa;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.06);
}

:global(html:not(.dark-theme)) .announcement-card:hover,
:global(.light-theme) .announcement-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(24, 144, 255, 0.12);
  border-color: #bae7ff;
  background: linear-gradient(180deg, #ffffff 0%, #f0f7ff 100%);
}

:global(html:not(.dark-theme)) .announcement-content,
:global(.light-theme) .announcement-content {
  color: #1e293b;
}

/* 白天模式时间轴样式 */
:global(html:not(.dark-theme)) :deep(.el-timeline-item__tail),
:global(.light-theme) :deep(.el-timeline-item__tail) {
  border-left-color: #bae7ff;
}

:global(html:not(.dark-theme)) :deep(.el-timeline-item__node),
:global(.light-theme) :deep(.el-timeline-item__node) {
  background-color: #1890ff;
  border-color: #1890ff;
}

:global(html:not(.dark-theme)) :deep(.el-timeline-item__timestamp),
:global(.light-theme) :deep(.el-timeline-item__timestamp) {
  color: #64748b;
}

/* ==================== 暗色主题适配 ==================== */

:global(.dark-theme) .notice-card {
  background-color: var(--background-white);
  border-color: var(--border-color);
}

:global(.dark-theme) .notice-header {
  color: var(--text-primary);
  border-bottom-color: var(--border-color);
}

:global(.dark-theme) .announcement-card {
  background-color: var(--background-light);
  border-color: var(--border-color);
}

:global(.dark-theme) .announcement-card:hover {
  border-color: var(--primary-color);
}

:global(.dark-theme) .announcement-content {
  color: var(--text-primary);
}

:global(.dark-theme) .refresh-btn {
  color: var(--text-secondary);
}

:global(.dark-theme) .refresh-btn:hover {
  color: var(--primary-light);
}

/* ==================== 响应式设计 ==================== */

@media (max-width: 768px) {
  .container {
    padding: var(--spacing-md);
  }
  
  .welcome-card {
    padding: var(--spacing-lg);
    font-size: var(--font-size-lg);
  }
  
  .notice-card {
    padding: var(--spacing-lg);
  }
}
</style>
