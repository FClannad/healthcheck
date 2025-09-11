<template>
  <div class="medical-literature-admin">
    <!-- 页面头部 -->
    <div class="admin-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Reading /></el-icon>
          医疗文献管理系统
        </h1>
        <p class="page-subtitle">管理医疗文献爬虫服务与文献数据</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showCrawlerDialog = true">
          <el-icon><Plus /></el-icon>
          手动爬虫
        </el-button>
        <el-button @click="refreshStats" :loading="statsLoading">
          <el-icon><Refresh /></el-icon>
          刷新统计
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stats-card" v-loading="statsLoading">
          <div class="stats-content">
            <div class="stats-number">{{ statsLoading ? '--' : stats.totalCount }}</div>
            <div class="stats-label">总文献数</div>
          </div>
          <el-icon class="stats-icon" color="#409EFF"><Document /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card" v-loading="statsLoading">
          <div class="stats-content">
            <div class="stats-number">{{ statsLoading ? '--' : stats.todayCrawled }}</div>
            <div class="stats-label">今日爬取</div>
          </div>
          <el-icon class="stats-icon" color="#67C23A"><Download /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card" v-loading="statsLoading">
          <div class="stats-content">
            <div class="stats-number">{{ statsLoading ? '--' : stats.totalViews }}</div>
            <div class="stats-label">总浏览量</div>
          </div>
          <el-icon class="stats-icon" color="#E6A23C"><View /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card" v-loading="statsLoading">
          <div class="stats-content">
            <div class="stats-number">{{ statsLoading ? '--' : stats.totalDownloads }}</div>
            <div class="stats-label">总下载量</div>
          </div>
          <el-icon class="stats-icon" color="#F56C6C"><Download /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 爬虫状态监控 -->
    <el-card class="monitor-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>爬虫服务监控</span>
          <el-tag :type="crawlerStatus.type">{{ crawlerStatus.text }}</el-tag>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="monitor-item">
            <h4>定时任务状态</h4>
            <p>下次执行时间: {{ crawlerStatus.nextRunTime }}</p>
            <p>上次执行时间: {{ crawlerStatus.lastRunTime }}</p>
            <p>执行结果: {{ crawlerStatus.lastResult }}</p>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="monitor-item">
            <h4>数据源状态</h4>
            <div class="source-status">
              <div class="source-item">
                <span>PubMed (NCBI)</span>
                <el-tag size="small" type="success">正常</el-tag>
              </div>
              <div class="source-item">
                <span>Crossref DOI</span>
                <el-tag size="small" type="success">正常</el-tag>
              </div>
              <div class="source-item">
                <span>arXiv Biomedical</span>
                <el-tag size="small" type="success">正常</el-tag>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 文献管理 -->
    <el-card class="literature-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>文献列表管理</span>
        </div>
      </template>
      
      <!-- 搜索筛选 -->
      <div class="search-area">
        <el-form :model="searchForm" :inline="true">
          <el-form-item label="关键词">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索标题、作者、关键词"
              style="width: 200px"
              clearable
            />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="searchForm.category" placeholder="选择分类" style="width: 150px" clearable>
              <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
            </el-select>
          </el-form-item>
          <el-form-item label="来源">
            <el-select v-model="searchForm.source" placeholder="选择来源" style="width: 120px" clearable>
              <el-option label="PubMed" value="PubMed" />
              <el-option label="Crossref" value="Crossref" />
              <el-option label="arXiv" value="arXiv" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="选择状态" style="width: 120px" clearable>
              <el-option label="正常" value="active" />
              <el-option label="已删除" value="deleted" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 文献列表 -->
      <el-table
        v-loading="tableLoading"
        :data="literatureList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        :empty-text="tableLoading ? '加载中...' : '暂无数据'"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="authors" label="作者" width="150" show-overflow-tooltip />
        <el-table-column prop="journal" label="期刊" width="120" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getCategoryType(row.category)">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="crawlSource" label="来源" width="80">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.crawlSource }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishDate" label="发布日期" width="120" />
        <el-table-column prop="createTime" label="录入时间" width="160" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
              {{ row.status === 'active' ? '正常' : '已删除' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">查看</el-button>
            <el-button size="small" type="primary" @click="editLiterature(row)">编辑</el-button>
            <el-button 
              size="small" 
              :type="row.status === 'active' ? 'danger' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 'active' ? '删除' : '恢复' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作 -->
      <div class="batch-actions" v-if="selectedItems.length > 0">
        <span>已选择 {{ selectedItems.length }} 项</span>
        <el-button size="small" type="danger" @click="batchDelete">批量删除</el-button>
        <el-button size="small" @click="batchExport">批量导出</el-button>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 手动爬虫对话框 -->
    <el-dialog v-model="showCrawlerDialog" title="文献爬虫配置" width="700px">
      <div class="crawler-intro">
        <el-alert
          title="文献爬虫"
          description="本系统已集成真实的学术数据库API，可以获取最新的医学研究文献"
          type="info"
          :closable="false"
          show-icon
        />
      </div>

      <el-form :model="crawlerForm" label-width="100px" style="margin-top: 20px;">
        <el-form-item label="关键词" required>
          <el-input
            v-model="crawlerForm.keyword"
            placeholder="输入搜索关键词，如：diabetes, cancer, covid-19"
            style="width: 100%"
            clearable
          />
          <div class="form-tip">支持英文关键词，多个关键词用空格分隔</div>
        </el-form-item>

        <el-form-item label="数据源">
          <el-select v-model="crawlerForm.source" placeholder="选择数据源" style="width: 100%">
            <el-option label="PubMed (NCBI 医学数据库) - 推荐" value="pubmed">
              <div>
                <div>PubMed (NCBI 医学数据库)</div>
                <div style="font-size: 12px; color: #999;">权威医学文献数据库，包含最新研究</div>
              </div>
            </el-option>
            <el-option label="arXiv (生物医学预印本)" value="arxiv">
              <div>
                <div>arXiv (生物医学预印本)</div>
                <div style="font-size: 12px; color: #999;">最新预印本论文，更新速度快</div>
              </div>
            </el-option>
            <el-option label="多源混合 (推荐)" value="">
              <div>
                <div>多源混合爬取</div>
                <div style="font-size: 12px; color: #999;">同时从多个数据源获取，结果更全面</div>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="爬取数量">
          <el-input-number
            v-model="crawlerForm.maxResults"
            :min="1"
            :max="50"
            style="width: 100%"
            controls-position="right"
          />
          <div class="form-tip">建议每次爬取1-20篇，避免请求过多</div>
        </el-form-item>

        <el-form-item label="预期分类">
          <el-select v-model="crawlerForm.category" placeholder="选择预期分类（可选）" style="width: 100%" clearable>
            <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
          </el-select>
          <div class="form-tip">系统会自动分类，此选项仅作参考</div>
        </el-form-item>
      </el-form>

      <div class="crawler-status" v-if="crawlerLoading">
        <el-progress :percentage="crawlerProgress" :status="crawlerProgressStatus">
          <template #default="{ percentage }">
            <span class="percentage-value">{{ Math.floor(percentage) }}%</span>
            <span class="percentage-label">{{ crawlerProgressText }}</span>
          </template>
        </el-progress>
      </div>

      <template #footer>
        <el-button @click="showCrawlerDialog = false" :disabled="crawlerLoading">取消</el-button>
        <el-button
          type="primary"
          @click="startManualCrawl"
          :loading="crawlerLoading"
          :disabled="!crawlerForm.keyword || !crawlerForm.source"
        >
          <el-icon><Download /></el-icon>
          开始爬取真实数据
        </el-button>
      </template>
    </el-dialog>

    <!-- 文献详情对话框 -->
    <el-dialog v-model="showDetailDialog" :title="currentLiterature?.title" width="80%" top="5vh">
      <div v-if="currentLiterature" class="literature-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ currentLiterature.id }}</el-descriptions-item>
          <el-descriptions-item label="DOI">{{ currentLiterature.doi || 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ currentLiterature.authors }}</el-descriptions-item>
          <el-descriptions-item label="期刊">{{ currentLiterature.journal }}</el-descriptions-item>
          <el-descriptions-item label="发布日期">{{ currentLiterature.publishDate }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ currentLiterature.category }}</el-descriptions-item>
          <el-descriptions-item label="语言">{{ currentLiterature.language }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ currentLiterature.crawlSource }}</el-descriptions-item>
          <el-descriptions-item label="影响因子">{{ currentLiterature.impact || 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="浏览次数">{{ currentLiterature.viewCount }}</el-descriptions-item>
          <el-descriptions-item label="下载次数">{{ currentLiterature.downloadCount }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ currentLiterature.status }}</el-descriptions-item>
          <el-descriptions-item label="关键词" :span="2">{{ currentLiterature.keywords }}</el-descriptions-item>
          <el-descriptions-item label="摘要" :span="2">{{ currentLiterature.abstractContent }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-actions" style="margin-top: 20px; text-align: center;">
          <el-button type="primary" v-if="currentLiterature.sourceUrl" @click="openSource(currentLiterature)">
            <el-icon><Link /></el-icon>
            查看原文
          </el-button>
          <el-button v-if="currentLiterature.pdfUrl" @click="downloadPdf(currentLiterature)">
            <el-icon><Download /></el-icon>
            下载PDF
          </el-button>
          <el-button @click="copyLiteratureInfo(currentLiterature)">
            <el-icon><CopyDocument /></el-icon>
            复制信息
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 编辑文献对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑文献" width="60%" top="5vh">
      <el-form v-if="currentLiterature" :model="currentLiterature" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="currentLiterature.title" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="currentLiterature.authors" />
        </el-form-item>
        <el-form-item label="期刊">
          <el-input v-model="currentLiterature.journal" />
        </el-form-item>
        <el-form-item label="发布日期">
          <el-input v-model="currentLiterature.publishDate" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="currentLiterature.category" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="currentLiterature.abstractContent" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="currentLiterature.keywords" />
        </el-form-item>
        <el-form-item label="原文链接">
          <el-input v-model="currentLiterature.sourceUrl" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Reading,
  Plus,
  Refresh,
  Document,
  Download,
  View,
  Link,
  CopyDocument
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const tableLoading = ref(false)
const statsLoading = ref(false)
const crawlerLoading = ref(false)
const showCrawlerDialog = ref(false)
const showDetailDialog = ref(false)
const showEditDialog = ref(false)
const currentLiterature = ref(null)
const total = ref(0)
const selectedItems = ref([])

// 统计数据
const stats = reactive({
  totalCount: 0,
  todayCrawled: 0,
  totalViews: 0,
  totalDownloads: 0
})

// 爬虫状态
const crawlerStatus = reactive({
  type: 'success',
  text: '真实爬虫运行正常',
  nextRunTime: '明天 06:00:00',
  lastRunTime: '今天 06:00:00',
  lastResult: '成功爬取真实数据 15 篇文献'
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  category: '',
  source: '',
  status: ''
})

// 爬虫配置
const crawlerForm = reactive({
  keyword: '',
  source: 'pubmed',
  category: '',
  maxResults: 10
})

// 爬虫进度
const crawlerProgress = ref(0)
const crawlerProgressStatus = ref('')
const crawlerProgressText = ref('')

// 分页配置
const pagination = reactive({
  pageNum: 1,
  pageSize: 20
})

// 文献列表
const literatureList = ref([])

// 分类列表 - 符合真实医学数据库的分类
const categories = ref([
  '心血管疾病', '肿瘤学', '神经科学', '内分泌学', '消化系统',
  '呼吸系统', '泌尿系统', '血液学', '免疫学', '感染科',
  '儿科学', '妇产科', '外科学', '影像学', '药理学',
  '精神病学', '皮肤病学', '眼科学', '耳鼻喉科', '骨科学'
])

// 页面加载时获取数据
onMounted(() => {
  loadLiteratureList()
  loadStats()
  loadCrawlerStatus()
})

// 加载文献列表 - 支持真实数据显示
const loadLiteratureList = async () => {
  tableLoading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    
    console.log('正在加载文献列表...', params)
    
    const response = await request.get('/medical-literature/admin/list', { params })
    
    console.log('文献列表响应:', response)
    
    if (response.code === '200') {
      literatureList.value = response.data.list || []
      total.value = response.data.total || 0
      
      console.log(`成功加载 ${literatureList.value.length} 篇文献，共 ${total.value} 篇`)
      
      // 确保显示真实数据的来源信息
      literatureList.value.forEach(lit => {
        if (!lit.crawlSource) {
          lit.crawlSource = '未知来源'
        }
        if (!lit.category) {
          lit.category = '未分类'
        }
      })
    } else {
      ElMessage.error(response.msg || '获取文献列表失败')
    }
  } catch (error) {
    console.error('获取文献列表失败:', error)
    ElMessage.error('获取文献列表失败')
  } finally {
    tableLoading.value = false
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    const response = await request.get('/medical-literature/admin/stats')
    
    if (response.code === '200') {
      Object.assign(stats, response.data)
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 加载真实爬虫状态
const loadCrawlerStatus = async () => {
  try {
    console.log('加载真实爬虫状态...')

    // 调用新版爬虫状态API
    const response = await request.get('/api/crawler/v2/status')

    if (response.code === '200') {
      const status = response.data

      console.log('爬虫状态响应:', status)

      // 更新爬虫状态显示
      Object.assign(crawlerStatus, {
        type: status.enabled ? 'success' : 'warning',
        text: status.enabled ? '新版爬虫服务运行正常' : '爬虫服务已禁用',
        nextRunTime: '随时可用 (手动触发)',
        lastRunTime: new Date().toLocaleString(),
        lastResult: `支持数据源: ${status.sources?.join(', ') || 'arxiv, pubmed'}`,
        crawlerType: 'SIMPLIFIED_CRAWLER_V2'
      })

      console.log('爬虫状态已更新:', crawlerStatus)
    } else {
      console.warn('获取爬虫状态失败:', response.msg)

      // 设置警告状态
      Object.assign(crawlerStatus, {
        type: 'warning',
        text: '爬虫服务状态未知',
        lastResult: response.msg || '无法获取详细状态'
      })
    }
  } catch (error) {
    console.error('获取真实爬虫状态失败:', error)

    // 设置错误状态
    Object.assign(crawlerStatus, {
      type: 'danger',
      text: '爬虫服务连接失败',
      lastResult: '请检查爬虫服务是否正常运行'
    })
  }
}

// 刷新统计
const refreshStats = async () => {
  try {
    statsLoading.value = true
    await Promise.all([loadStats(), loadCrawlerStatus()])
    ElMessage.success('统计数据已刷新')
  } catch (error) {
    console.error('刷新统计失败:', error)
    ElMessage.error('刷新统计失败，请稍后重试')
  } finally {
    statsLoading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.pageNum = 1
  loadLiteratureList()
}

// 重置搜索
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  pagination.pageNum = 1
  loadLiteratureList()
}

// 分页处理
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.pageNum = 1
  loadLiteratureList()
}

const handleCurrentChange = (val) => {
  pagination.pageNum = val
  loadLiteratureList()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedItems.value = selection
}

// 查看详情
const viewDetail = async (row) => {
  try {
    // 使用 detail 接口，不增加浏览次数
    const response = await request.get(`/medical-literature/detail/${row.id}`)
    if (response.code === '200') {
      currentLiterature.value = response.data
      showDetailDialog.value = true
    }
  } catch (error) {
    console.error('获取文献详情失败:', error)
    ElMessage.error('获取文献详情失败')
  }
}

// 编辑文献
const editLiterature = (row) => {
  currentLiterature.value = { ...row }
  showEditDialog.value = true
}

// 保存编辑
const saveEdit = async () => {
  try {
    const response = await request.put('/medical-literature/update', currentLiterature.value)

    if (response.code === '200') {
      ElMessage.success('文献更新成功')
      showEditDialog.value = false
      loadLiteratureList()
    } else {
      ElMessage.error(response.msg || '文献更新失败')
    }
  } catch (error) {
    console.error('文献更新失败:', error)
    ElMessage.error('文献更新失败')
  }
}

// 切换状态
const toggleStatus = async (row) => {
  const action = row.status === 'active' ? '删除' : '恢复'
  const newStatus = row.status === 'active' ? 'deleted' : 'active'
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}文献《${row.title}》吗？`,
      `${action}确认`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await request.put(`/medical-literature/${row.id}/status`, { status: newStatus })
    
    if (response.code === '200') {
      ElMessage.success(`${action}成功`)
      loadLiteratureList()
    } else {
      ElMessage.error(response.msg || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${action}失败:`, error)
      ElMessage.error(`${action}失败`)
    }
  }
}

