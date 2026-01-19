<template>
  <div class="medical-literature-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><Reading /></el-icon>
        医疗文献检索系统
      </h1>
      <p class="page-subtitle">为医生提供专业的医疗文献搜索与浏览服务</p>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>文献搜索</span>
        </div>
      </template>
      
      <el-form :model="searchForm" label-width="80px" :inline="true">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入标题、作者、关键词等"
            style="width: 300px"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" style="width: 200px" clearable>
            <el-option label="全部分类" value=""></el-option>
            <el-option label="心血管疾病" value="心血管疾病"></el-option>
            <el-option label="肿瘤学" value="肿瘤学"></el-option>
            <el-option label="神经科学" value="神经科学"></el-option>
            <el-option label="内分泌学" value="内分泌学"></el-option>
            <el-option label="消化系统" value="消化系统"></el-option>
            <el-option label="呼吸系统" value="呼吸系统"></el-option>
            <el-option label="泌尿系统" value="泌尿系统"></el-option>
            <el-option label="血液学" value="血液学"></el-option>
            <el-option label="免疫学" value="免疫学"></el-option>
            <el-option label="感染科" value="感染科"></el-option>
            <el-option label="儿科学" value="儿科学"></el-option>
            <el-option label="妇产科" value="妇产科"></el-option>
            <el-option label="外科学" value="外科学"></el-option>
            <el-option label="影像学" value="影像学"></el-option>
            <el-option label="药理学" value="药理学"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="语言">
          <el-select v-model="searchForm.language" placeholder="请选择语言" style="width: 120px" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="中文" value="中文"></el-option>
            <el-option label="英文" value="英文"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 高级搜索 -->
      <el-collapse v-model="activeNames" class="advanced-search">
        <el-collapse-item title="高级搜索" name="1">
          <el-form :model="searchForm" label-width="80px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="发布时间">
                  <el-date-picker
                    v-model="dateRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="期刊">
                  <el-input
                    v-model="searchForm.journal"
                    placeholder="请输入期刊名称"
                    clearable
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <!-- 快捷分类 -->
    <div class="quick-categories">
      <h3>热门分类</h3>
      <el-tag
        v-for="category in hotCategories"
        :key="category"
        class="category-tag"
        :type="searchForm.category === category ? 'primary' : ''"
        @click="selectCategory(category)"
      >
        {{ category }}
      </el-tag>
    </div>

    <!-- 搜索结果 -->
    <el-card class="result-card" shadow="hover">
      <template #header>
        <div class="result-header">
          <span>搜索结果 (共 {{ total }} 条)</span>
          <div class="sort-options">
            <el-select v-model="sortBy" placeholder="排序方式" style="width: 150px" @change="handleSort">
              <el-option label="相关度" value="relevance"></el-option>
              <el-option label="发布时间" value="publishDate"></el-option>
              <el-option label="浏览次数" value="viewCount"></el-option>
              <el-option label="影响因子" value="impact"></el-option>
            </el-select>
          </div>
        </div>
      </template>
      
      <div v-loading="loading" class="literature-list">
        <div
          v-for="item in literatureList"
          :key="item.id"
          class="literature-item"
          @click="showDetail(item)"
        >
          <div class="literature-header">
            <h3 class="literature-title">{{ item.title }}</h3>
            <div class="literature-meta">
              <el-tag size="small" type="primary">{{ item.crawlSource || '未知来源' }}</el-tag>
              <span class="meta-item">
                <el-icon><User /></el-icon>
                {{ item.authors || '未知作者' }}
              </span>
              <span class="meta-item">
                <el-icon><Calendar /></el-icon>
                {{ item.publishDate || '未知日期' }}
              </span>
            </div>
          </div>
          
          <div class="literature-content">
            <p class="literature-abstract">{{ truncateText(item.abstractContent, 200) }}</p>
            <div class="literature-info">
              <span class="info-item" v-if="item.journal">
                <el-icon><Document /></el-icon>
                期刊: {{ item.journal }}
              </span>
              <span class="info-item" v-if="item.keywords">
                <el-icon><Collection /></el-icon>
                关键词: {{ truncateText(item.keywords, 50) }}
              </span>
            </div>
          </div>
          
          <div class="literature-actions">
            <el-button size="small" type="primary" @click.stop="showDetail(item)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
            <el-button size="small" v-if="item.sourceUrl" @click.stop="openSource(item)">
              <el-icon><Link /></el-icon>
              查看原文
            </el-button>
            <el-button size="small" @click.stop="collectLiterature(item)">
              <el-icon><Star /></el-icon>
              收藏
            </el-button>
          </div>
        </div>
        
        <!-- 空状态 -->
        <el-empty v-if="!loading && literatureList.length === 0" description="暂无搜索结果">
          <el-button type="primary" @click="handleReset">重新搜索</el-button>
        </el-empty>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > 0">
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

    <!-- 文献详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="currentLiterature?.title"
      width="80%"
      top="5vh"
      destroy-on-close
    >
      <div v-if="currentLiterature" class="literature-detail">
        <div class="detail-header">
          <div class="detail-meta">
            <el-tag type="primary">{{ currentLiterature.crawlSource || '未知来源' }}</el-tag>
            <span class="detail-meta-item">创建时间: {{ formatDate(currentLiterature.createTime) }}</span>
          </div>
        </div>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="作者">{{ currentLiterature.authors }}</el-descriptions-item>
          <el-descriptions-item label="期刊">{{ currentLiterature.journal }}</el-descriptions-item>
          <el-descriptions-item label="发布日期">{{ currentLiterature.publishDate }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ currentLiterature.crawlSource }}</el-descriptions-item>
          <el-descriptions-item label="关键词" :span="2">
            <el-tag
              v-for="keyword in currentLiterature.keywords?.split(',')"
              :key="keyword"
              size="small"
              class="keyword-tag"
            >
              {{ keyword.trim() }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="detail-section">
          <h3>摘要</h3>
          <p class="abstract-content">{{ currentLiterature.abstractContent }}</p>
        </div>
        
        <div class="detail-actions">
          <el-button type="primary" v-if="currentLiterature.sourceUrl" @click="openSource(currentLiterature)">
            <el-icon><Link /></el-icon>
            查看原文
          </el-button>

          <el-button @click="collectLiterature(currentLiterature)">
            <el-icon><Star /></el-icon>
            收藏文献
          </el-button>
          <el-button @click="shareLiterature(currentLiterature)">
            <el-icon><Share /></el-icon>
            分享
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Reading,
  User,
  Calendar,
  Document,
  Collection,
  View,
  Star,
  Link,
  Share
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const loading = ref(false)
const activeNames = ref([])
const detailDialogVisible = ref(false)
const currentLiterature = ref(null)
const dateRange = ref([])
const sortBy = ref('relevance')
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  category: '',
  language: '',
  journal: '',
  startDate: '',
  endDate: ''
})

