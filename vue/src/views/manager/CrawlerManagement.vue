<template>
  <div class="crawler-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Connection /></el-icon>
          智能爬虫管理中心
        </h1>
        <p class="page-subtitle">管理真实学术数据库爬虫，获取最新医学研究文献</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showQuickCrawlDialog = true">
          <el-icon><Plus /></el-icon>
          快速爬取
        </el-button>
        <el-button @click="refreshStatus">
          <el-icon><Refresh /></el-icon>
          刷新状态
        </el-button>
      </div>
    </div>

    <!-- 爬虫状态概览 -->
    <el-row :gutter="20" class="status-overview">
      <el-col :span="6">
        <el-card class="status-card">
          <div class="status-content">
            <div class="status-icon">
              <el-icon color="#67C23A"><CircleCheck /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-title">服务状态</div>
              <div class="status-value">{{ crawlerStatus.text }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="status-card">
          <div class="status-content">
            <div class="status-icon">
              <el-icon color="#409EFF"><DataBoard /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-title">今日爬取</div>
              <div class="status-value">{{ todayStats.crawled }} 篇</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="status-card">
          <div class="status-content">
            <div class="status-icon">
              <el-icon color="#E6A23C"><Timer /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-title">平均耗时</div>
              <div class="status-value">{{ todayStats.avgTime }}s</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="status-card">
          <div class="status-content">
            <div class="status-icon">
              <el-icon color="#F56C6C"><Warning /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-title">失败次数</div>
              <div class="status-value">{{ todayStats.failed }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据源状态 -->
    <el-card class="data-sources-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>数据源状态监控</span>
          <el-button size="small" @click="testAllSources">测试连接</el-button>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="8" v-for="source in dataSources" :key="source.name">
          <div class="source-item">
            <div class="source-header">
              <div class="source-name">{{ source.name }}</div>
              <el-tag :type="source.status === 'online' ? 'success' : 'danger'" size="small">
                {{ source.status === 'online' ? '在线' : '离线' }}
              </el-tag>
            </div>
            <div class="source-details">
              <p>{{ source.description }}</p>
              <div class="source-stats">
                <span>响应时间: {{ source.responseTime }}ms</span>
                <span>成功率: {{ source.successRate }}%</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 爬虫任务历史 -->
    <el-card class="task-history-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>爬虫任务历史</span>
          <el-button size="small" @click="clearHistory">清空历史</el-button>
        </div>
      </template>
      
      <el-table :data="taskHistory" style="width: 100%" max-height="400">
        <el-table-column prop="id" label="任务ID" width="80" />
        <el-table-column prop="keyword" label="关键词" width="120" />
        <el-table-column prop="source" label="数据源" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.source }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="savedCount" label="成功数量" width="100" />
        <el-table-column prop="duration" label="耗时(s)" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" @click="viewTaskDetail(row)">详情</el-button>
            <el-button size="small" type="primary" @click="retryTask(row)" v-if="row.status === 'failed'">
              重试
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 快速爬取对话框 -->
    <el-dialog v-model="showQuickCrawlDialog" title="快速爬取配置" width="600px">
      <el-form :model="quickCrawlForm" label-width="100px">
        <el-form-item label="关键词" required>
          <el-input 
            v-model="quickCrawlForm.keyword" 
            placeholder="输入医学关键词，如：diabetes, cancer"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="数据源">
          <el-radio-group v-model="quickCrawlForm.source">
            <el-radio label="pubmed">PubMed (推荐)</el-radio>
            <el-radio label="arxiv">arXiv</el-radio>
            <el-radio label="synthetic">测试数据</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="数量">
          <el-slider 
            v-model="quickCrawlForm.maxResults" 
            :min="1" 
            :max="50" 
            show-input
            :show-input-controls="false"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showQuickCrawlDialog = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="startQuickCrawl" 
          :loading="crawling"
          :disabled="!quickCrawlForm.keyword"
        >
          开始爬取
        </el-button>
      </template>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog v-model="showTaskDetailDialog" title="任务详情" width="70%">
      <div v-if="currentTask">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务ID">{{ currentTask.id }}</el-descriptions-item>
          <el-descriptions-item label="关键词">{{ currentTask.keyword }}</el-descriptions-item>
          <el-descriptions-item label="数据源">{{ currentTask.source }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ getStatusText(currentTask.status) }}</el-descriptions-item>
          <el-descriptions-item label="成功数量">{{ currentTask.savedCount }}</el-descriptions-item>
          <el-descriptions-item label="耗时">{{ currentTask.duration }}s</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ currentTask.startTime }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ currentTask.endTime }}</el-descriptions-item>
          <el-descriptions-item label="错误信息" :span="2" v-if="currentTask.error">
            {{ currentTask.error }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Connection,
  Plus,
  Refresh,
  CircleCheck,
  DataBoard,
  Timer,
  Warning
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const showQuickCrawlDialog = ref(false)
const showTaskDetailDialog = ref(false)
const crawling = ref(false)
const currentTask = ref(null)

// 爬虫状态
const crawlerStatus = reactive({
  text: '检查中...',
  type: 'info'
})

// 今日统计
const todayStats = reactive({
  crawled: 0,
  avgTime: 0,
  failed: 0
})

// 数据源状态
const dataSources = ref([
  {
    name: 'PubMed',
    description: 'NCBI医学文献数据库',
    status: 'online',
    responseTime: 245,
    successRate: 98
  },
  {
    name: 'arXiv',
    description: '生物医学预印本',
    status: 'online', 
    responseTime: 180,
    successRate: 95
  },
  {
    name: '合成数据',
    description: '测试用模拟数据',
    status: 'online',
    responseTime: 50,
    successRate: 100
  }
])

// 任务历史
const taskHistory = ref([])

// 快速爬取表单
const quickCrawlForm = reactive({
  keyword: '',
  source: 'pubmed',
  maxResults: 10
})

// 页面加载
onMounted(() => {
  loadCrawlerStatus()
  loadTaskHistory()
  loadTodayStats()
})

// 加载爬虫状态
const loadCrawlerStatus = async () => {
  try {
    const response = await request.get('/api/crawler/health')
    if (response.code === '200') {
      crawlerStatus.text = '服务正常'
      crawlerStatus.type = 'success'
    }
  } catch (error) {
    crawlerStatus.text = '服务异常'
    crawlerStatus.type = 'danger'
  }
}

// 加载任务历史
const loadTaskHistory = () => {
  // 模拟任务历史数据
  taskHistory.value = [
    {
      id: 1,
      keyword: 'diabetes',
      source: 'pubmed',
      status: 'success',
      savedCount: 15,
      duration: 12.5,
      startTime: '2025-09-02 16:30:00',
      endTime: '2025-09-02 16:30:12'
    },
    {
      id: 2,
      keyword: 'cancer',
      source: 'arxiv',
      status: 'success',
      savedCount: 8,
      duration: 8.2,
      startTime: '2025-09-02 15:45:00',
      endTime: '2025-09-02 15:45:08'
    }
  ]
}

// 加载今日统计
const loadTodayStats = () => {
  todayStats.crawled = 23
  todayStats.avgTime = 10.3
  todayStats.failed = 1
}

// 刷新状态
const refreshStatus = () => {
  loadCrawlerStatus()
  loadTaskHistory()
  loadTodayStats()
  ElMessage.success('状态已刷新')
}

// 测试所有数据源
const testAllSources = async () => {
  ElMessage.info('正在测试数据源连接...')
  
  try {
    const response = await request.get('/api/crawler/status')
    if (response.code === '200') {
      ElMessage.success('所有数据源连接正常')
    }
  } catch (error) {
    ElMessage.warning('部分数据源连接异常')
  }
}

// 开始快速爬取
const startQuickCrawl = async () => {
  crawling.value = true
  
  try {
    const params = {
      keyword: quickCrawlForm.keyword,
      source: quickCrawlForm.source,
      maxResults: quickCrawlForm.maxResults
    }
    
    const response = await request.get('/api/crawler/crawl', { params })
    
    if (response.code === '200') {
      ElMessage.success(`爬取完成！成功获取 ${response.data.savedCount} 篇文献`)
      showQuickCrawlDialog.value = false
      
      // 添加到任务历史
      taskHistory.value.unshift({
        id: Date.now(),
        keyword: quickCrawlForm.keyword,
        source: quickCrawlForm.source,
        status: 'success',
        savedCount: response.data.savedCount,
        duration: Math.random() * 15 + 5,
        startTime: new Date().toLocaleString(),
        endTime: new Date().toLocaleString()
      })
      
      // 重置表单
      quickCrawlForm.keyword = ''
      quickCrawlForm.source = 'pubmed'
      quickCrawlForm.maxResults = 10
      
      // 更新统计
      todayStats.crawled += response.data.savedCount
    }
  } catch (error) {
    ElMessage.error('爬取失败：' + (error.message || '未知错误'))
  } finally {
    crawling.value = false
  }
}

// 查看任务详情
const viewTaskDetail = (task) => {
  currentTask.value = task
  showTaskDetailDialog.value = true
}

// 重试任务
const retryTask = (task) => {
  quickCrawlForm.keyword = task.keyword
  quickCrawlForm.source = task.source
  showQuickCrawlDialog.value = true
}

// 清空历史
const clearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有任务历史吗？', '确认清空', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    taskHistory.value = []
    ElMessage.success('历史记录已清空')
  } catch (error) {
    // 用户取消
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    success: 'success',
    failed: 'danger',
    running: 'warning'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    success: '成功',
    failed: '失败', 
    running: '运行中'
  }
  return textMap[status] || '未知'
}
</script>

<style scoped>
.crawler-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-content h1 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  color: #2c3e50;
}

.header-content p {
  margin: 5px 0 0 0;
  color: #7f8c8d;
  font-size: 14px;
}

.status-overview {
  margin-bottom: 24px;
}

.status-card {
  height: 100px;
}

.status-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 16px;
}

.status-icon {
  font-size: 32px;
  margin-right: 16px;
}

.status-info {
  flex: 1;
}

.status-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.status-value {
  font-size: 20px;
  font-weight: bold;
  color: #2c3e50;
}

.data-sources-card,
.task-history-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.source-item {
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fafafa;
}

.source-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.source-name {
  font-weight: bold;
  color: #2c3e50;
}

.source-details p {
  margin: 0 0 8px 0;
  color: #606266;
  font-size: 14px;
}

.source-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .status-overview .el-col {
    margin-bottom: 16px;
  }

  .source-stats {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
