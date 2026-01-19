<template>
  <div class="simple-literature">
    <!-- Header -->
    <div class="header">
      <h1>医疗文献管理 - 知网爬取</h1>
      <div class="actions">
        <el-button type="primary" @click="showCrawlDialog = true">
          <el-icon><Plus /></el-icon>
          知网爬取
        </el-button>
        <el-button @click="loadLiteratureList">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- Search -->
    <div class="search">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索文献标题"
        style="width: 300px"
        @keyup.enter="loadLiteratureList"
      />
      <el-button type="primary" @click="loadLiteratureList">搜索</el-button>
    </div>

    <!-- Literature List -->
    <el-table 
      v-loading="tableLoading" 
      :data="literatureList" 
      style="width: 100%; margin-top: 20px;"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="300" show-overflow-tooltip />
      <el-table-column prop="authors" label="作者" width="150" show-overflow-tooltip />
      <el-table-column prop="journal" label="期刊" width="150" show-overflow-tooltip />
      <el-table-column prop="publishDate" label="发布日期" width="120" />
      <el-table-column prop="createTime" label="录入时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" type="primary" @click="editLiterature(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteLiterature(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- CNKI Crawl Dialog -->
    <el-dialog v-model="showCrawlDialog" title="知网文献爬取" width="500px">
      <el-form :model="crawlForm" label-width="80px">
        <el-form-item label="关键词">
          <el-input v-model="crawlForm.keyword" placeholder="请输入搜索关键词" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="crawlForm.count" :min="1" :max="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCrawlDialog = false">取消</el-button>
        <el-button type="primary" @click="startCrawl" :loading="crawlLoading">开始爬取</el-button>
      </template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog v-model="showDetailDialog" :title="currentLiterature?.title" width="70%" top="5vh">
      <div v-if="currentLiterature" class="literature-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ currentLiterature.id }}</el-descriptions-item>
          <el-descriptions-item label="标题">{{ currentLiterature.title }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ currentLiterature.authors }}</el-descriptions-item>
          <el-descriptions-item label="期刊">{{ currentLiterature.journal }}</el-descriptions-item>
          <el-descriptions-item label="发布日期">{{ currentLiterature.publishDate }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ currentLiterature.crawlSource }}</el-descriptions-item>
          <el-descriptions-item label="语言">{{ currentLiterature.language }}</el-descriptions-item>
          <el-descriptions-item label="录入时间">{{ currentLiterature.createTime }}</el-descriptions-item>
          <el-descriptions-item label="摘要" :span="2">{{ currentLiterature.abstractContent }}</el-descriptions-item>
          <el-descriptions-item label="关键词" :span="2">{{ currentLiterature.keywords }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- External Link -->
        <div style="margin-top: 20px;">
          <el-button type="primary" @click="openExternalLink(currentLiterature.sourceUrl)">
            <el-icon><Link /></el-icon>
            查看原文
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- Edit Dialog -->
    <el-dialog v-model="showEditDialog" title="编辑文献" width="60%">
      <el-form v-if="editForm" :model="editForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="editForm.authors" />
        </el-form-item>
        <el-form-item label="期刊">
          <el-input v-model="editForm.journal" />
        </el-form-item>
        <el-form-item label="发布日期">
          <el-input v-model="editForm.publishDate" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="editForm.abstractContent" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="editForm.keywords" />
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
import { Plus, Refresh, Link } from '@element-plus/icons-vue'
import request from '@/utils/request'

// Reactive data
const tableLoading = ref(false)
const crawlLoading = ref(false)
const showCrawlDialog = ref(false)
const showDetailDialog = ref(false)
const showEditDialog = ref(false)
const currentLiterature = ref(null)
const editForm = ref(null)
const searchKeyword = ref('')
const total = ref(0)

// Literature list
const literatureList = ref([])

// Crawl form
const crawlForm = reactive({
  keyword: '',
  count: 10
})

// Pagination
const pagination = reactive({
  pageNum: 1,
  pageSize: 10
})

// Load literature list
const loadLiteratureList = async () => {
  tableLoading.value = true
  try {
    const params = {
      keyword: searchKeyword.value,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    
    const response = await request.get('/medical-literature/list', { params })
    
    if (response.code === '200') {
      literatureList.value = response.data.list || []
      total.value = response.data.total || 0
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

// View detail
const viewDetail = async (row) => {
  try {
    const response = await request.get(`/medical-literature/${row.id}`)
    if (response.code === '200') {
      currentLiterature.value = response.data
      showDetailDialog.value = true
    }
  } catch (error) {
    console.error('获取文献详情失败:', error)
    ElMessage.error('获取文献详情失败')
  }
}

// Edit literature
const editLiterature = (row) => {
  editForm.value = { ...row }
  showEditDialog.value = true
}

// Save edit
const saveEdit = async () => {
  try {
    const response = await request.put('/medical-literature/update', editForm.value)
    if (response.code === '200') {
      ElMessage.success('更新成功')
      showEditDialog.value = false
      loadLiteratureList()
    } else {
      ElMessage.error(response.msg || '更新失败')
    }
  } catch (error) {
    console.error('更新失败:', error)
    ElMessage.error('更新失败')
  }
}

// Delete literature
const deleteLiterature = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文献《${row.title}》吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await request.delete(`/medical-literature/delete/${row.id}`)
    
    if (response.code === '200') {
      ElMessage.success('删除成功')
      loadLiteratureList()
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// Start crawling
const startCrawl = async () => {
  if (!crawlForm.keyword) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  crawlLoading.value = true
  try {
    const response = await request.post('/medical-literature/crawl', crawlForm)
    
    if (response.code === '200') {
      const result = response.data
      ElMessage.success(`知网爬取完成！成功保存 ${result.savedCount} 篇文献`)
      showCrawlDialog.value = false
      loadLiteratureList()
    } else {
      ElMessage.error(response.msg || '爬取失败')
    }
  } catch (error) {
    console.error('爬取失败:', error)
    ElMessage.error('爬取失败')
  } finally {
    crawlLoading.value = false
  }
}

// Open external link
const openExternalLink = (url) => {
  if (url) {
    window.open(url, '_blank')
  } else {
    ElMessage.warning('暂无原文链接')
  }
}

// Pagination handlers
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.pageNum = 1
  loadLiteratureList()
}

const handleCurrentChange = (val) => {
  pagination.pageNum = val
  loadLiteratureList()
}

// Load data on mount
onMounted(() => {
  loadLiteratureList()
})
</script>

<style scoped>
.simple-literature {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  margin: 0;
  color: #2c3e50;
}

.search {
  margin-bottom: 20px;
}

.search .el-input {
  margin-right: 10px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.literature-detail {
  max-height: 500px;
  overflow-y: auto;
}
</style>