// 批量删除
const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedItems.value.length} 条文献吗？`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const ids = selectedItems.value.map(item => item.id)
    const response = await request.delete('/medical-literature/batch', { data: { ids } })
    
    if (response.code === '200') {
      ElMessage.success('批量删除成功')
      loadLiteratureList()
    } else {
      ElMessage.error(response.msg || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 批量导出
const batchExport = () => {
  ElMessage.info('导出功能开发中...')
}

// 手动爬虫 - 使用真实数据爬取
const startManualCrawl = async () => {
  if (!crawlerForm.keyword || !crawlerForm.source) {
    ElMessage.warning('请输入关键词并选择数据源')
    return
  }

  crawlerLoading.value = true
  crawlerProgress.value = 0
  crawlerProgressStatus.value = ''
  crawlerProgressText.value = '准备开始爬取...'

  try {
    console.log('发起真实爬虫请求:', crawlerForm)

    // 模拟进度更新
    const progressInterval = setInterval(() => {
      if (crawlerProgress.value < 90) {
        crawlerProgress.value = Math.floor(crawlerProgress.value + Math.random() * 20)
        if (crawlerProgress.value < 30) {
          crawlerProgressText.value = '连接数据源...'
        } else if (crawlerProgress.value < 60) {
          crawlerProgressText.value = '搜索文献...'
        } else if (crawlerProgress.value < 90) {
          crawlerProgressText.value = '解析数据...'
        }
      }
    }, 500)

    // 调用真实爬虫API
    const params = {
      keyword: crawlerForm.keyword,
      source: crawlerForm.source,
      maxResults: crawlerForm.maxResults
    }

    const response = await request.get('/api/crawler/v2/crawl', { params })

    clearInterval(progressInterval)
    crawlerProgress.value = 100
    crawlerProgressText.value = '爬取完成！'
    crawlerProgressStatus.value = 'success'

    console.log('爬虫响应:', response)

    if (response.code === '200') {
      const result = response.data

      // 显示详细的成功信息
      ElMessage.success({
        message: `新版爬虫完成！关键词: ${result.keyword}, 找到: ${result.found} 篇, 保存: ${result.saved} 篇文献`,
        duration: 5000,
        showClose: true
      })

      // 延迟关闭对话框，让用户看到完成状态
      setTimeout(() => {
        showCrawlerDialog.value = false

        // 重置表单
        crawlerForm.keyword = ''
        crawlerForm.source = ''
        crawlerForm.category = ''
        crawlerForm.maxResults = 10

        // 刷新数据
        loadLiteratureList()
        loadStats()
        loadCrawlerStatus()
      }, 2000)

    } else {
      clearInterval(progressInterval)
      crawlerProgress.value = 0
      crawlerProgressStatus.value = 'exception'
      crawlerProgressText.value = '爬取失败'

      ElMessage.error(response.msg || '真实爬虫失败')
    }
  } catch (error) {
    console.error('真实爬虫失败:', error)

    crawlerProgress.value = 0
    crawlerProgressStatus.value = 'exception'
    crawlerProgressText.value = '爬取失败'

    let errorMsg = '真实爬虫执行失败'
    if (error.response) {
      errorMsg += `: ${error.response.data.msg || error.response.statusText}`
    } else if (error.message) {
      errorMsg += `: ${error.message}`
    }

    ElMessage.error({
      message: errorMsg,
      duration: 5000,
      showClose: true
    })
  } finally {
    crawlerLoading.value = false
  }
}

// 获取分类标签类型
const getCategoryType = (category) => {
  const typeMap = {
    '心血管疾病': 'danger',
    '肿瘤学': 'warning',
    '神经科学': 'primary',
    '内分泌学': 'success',
    '消化系统': 'info'
  }
  return typeMap[category] || ''
}

// 打开原文链接
const openSource = (literature) => {
  if (literature.sourceUrl) {
    window.open(literature.sourceUrl, '_blank')
    ElMessage.success('已在新窗口打开原文')
  } else {
    ElMessage.warning('该文献暂无原文链接')
  }
}

// 下载PDF
const downloadPdf = (literature) => {
  if (literature.pdfUrl) {
    window.open(literature.pdfUrl, '_blank')
    ElMessage.success('开始下载PDF')
  } else {
    ElMessage.warning('该文献暂无PDF下载链接')
  }
}

// 复制文献信息
const copyLiteratureInfo = async (literature) => {
  const info = `标题：${literature.title}