// 分页配置
const pagination = reactive({
  pageNum: 1,
  pageSize: 20
})

// 文献列表
const literatureList = ref([])

// 热门分类
const hotCategories = ref([
  '心血管疾病', '肿瘤学', '神经科学', '内分泌学', '消化系统',
  '呼吸系统', '儿科学', '妇产科', '影像学', '药理学'
])

// 监听日期范围变化
watch(dateRange, (newVal) => {
  if (newVal && newVal.length === 2) {
    searchForm.startDate = newVal[0]
    searchForm.endDate = newVal[1]
  } else {
    searchForm.startDate = ''
    searchForm.endDate = ''
  }
})

// 页面加载时获取数据
onMounted(() => {
  loadLiteratureList()
})

// 加载文献列表
const loadLiteratureList = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      sortBy: sortBy.value
    }
    
    const response = await request.get('/medical-literature/search', { params })
    
    if (response.code === '200') {
      literatureList.value = response.data.records || []
      total.value = response.data.total || 0
    } else {
      ElMessage.error(response.msg || '获取文献列表失败')
    }
  } catch (error) {
    console.error('获取文献列表失败:', error)
    ElMessage.error('获取文献列表失败')
  } finally {
    loading.value = false
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
  dateRange.value = []
  sortBy.value = 'relevance'
  pagination.pageNum = 1
  loadLiteratureList()
}

