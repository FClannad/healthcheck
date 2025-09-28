<template>
  <div class="doctor-literature">
    <!-- 页面头部 -->
    <div class="doctor-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Reading /></el-icon>
          医疗文献阅读
        </h1>
        <p class="page-subtitle">浏览最新医疗文献，获取专业知识</p>
      </div>
    </div>

    <!-- 统计信息 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card class="stats-card" v-loading="statsLoading">
          <div class="stats-content">
            <div class="stats-number">{{ statsLoading ? '--' : stats.totalCount }}</div>
            <div class="stats-label">总文献数</div>
          </div>
          <el-icon class="stats-icon" color="#409EFF"><Document /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stats-card" v-loading="statsLoading">
          <div class="stats-content">
            <div class="stats-number">{{ statsLoading ? '--' : stats.todayCrawled }}</div>
            <div class="stats-label">今日更新</div>
          </div>
          <el-icon class="stats-icon" color="#67C23A"><Download /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stats-card" v-loading="statsLoading">
          <div class="stats-content">
            <div class="stats-number">{{ statsLoading ? '--' : stats.recentCount }}</div>
            <div class="stats-label">本周新增</div>
          </div>
          <el-icon class="stats-icon" color="#E6A23C"><Plus /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 文献浏览 -->
    <el-card class="literature-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>文献浏览</span>
          <el-button @click="refreshData" :loading="tableLoading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <!-- 搜索筛选 -->
      <div class="search-area">
        <el-form :model="searchForm" :inline="true">
          <el-form-item label="关键词">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索标题、作者、关键词"
              style="width: 250px"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="来源">
            <el-select v-model="searchForm.source" placeholder="选择来源" style="width: 120px" clearable>
              <el-option label="PubMed" value="PubMed" />
              <el-option label="arXiv" value="arXiv" />
              <el-option label="Crossref" value="Crossref" />
            </el-select>
          </el-form-item>
          <el-form-item label="期刊">
            <el-input
              v-model="searchForm.journal"
              placeholder="期刊名称"
              style="width: 150px"
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 文献列表 -->
      <el-table
        v-loading="tableLoading"
        :data="literatureList"
        style="width: 100%"
        :empty-text="tableLoading ? '加载中...' : '暂无数据'"
        @row-click="viewDetail"
        class="literature-table"
      >
        <el-table-column prop="title" label="标题" min-width="300" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="title-cell">
              <span class="title-text">{{ row.title }}</span>
              <el-tag v-if="isNewLiterature(row)" size="small" type="success">新</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="authors" label="作者" width="180" show-overflow-tooltip />
        <el-table-column prop="journal" label="期刊" width="150" show-overflow-tooltip />
        <el-table-column prop="crawlSource" label="来源" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getSourceType(row.crawlSource)">{{ row.crawlSource }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishDate" label="发布日期" width="120" />
        <el-table-column prop="createTime" label="录入时间" width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click.stop="viewDetail(row)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          :current-page="pagination.page"
          :page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 文献详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="文献详情"
      width="80%"
      :before-close="handleCloseDetail"
      class="literature-detail-dialog"
    >
      <div v-if="currentLiterature" class="literature-detail">
        <div class="detail-header">
          <h2 class="detail-title">{{ currentLiterature.title }}</h2>
          <div class="detail-meta">
            <el-tag type="info">{{ currentLiterature.crawlSource }}</el-tag>
            <span class="meta-item">作者：{{ currentLiterature.authors }}</span>
            <span class="meta-item">期刊：{{ currentLiterature.journal }}</span>
            <span class="meta-item">发布日期：{{ currentLiterature.publishDate }}</span>
          </div>
        </div>
        
        <div class="detail-content">
          <div class="content-section">
            <h3>摘要</h3>
            <p class="abstract-text">{{ currentLiterature.abstractContent || '暂无摘要' }}</p>
          </div>
          
          <div class="content-section" v-if="currentLiterature.keywords">
            <h3>关键词</h3>
            <div class="keywords">
              <el-tag
                v-for="keyword in getKeywordList(currentLiterature.keywords)"
                :key="keyword"
                class="keyword-tag"
              >
                {{ keyword }}
              </el-tag>
            </div>
          </div>
          
          <div class="content-section" v-if="currentLiterature.sourceUrl">
            <h3>原文链接</h3>
            <el-link :href="currentLiterature.sourceUrl" target="_blank" type="primary">
              <el-icon><Link /></el-icon>
              查看原文
            </el-link>
          </div>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button v-if="currentLiterature?.sourceUrl" type="primary" @click="openOriginalLink">
            <el-icon><Link /></el-icon>
            查看原文
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import request from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import {
  Reading, Document, Download, Plus, Refresh, Search, View, Link
} from '@element-plus/icons-vue'

