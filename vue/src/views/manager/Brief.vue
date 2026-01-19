<template>
  <div class="medical-platform">
    <!-- 顶部介绍区域 -->
    <section class="hero-section">
      <div class="container">
        <div class="hero-content">
          <h1 class="hero-title">智慧医疗健康管理平台</h1>
          <p class="hero-subtitle">基于人工智能和大数据技术的综合性医疗信息化平台</p>
          <p class="hero-description">
            为医疗机构、医生和患者提供全方位的智慧医疗服务，推动医疗行业的数字化转型，
            提升医疗质量和效率，构建智慧医疗新生态。
          </p>
          <div class="hero-stats">
            <div class="stat-item">
              <div class="stat-number">500+</div>
              <div class="stat-label">合作医院</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">10万+</div>
              <div class="stat-label">医生用户</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">99.9%</div>
              <div class="stat-label">系统稳定性</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 核心功能模块 -->
    <section class="features-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">核心功能模块</h2>
          <p class="section-subtitle">四大智能模块，构建完整医疗生态系统</p>
        </div>
        <div class="features-grid">
          <div class="feature-card" v-for="feature in features" :key="feature.id">
            <div class="feature-icon">
              <component :is="feature.icon" />
            </div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-description">{{ feature.description }}</p>
            <div class="feature-tags">
              <span v-for="tag in feature.tags" :key="tag" class="feature-tag">{{ tag }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 技术架构 -->
    <section class="tech-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">技术架构</h2>
          <p class="section-subtitle">基于现代化技术栈，确保系统高性能和可扩展性</p>
        </div>
        <div class="tech-grid">
          <div class="tech-category">
            <h4 class="tech-title">前端技术</h4>
            <div class="tech-tags">
              <span class="tech-tag">Vue 3</span>
              <span class="tech-tag">Element Plus</span>
              <span class="tech-tag">TypeScript</span>
              <span class="tech-tag">Vite</span>
            </div>
          </div>
          <div class="tech-category">
            <h4 class="tech-title">后端服务</h4>
            <div class="tech-tags">
              <span class="tech-tag">Spring Boot</span>
              <span class="tech-tag">MyBatis Plus</span>
              <span class="tech-tag">Redis</span>
              <span class="tech-tag">RabbitMQ</span>
            </div>
          </div>
          <div class="tech-category">
            <h4 class="tech-title">AI技术</h4>
            <div class="tech-tags">
              <span class="tech-tag">机器学习</span>
              <span class="tech-tag">自然语言处理</span>
              <span class="tech-tag">知识图谱</span>
              <span class="tech-tag">深度学习</span>
            </div>
          </div>
          <div class="tech-category">
            <h4 class="tech-title">基础设施</h4>
            <div class="tech-tags">
              <span class="tech-tag">Docker</span>
              <span class="tech-tag">Kubernetes</span>
              <span class="tech-tag">MySQL</span>
              <span class="tech-tag">Elasticsearch</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 平台优势 -->
    <section class="advantages-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">平台优势</h2>
          <p class="section-subtitle">专业、智能、安全、高效的医疗信息化解决方案</p>
        </div>
        <div class="advantages-grid">
          <div class="advantage-card" v-for="advantage in advantages" :key="advantage.id">
            <div class="advantage-icon">
              <component :is="advantage.icon" />
            </div>
            <h4 class="advantage-title">{{ advantage.title }}</h4>
            <p class="advantage-description">{{ advantage.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 应用场景 -->
    <section class="scenarios-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">应用场景</h2>
          <p class="section-subtitle">广泛适用于各类医疗机构，助力医疗行业数字化转型</p>
        </div>
        <div class="scenarios-grid">
          <div class="scenario-card" v-for="scenario in scenarios" :key="scenario.id">
            <h4 class="scenario-title">{{ scenario.title }}</h4>
            <p class="scenario-description">{{ scenario.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 联系我们 -->
    <section class="contact-section">
      <div class="container">
        <div class="contact-content">
          <h2 class="contact-title">开启智慧医疗新时代</h2>
          <p class="contact-subtitle">让我们一起推动医疗行业的数字化转型</p>
          <div class="contact-actions">
            <el-button type="primary" size="large" @click="handleDemo">
              <el-icon><VideoPlay /></el-icon>
              观看演示
            </el-button>
            <el-button size="large" @click="handleContact">
              <el-icon><Phone /></el-icon>
              联系我们
            </el-button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { 
  Document, 
  DataAnalysis, 
  Monitor, 
  Connection,
  VideoPlay,
  Phone,
  Lock,
  Star,
  Setting
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 核心功能数据
const features = reactive([
  {
    id: 1,
    icon: Document,
    title: '医疗文献管理',
    description: '智能化的医疗文献检索、分类和管理系统，支持多语言文献处理和知识图谱构建。',
    tags: ['智能检索', '自动分类', '知识图谱']
  },
  {
    id: 2,
    icon: DataAnalysis,
    title: '健康数据分析',
    description: '基于大数据和AI算法的健康数据深度分析，提供个性化健康评估和风险预测。',
    tags: ['大数据', '风险预测', '个性化']
  },
  {
    id: 3,
    icon: Monitor,
    title: '智能诊疗辅助',
    description: 'AI驱动的诊断辅助系统，为医生提供精准的诊断建议和治疗方案推荐。',
    tags: ['AI诊断', '治疗推荐', '临床决策']
  },
  {
    id: 4,
    icon: Connection,
    title: '医疗资源调度',
    description: '智能化的医疗资源管理和调度系统，优化医院运营效率和资源配置。',
    tags: ['智能排班', '资源优化', '效率提升']
  }
])

// 平台优势数据
const advantages = reactive([
  {
    id: 1,
    icon: Lock,
    title: '安全可靠',
    description: '采用多层安全防护，确保医疗数据的安全性和隐私保护。'
  },
  {
    id: 2,
    icon: Star,
    title: '高效智能',
    description: 'AI算法驱动，自动化处理，大幅提升医疗工作效率。'
  },
  {
    id: 3,
    icon: Star,
    title: '专业精准',
    description: '基于医学专业知识，提供精准的诊疗辅助和决策支持。'
  },
  {
    id: 4,
    icon: Setting,
    title: '灵活扩展',
    description: '模块化设计，支持个性化定制和功能扩展。'
  }
])

// 应用场景数据
const scenarios = reactive([
  {
    id: 1,
    title: '三甲医院',
    description: '为大型综合医院提供全面的智慧化解决方案，提升医疗服务质量和管理效率。'
  },
  {
    id: 2,
    title: '专科医院',
    description: '针对专科医院的特殊需求，提供定制化的专业医疗信息系统。'
  },
  {
    id: 3,
    title: '社区医院',
    description: '帮助社区医院实现数字化转型，提升基层医疗服务能力。'
  },
  {
    id: 4,
    title: '医疗集团',
    description: '为医疗集团提供统一的信息化平台，实现资源共享和协同管理。'
  }
])

// 演示功能
const handleDemo = () => {
  ElMessage.info('演示功能开发中...')
}

// 联系我们
const handleContact = () => {
  ElMessage.info('联系方式：400-123-4567')
}
</script>

<style scoped>
.medical-platform {
  background: #ffffff;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 顶部介绍区域 */
.hero-section {
  background: linear-gradient(135deg, #1890ff 0%, #52c41a 100%);
  color: white;
  padding: 80px 0;
  text-align: center;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 20px;
  line-height: 1.2;
}

.hero-subtitle {
  font-size: 20px;
  margin-bottom: 20px;
  opacity: 0.9;
  font-weight: 500;
}

.hero-description {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 40px;
  opacity: 0.8;
}

.hero-stats {
  display: flex;
  justify-content: center;
  gap: 60px;
  margin-top: 40px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.8;
}

/* 通用样式 */
.section-header {
  text-align: center;
  margin-bottom: 60px;
}

.section-title {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 16px;
}

.section-subtitle {
  font-size: 16px;
  color: #7f8c8d;
  max-width: 600px;
  margin: 0 auto;
}

/* 功能模块 */
.features-section {
  padding: 80px 0;
  background: #f8f9fa;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 30px;
}

.feature-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.feature-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #1890ff, #52c41a);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  color: white;
  font-size: 24px;
}

.feature-title {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
}

.feature-description {
  font-size: 14px;
  color: #7f8c8d;
  line-height: 1.6;
  margin-bottom: 20px;
}

.feature-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.feature-tag {
  background: #e8f4fd;
  color: #1890ff;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

/* 技术架构 */
.tech-section {
  padding: 80px 0;
  background: white;
}

.tech-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
}

.tech-category {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 30px;
  text-align: center;
}

.tech-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 20px;
}

.tech-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.tech-tag {
  background: white;
  color: #7f8c8d;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
}

.tech-tag:hover {
  background: #1890ff;
  color: white;
  border-color: #1890ff;
}

/* 平台优势 */
.advantages-section {
  padding: 80px 0;
  background: #f8f9fa;
}

.advantages-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
}

.advantage-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.advantage-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.advantage-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #1890ff, #52c41a);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  color: white;
  font-size: 20px;
}

.advantage-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
}

.advantage-description {
  font-size: 14px;
  color: #7f8c8d;
  line-height: 1.6;
}

/* 应用场景 */
.scenarios-section {
  padding: 80px 0;
  background: white;
}

.scenarios-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
}

.scenario-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 30px;
  text-align: center;
  transition: all 0.3s ease;
}

.scenario-card:hover {
  background: #e9ecef;
}

.scenario-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
}

.scenario-description {
  font-size: 14px;
  color: #7f8c8d;
  line-height: 1.6;
}

/* 联系我们 */
.contact-section {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  color: white;
  padding: 80px 0;
  text-align: center;
}

.contact-content {
  max-width: 600px;
  margin: 0 auto;
}

.contact-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 16px;
}

.contact-subtitle {
  font-size: 16px;
  margin-bottom: 40px;
  opacity: 0.9;
}

.contact-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
  flex-wrap: wrap;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 32px;
  }
  
  .hero-stats {
    gap: 30px;
  }
  
  .section-title {
    font-size: 28px;
  }
  
  .features-grid,
  .tech-grid,
  .advantages-grid,
  .scenarios-grid {
    grid-template-columns: 1fr;
  }
  
  .contact-actions {
    flex-direction: column;
    align-items: center;
  }
}

@media (max-width: 480px) {
  .hero-stats {
    flex-direction: column;
    gap: 20px;
  }
  
  .container {
    padding: 0 15px;
  }
}
</style>