作者：${literature.authors}
期刊：${literature.journal}
发布日期：${literature.publishDate}
DOI：${literature.doi || 'N/A'}
原文链接：${literature.sourceUrl || 'N/A'}
摘要：${literature.abstractContent}`

  try {
    await navigator.clipboard.writeText(info)
    ElMessage.success('文献信息已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败，请手动复制')
  }
}
</script>

<style scoped>
.medical-literature-admin {
  padding: 20px;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  position: relative;
  overflow: hidden;
}

.stats-content {
  text-align: center;
}

.stats-number {
  font-size: 32px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 8px;
}

.stats-label {
  color: #7f8c8d;
  font-size: 14px;
}

.stats-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 48px;
  opacity: 0.1;
}

.monitor-card,
.literature-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.monitor-item h4 {
  margin: 0 0 10px 0;
  color: #2c3e50;
}

.monitor-item p {
  margin: 5px 0;
  color: #7f8c8d;
  font-size: 14px;
}

.source-status {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.source-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-area {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.batch-actions {
  margin-top: 16px;
  padding: 12px;
  background: #f0f9ff;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.literature-detail {
  max-height: 70vh;
  overflow-y: auto;
}

/* 爬虫对话框样式 */
.crawler-intro {
  margin-bottom: 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}

.crawler-status {
  margin: 20px 0;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.percentage-value {
  font-weight: bold;
  margin-right: 8px;
}

.percentage-label {
  font-size: 12px;
  color: #909399;
}

/* 数据源选项样式 */
.el-select-dropdown__item {
  height: auto !important;
  padding: 8px 20px !important;
}

.el-select-dropdown__item > div {
  line-height: 1.4;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .stats-row .el-col {
    margin-bottom: 16px;
  }

  .search-area .el-form {
    flex-direction: column;
  }

  .search-area .el-form-item {
    margin-right: 0;
    margin-bottom: 16px;
  }
}
</style>