// 响应式数据
const statsLoading = ref(false)
const tableLoading = ref(false)
const detailVisible = ref(false)
const currentLiterature = ref(null)

const stats = reactive({
  totalCount: 0,
  todayCrawled: 0,
  recentCount: 0
})

const searchForm = reactive({
  keyword: '',
  source: '',
  journal: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const literatureList = ref([])

// 方法
const refreshStats = async () => {
  statsLoading.value = true
  try {
    const [totalRes, todayRes, recentRes] = await Promise.all([
      request.get('/medical-literature/count'),
      request.get('/medical-literature/count-today'),
      request.get('/medical-literature/count-recent', { params: { days: 7 } })
    ])
    
    stats.totalCount = totalRes.data || 0
    stats.todayCrawled = todayRes.data || 0
    stats.recentCount = recentRes.data || 0
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  } finally {
    statsLoading.value = false
  }
}

const loadLiteratureList = async () => {
  tableLoading.value = true
  try {
    // 构建查询参数，确保参数格式正确
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.size
    }

    // 只添加非空的搜索参数
    if (searchForm.keyword && searchForm.keyword.trim()) {
      params.keyword = searchForm.keyword.trim()
    }
    if (searchForm.source && searchForm.source.trim()) {
      params.source = searchForm.source.trim()
    }
    if (searchForm.journal && searchForm.journal.trim()) {
      params.journal = searchForm.journal.trim()
    }

    console.log('Loading literature with params:', params)

    const response = await request.get('/medical-literature/list', { params })

    console.log('API Response:', response)

    if (response.code === '200') {
      literatureList.value = response.data.list || []
      pagination.total = response.data.total || 0
      console.log('Loaded literature list:', literatureList.value.length, 'items, total:', pagination.total)
    } else {
      ElMessage.error(response.msg || '获取文献列表失败')
      literatureList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取文献列表失败:', error)
    ElMessage.error('获取文献列表失败: ' + (error.message || '网络错误'))
    literatureList.value = []
    pagination.total = 0
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadLiteratureList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    source: '',
    journal: ''
  })
  pagination.page = 1
  loadLiteratureList()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadLiteratureList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadLiteratureList()
}

const viewDetail = (row) => {
  currentLiterature.value = row
  detailVisible.value = true
}

const handleCloseDetail = () => {
  detailVisible.value = false
  currentLiterature.value = null
}

const openOriginalLink = () => {
  if (currentLiterature.value?.sourceUrl) {
    window.open(currentLiterature.value.sourceUrl, '_blank')
  }
}

const refreshData = () => {
  loadLiteratureList()
  refreshStats()
}

const isNewLiterature = (row) => {
  if (!row.createTime) return false
  const createDate = new Date(row.createTime)
  const now = new Date()
  const diffTime = Math.abs(now - createDate)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays <= 3
}

const getSourceType = (source) => {
  const typeMap = {
    'PubMed': 'success',
    'arXiv': 'warning',
    'Crossref': 'info'
  }
  return typeMap[source] || 'info'
}

const getKeywordList = (keywords) => {
  if (!keywords) return []
  return keywords.split(',').map(k => k.trim()).filter(k => k)
}

// 生命周期
onMounted(() => {
  refreshStats()
  loadLiteratureList()
})
</script>

<style scoped>
@import '@/assets/styles/pages/doctor-literature.css';
</style>