// 选择分类
const selectCategory = (category) => {
  searchForm.category = searchForm.category === category ? '' : category
  handleSearch()
}

// 排序处理
const handleSort = () => {
  pagination.pageNum = 1
  loadLiteratureList()
}

// 分页大小变化
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.pageNum = 1
  loadLiteratureList()
}

// 页码变化
const handleCurrentChange = (val) => {
  pagination.pageNum = val
  loadLiteratureList()
}

// 显示详情
const showDetail = async (literature) => {
  try {
    // 增加浏览次数
    await request.post(`/medical-literature/${literature.id}/view`)
    
    // 获取详细信息
    const response = await request.get(`/medical-literature/${literature.id}`)
    if (response.code === '200') {
      currentLiterature.value = response.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取文献详情失败:', error)
    ElMessage.error('获取文献详情失败')
  }
}

// PDF下载功能已移除

// 收藏文献
const collectLiterature = async (literature) => {
  try {
    await ElMessageBox.confirm(
      `确定要收藏文献《${literature.title}》吗？`,
      '收藏确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    // 这里可以调用收藏接口
    ElMessage.success('收藏成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('收藏失败:', error)
      ElMessage.error('收藏失败')
    }
  }
}

// 打开原文链接
const openSource = (literature) => {
  if (literature.sourceUrl) {
    window.open(literature.sourceUrl, '_blank')
  }
}

// 分享文献
const shareLiterature = (literature) => {
  // 复制分享链接到剪贴板
  const shareText = `《${literature.title}》 - ${literature.authors} - ${literature.journal}`
  navigator.clipboard.writeText(shareText).then(() => {
    ElMessage.success('分享信息已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 截断文本
const truncateText = (text, length) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '未知日期'
  try {
    return new Date(dateStr).toLocaleDateString('zh-CN')
  } catch {
    return dateStr
  }
}
</script>

<style scoped>
.medical-literature-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-title {
  font-size: 28px;
  color: #2c3e50;
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.page-subtitle {
  color: #7f8c8d;
  margin: 10px 0 0 0;
  font-size: 14px;
}

.search-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: bold;
  color: #2c3e50;
}

.advanced-search {
  margin-top: 20px;
}

.quick-categories {
  margin-bottom: 20px;
}

.quick-categories h3 {
  margin: 0 0 10px 0;
  color: #2c3e50;
  font-size: 16px;
}

.category-tag {
  margin: 0 8px 8px 0;
  cursor: pointer;
  transition: all 0.3s;
}

.category-tag:hover {
  transform: translateY(-2px);
}

.result-card {
  min-height: 500px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.literature-list {
  min-height: 400px;
}

.literature-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
}

.literature-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.literature-header {
  margin-bottom: 12px;
}

.literature-title {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 18px;
  line-height: 1.4;
}

.literature-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #7f8c8d;
  font-size: 13px;
}

.literature-content {
  margin-bottom: 16px;
}

.literature-abstract {
  color: #5f6368;
  line-height: 1.6;
  margin: 0 0 12px 0;
}

.literature-info {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #7f8c8d;
  font-size: 13px;
}

.literature-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.literature-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-header {
  margin-bottom: 20px;
}

.detail-meta {
  margin-bottom: 12px;
}

.detail-meta-item {
  color: #7f8c8d;
  font-size: 14px;
  margin-left: 12px;
}

.detail-stats {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #7f8c8d;
  font-size: 14px;
}

.keyword-tag {
  margin: 0 4px 4px 0;
}

.detail-section {
  margin: 20px 0;
}

.detail-section h3 {
  margin: 0 0 12px 0;
  color: #2c3e50;
  font-size: 16px;
}

.abstract-content {
  line-height: 1.8;
  color: #5f6368;
  text-align: justify;
}

.detail-actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .medical-literature-container {
    padding: 10px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .literature-item {
    padding: 16px;
  }
  
  .literature-meta,
  .literature-info {
    flex-direction: column;
    gap: 8px;
  }
  
  .detail-stats {
    flex-direction: column;
    gap: 8px;
  }
}
</style>