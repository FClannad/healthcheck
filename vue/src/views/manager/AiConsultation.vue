<template>
  <div class="container">
    <h2>AI健康咨询</h2>

    <div class="chat-box">
      <!-- 消息列表 -->
      <div class="messages" ref="messageList">
        <div v-if="messages.length === 0" class="welcome-msg">
          欢迎使用AI健康咨询！请描述您的症状。
        </div>

        <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.type]">
          <div class="avatar">
            <el-icon v-if="msg.type === 'user'"><User /></el-icon>
            <el-icon v-else><ChatDotRound /></el-icon>
          </div>
          <div class="content">
            <div class="text">{{ msg.content }}</div>

            <!-- AI回复中的推荐体检项目（仅在流式输出完成后显示） -->
            <div v-if="msg.type === 'ai' && msg.recommendedExams && msg.recommendedExams.length > 0 && !msg.isStreaming" class="recommended-exams">
              <h4>📋 推荐体检项目</h4>
              <div class="exam-tags">
                <el-tag v-for="exam in msg.recommendedExams" :key="exam" type="info" style="margin: 2px;">{{ exam }}</el-tag>
              </div>
            </div>

            <!-- AI回复中的体检套餐推荐（仅在流式输出完成后显示） -->
            <div v-if="msg.type === 'ai' && msg.recommendedPackages && msg.recommendedPackages.length > 0 && !msg.isStreaming" class="package-recommendations">
              <h4>🏥 推荐体检套餐</h4>
              <div class="package-list">
                <div v-for="pkg in msg.recommendedPackages" :key="pkg.id" class="package-item">
                  <div class="package-name">{{ pkg.name }}</div>
                  <div class="package-price">¥{{ pkg.money }}</div>
                  <div class="package-desc">{{ pkg.content || '综合体检套餐' }}</div>
                  <el-button size="small" type="primary" @click="goToBooking(pkg.id)">立即预约</el-button>
                </div>
              </div>
            </div>

            <!-- 如果没有后端返回的套餐，显示默认推荐（仅在流式输出完成后显示） -->
            <div v-else-if="msg.type === 'ai' && msg.content.includes('体检') && !msg.recommendedPackages && !msg.isStreaming" class="package-recommendations">
              <h4>🏥 推荐体检套餐</h4>
              <div class="package-list">
                <div class="package-item">
                  <div class="package-name">基础体检套餐</div>
                  <div class="package-price">¥299</div>
                  <div class="package-desc">血常规、尿常规、心电图、胸片</div>
                  <el-button size="small" type="primary" @click="goToBooking('basic')">立即预约</el-button>
                </div>
                <div class="package-item">
                  <div class="package-name">全面体检套餐</div>
                  <div class="package-price">¥599</div>
                  <div class="package-desc">基础项目+心脏彩超、腹部B超、肿瘤标志物</div>
                  <el-button size="small" type="primary" @click="goToBooking('comprehensive')">立即预约</el-button>
                </div>
              </div>
            </div>

            <!-- 后续问题提 -->
            <div v-if="msg.type === 'ai' && msg.followUpQuestion" class="follow-up-question">
              <el-alert :title="msg.followUpQuestion" type="info" :closable="false" show-icon />
            </div>
          </div>
        </div>

        <!-- 只在非流式模式下显示加载状态 -->
        <div v-if="isLoading && !isStreaming" class="message ai">
          <div class="avatar">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="content">正在分析中...</div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <el-input
          v-model="currentQuestion"
          type="textarea"
          :rows="3"
          placeholder="请描述您的症状..."
          :disabled="isLoading"
        />
        <div class="buttons">
          <el-button type="primary" @click="sendMessage" :loading="isLoading">发送</el-button>
          <el-button @click="clearChat">清空</el-button>
        </div>
      </div>
    </div>

    <!-- 快捷问题 -->
    <div class="quick-questions" v-if="messages.length === 0">
      <h3>常见问题：</h3>
      <el-button
        v-for="question in quickQuestions"
        :key="question"
        @click="selectQuickQuestion(question)"
        size="small"
        style="margin: 5px;"
      >
        {{ question }}
      </el-button>
    </div>
  </div>
</template>

<script>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, ChatDotRound } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

export default {
  name: 'AiConsultation',
  components: {
    User,
    ChatDotRound
  },
  setup() {
    const router = useRouter()
    const currentQuestion = ref('')
    const messages = ref([])
    const isLoading = ref(false)
    const messageList = ref(null)
    const sessionId = ref('')  // 会话ID，用于保持对话上下文
    const historyList = ref([])  // 历史咨询记录列表
    const isStreaming = ref(false)  // 是否正在流式输出

    const quickQuestions = [
      '我经常头痛，应该做什么检查？',
      '胸闷气短需要检查什么？',
      '腹痛应该做哪些体检项目？',
      '想做全身体检，推荐什么套餐？',
      '血压高需要做什么检查？'
    ]

    // 页面加载时获取历史记录
    onMounted(() => {
      loadHistory()
      // 从本地存储恢复当前会话
      const savedSessionId = localStorage.getItem('ai_session_id')
      const savedMessages = localStorage.getItem('ai_messages')
      if (savedSessionId) {
        sessionId.value = savedSessionId
      }
      if (savedMessages) {
        try {
          messages.value = JSON.parse(savedMessages)
        } catch (e) {
          console.error('恢复消息失败:', e)
        }
      }
    })

    // 加载历史咨询记录
    const loadHistory = async () => {
      try {
        const response = await request.get('/ai-consultation/history')
        if (response.code === '200') {
          historyList.value = response.data || []
        }
      } catch (error) {
        console.error('加载历史记录失败:', error)
      }
    }

    // 保存当前会话到本地存储
    const saveToLocalStorage = () => {
      if (sessionId.value) {
        localStorage.setItem('ai_session_id', sessionId.value)
      }
      localStorage.setItem('ai_messages', JSON.stringify(messages.value))
    }

    // 是否使用流式输出
    const useStream = ref(true)

    const sendMessage = async () => {
      if (!currentQuestion.value.trim() || isLoading.value) return

      const question = currentQuestion.value.trim()

      messages.value.push({
        type: 'user',
        content: question
      })

      currentQuestion.value = ''
      isLoading.value = true

      if (useStream.value) {
        // 使用流式输出
        await sendMessageStream(question)
      } else {
        // 使用普通请求
        await sendMessageNormal(question)
      }
    }

    // 流式输出发送消息
    const sendMessageStream = async (question) => {
      // 先添加一个空的AI消息，用于流式更新
      const aiMessageIndex = messages.value.length
      messages.value.push({
        type: 'ai',
        content: '',
        isStreaming: true
      })
      isStreaming.value = true  // 标记正在流式输出

      try {
        const token = localStorage.getItem('token')
        const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9090'
        const url = `${baseUrl}/ai-consultation/stream?question=${encodeURIComponent(question)}&sessionId=${encodeURIComponent(sessionId.value || '')}&token=${encodeURIComponent(token || '')}`
        
        const eventSource = new EventSource(url)
        let fullContent = ''

        eventSource.onopen = () => {
          console.log('SSE连接已建立')
        }

        eventSource.addEventListener('message', (event) => {
          // 接收到内容片段
          const content = event.data
          fullContent += content
          messages.value[aiMessageIndex].content = fullContent
          scrollToBottom()
        })

        eventSource.addEventListener('done', async (event) => {
          // 流式输出完成
          console.log('流式输出完成')
          messages.value[aiMessageIndex].isStreaming = false
          const finalContent = event.data || fullContent
          messages.value[aiMessageIndex].content = finalContent
          
          // 提取推荐的体检项目并添加默认套餐推荐
          if (finalContent.includes('体检')) {
            // 从AI回复中提取体检项目关键词
            const examKeywords = ['血常规', '尿常规', '心电图', '胸片', '胸部CT', '腹部B超',
              '心脏彩超', '动态心电图', '血脂检查', '肝功能', '肾功能',
              '甲状腺功能', '血糖', '糖化血红蛋白', '肿瘤标志物',
              '胃镜', '肠镜', '肺功能检查', '骨密度检查',
              '妇科常规', '宫颈癌筛查', '乳腺检查', '前列腺检查']
            const foundExams = examKeywords.filter(exam => finalContent.includes(exam))
            if (foundExams.length > 0) {
              messages.value[aiMessageIndex].recommendedExams = foundExams
            }
          }
          
          // 生成新的sessionId
          if (!sessionId.value) {
            sessionId.value = 'session_' + Date.now() + '_' + Math.floor(Math.random() * 1000)
          }
          
          // 保存对话记录到数据库
          try {
            await request.post('/ai-consultation/save-stream', {
              question: question,
              response: finalContent,
              sessionId: sessionId.value
            })
            console.log('对话记录已保存到数据库')
          } catch (saveError) {
            console.error('保存对话记录失败:', saveError)
          }
          
          // 保存到本地存储
          saveToLocalStorage()
          
          eventSource.close()
          isLoading.value = false
          isStreaming.value = false  // 流式输出结束
        })

        eventSource.addEventListener('error', (event) => {
          console.error('SSE错误:', event)
          if (event.data) {
            ElMessage.error(event.data)
          }
          messages.value[aiMessageIndex].isStreaming = false
          if (!messages.value[aiMessageIndex].content) {
            messages.value[aiMessageIndex].content = '抱歉，AI服务暂时不可用，请稍后重试。'
          }
          eventSource.close()
          isLoading.value = false
          isStreaming.value = false
        })

        eventSource.onerror = (error) => {
          console.error('SSE连接错误:', error)
          messages.value[aiMessageIndex].isStreaming = false
          if (!messages.value[aiMessageIndex].content) {
            // 降级到普通请求
            messages.value.pop()
            isStreaming.value = false
            sendMessageNormal(question)
            return
          }
          eventSource.close()
          isLoading.value = false
          isStreaming.value = false
        }

      } catch (error) {
        console.error('流式请求失败:', error)
        isStreaming.value = false
        // 降级到普通请求
        messages.value.pop()
        await sendMessageNormal(question)
      }
    }

    // 普通请求发送消息
    const sendMessageNormal = async (question) => {
      try {
        const response = await request.post('/ai-consultation/consult', {
          question: question,
          sessionId: sessionId.value
        })

        if (response.code === '200') {
          const data = response.data
          
          // 更新sessionId
          if (data.sessionId) {
            sessionId.value = data.sessionId
          }

          messages.value.push({
            type: 'ai',
            content: data.response,
            recommendedExams: data.recommendedExams,
            recommendedPackages: data.recommendedPackages,
            needMoreInfo: data.needMoreInfo,
            followUpQuestion: data.followUpQuestion
          })
          
          // 保存到本地存储
          saveToLocalStorage()
        } else {
          throw new Error(response.msg || '请求失败')
        }

      } catch (error) {
        console.error('发送消息失败:', error)
        ElMessage.error('发送失败，请稍后重试')

        // 降级到本地响应
        let aiResponse = generateLocalResponse(question)
        messages.value.push({
          type: 'ai',
          content: aiResponse
        })
        
        // 保存到本地存储
        saveToLocalStorage()
      } finally {
        isLoading.value = false
        await nextTick()
        scrollToBottom()
      }
    }

    const scrollToBottom = () => {
      if (messageList.value) {
        messageList.value.scrollTop = messageList.value.scrollHeight
      }
    }

    const selectQuickQuestion = (question) => {
      currentQuestion.value = question
      sendMessage()
    }

    const clearChat = () => {
      messages.value = []
      sessionId.value = ''  // 重置会话ID
      // 清除本地存储
      localStorage.removeItem('ai_session_id')
      localStorage.removeItem('ai_messages')
      ElMessage.success('对话已清空')
    }

    const generateLocalResponse = (question) => {
      const lowerQuestion = question.toLowerCase()

      if (lowerQuestion.includes('头痛') || lowerQuestion.includes('头疼')) {
        return '根据您描述的头痛症状，建议您进行以下体检：\n\n' +
               '1. 推荐体检项目：\n' +
               '   - 血常规检查\n' +
               '   - 血压测量\n' +
               '   - 头部CT或MRI\n' +
               '   - 颈椎X光\n\n' +
               '2. 可能需要的体检套餐：\n' +
               '   - 神经系统体检套餐\n' +
               '   - 心血管体检套餐\n\n' +
               '建议及时就医，排除器质性疾病。'
      }

      if (lowerQuestion.includes('胸闷') || lowerQuestion.includes('胸痛')) {
        return '根据您描述的胸闷症状，建议您进行以下体检：\n\n' +
               '1. 推荐体检项目：\n' +
               '   - 心电图\n' +
               '   - 心脏彩超\n' +
               '   - 胸部CT\n' +
               '   - 血脂检查\n\n' +
               '2. 可能需要的体检套餐：\n' +
               '   - 心血管体检套餐\n' +
               '   - 呼吸系统体检套餐\n\n' +
               '如症状严重，请立即就医。'
      }

      if (lowerQuestion.includes('腹痛') || lowerQuestion.includes('肚子疼')) {
        return '根据您描述的腹痛症状，建议您进行以下体检：\n\n' +
               '1. 推荐体检项目：\n' +
               '   - 腹部B超\n' +
               '   - 血常规\n' +
               '   - 肝功能检查\n' +
               '   - 胃镜检查\n\n' +
               '2. 可能需要的体检套餐：\n' +
               '   - 消化系统体检套餐\n' +
               '   - 全身体检套餐\n\n' +
               '建议尽快就医检查。'
      }

      return '感谢您的咨询。根据您的描述，我建议您：\n\n' +
             '1. 如有不适症状，建议及时就医\n' +
             '2. 定期进行健康体检\n' +
             '3. 保持良好的生活习惯\n\n' +
             '如需更详细的建议，请描述具体症状，我会为您提供更准确的体检建议。\n\n' +
             '温馨提示：AI建议仅供参考，不能替代医生诊断。'
    }

    const goToBooking = (packageId) => {
      // 跳转到体检套餐预约页面
      if (typeof packageId === 'number') {
        // 如果是数字ID，跳转到具体套餐
        router.push(`/manager/userExaminationPackage?packageId=${packageId}`)
        ElMessage.success('正在跳转到体检套餐预约页面...')
      } else {
        // 兼容旧的字符串类型
        router.push('/manager/userExaminationPackage')
        ElMessage.success(`正在跳转到${packageId === 'basic' ? '基础' : '全面'}体检套餐预约页面...`)
      }
    }

    return {
      currentQuestion,
      messages,
      isLoading,
      isStreaming,
      messageList,
      sessionId,
      historyList,
      quickQuestions,
      useStream,
      sendMessage,
      sendMessageStream,
      sendMessageNormal,
      selectQuickQuestion,
      clearChat,
      goToBooking,
      loadHistory,
      saveToLocalStorage
    }
  }
}
</script>

<style scoped>
/* ==================== AI健康咨询页面样式 ==================== */

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: var(--spacing-lg);
}

/* 页面标题 */
h2 {
  text-align: center;
  font-size: var(--font-size-xxl);
  font-weight: var(--font-weight-semibold);
  margin-bottom: var(--spacing-xl);
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 聊天容器 */
.chat-box {
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-lg);
  background: var(--background-white);
  margin-bottom: var(--spacing-lg);
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

/* 消息列表区域 */
.messages {
  height: 450px;
  overflow-y: auto;
  padding: var(--spacing-lg);
  background: linear-gradient(180deg, var(--background-light) 0%, var(--background-white) 100%);
}

/* 自定义滚动条 */
.messages::-webkit-scrollbar {
  width: 6px;
}

.messages::-webkit-scrollbar-track {
  background: var(--background-light);
  border-radius: 3px;
}

.messages::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
}

.messages::-webkit-scrollbar-thumb:hover {
  background: var(--text-tertiary);
}

/* 欢迎消息 */
.welcome-msg {
  text-align: center;
  color: var(--text-secondary);
  padding: 80px var(--spacing-xl);
  font-size: var(--font-size-lg);
}

.welcome-msg::before {
  content: '🏥';
  display: block;
  font-size: 48px;
  margin-bottom: var(--spacing-md);
}

/* 消息气泡 */
.message {
  display: flex;
  margin-bottom: var(--spacing-lg);
  align-items: flex-start;
  animation: messageSlideIn 0.3s ease-out;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message.user {
  flex-direction: row-reverse;
}

/* 头像样式 */
.avatar {
  width: 44px;
  height: 44px;
  border-radius: var(--border-radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
  flex-shrink: 0;
  margin: 0 var(--spacing-md);
  font-size: 20px;
  box-shadow: var(--shadow-sm);
}

.message.ai .avatar {
  background: var(--gradient-primary);
}

.message.user .avatar {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
}

/* 消息内容 */
.content {
  max-width: 70%;
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--border-radius-lg);
  word-wrap: break-word;
  box-shadow: var(--shadow-sm);
  position: relative;
}

.message.user .content {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  color: var(--text-inverse);
  border-bottom-right-radius: var(--border-radius-xs);
}

.message.ai .content {
  background: var(--background-white);
  color: var(--text-primary);
  border: 1px solid var(--border-light);
  border-bottom-left-radius: var(--border-radius-xs);
}

/* 消息文本 */
.text {
  line-height: 1.7;
  font-size: var(--font-size-sm);
  white-space: pre-wrap;
}

/* 输入区域 */
.input-area {
  padding: var(--spacing-lg);
  background: var(--background-light);
  border-top: 1px solid var(--border-light);
}

.input-area :deep(.el-textarea__inner) {
  border-radius: var(--border-radius-md);
  border-color: var(--border-color);
  background: var(--background-white);
  color: var(--text-primary);
  font-size: var(--font-size-sm);
  padding: var(--spacing-md);
  resize: none;
  transition: all var(--duration-fast) var(--ease-out);
}

.input-area :deep(.el-textarea__inner):focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.buttons {
  margin-top: var(--spacing-md);
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
}

.buttons .el-button {
  border-radius: var(--border-radius-md);
  padding: var(--spacing-sm) var(--spacing-lg);
  font-weight: var(--font-weight-medium);
}

/* 快捷问题区域 */
.quick-questions {
  background: var(--background-white);
  padding: var(--spacing-xl);
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

.quick-questions h3 {
  margin: 0 0 var(--spacing-md) 0;
  color: var(--text-primary);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.quick-questions h3::before {
  content: '💡';
}

.quick-questions .el-button {
  margin: var(--spacing-xs);
  border-radius: var(--border-radius-full);
  background: var(--background-light);
  border-color: var(--border-color);
  color: var(--text-secondary);
  transition: all var(--duration-fast) var(--ease-out);
}

.quick-questions .el-button:hover {
  background: var(--primary-lightest);
  border-color: var(--primary-color);
  color: var(--primary-color);
  transform: translateY(-2px);
}

/* 推荐体检项目样式 */
.recommended-exams {
  margin-top: var(--spacing-md);
  padding-top: var(--spacing-md);
  border-top: 1px dashed var(--border-color);
}

.recommended-exams h4 {
  margin: 0 0 var(--spacing-sm) 0;
  color: var(--success-color);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

.exam-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
}

/* 后续问题提示样式 */
.follow-up-question {
  margin-top: var(--spacing-md);
}

.follow-up-question :deep(.el-alert) {
  border-radius: var(--border-radius-md);
}

/* 体检套餐推荐样式 */
.package-recommendations {
  margin-top: var(--spacing-lg);
  padding-top: var(--spacing-lg);
  border-top: 1px dashed var(--border-color);
}

.package-recommendations h4 {
  margin: 0 0 var(--spacing-md) 0;
  color: var(--primary-color);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

.package-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.package-item {
  background: var(--background-light);
  border: 1px solid var(--border-light);
  border-radius: var(--border-radius-md);
  padding: var(--spacing-md);
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all var(--duration-fast) var(--ease-out);
}

.package-item:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-primary);
  transform: translateX(4px);
}

.package-name {
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  font-size: var(--font-size-sm);
}

.package-price {
  color: var(--warning-color);
  font-weight: var(--font-weight-bold);
  font-size: var(--font-size-lg);
}

.package-desc {
  color: var(--text-secondary);
  font-size: var(--font-size-xs);
  margin: var(--spacing-xs) 0;
  flex: 1;
  margin-left: var(--spacing-md);
  margin-right: var(--spacing-md);
}

/* ==================== 暗色主题适配 ==================== */

:global(.dark-theme) .chat-box {
  background: var(--background-white);
  border-color: var(--border-color);
}

:global(.dark-theme) .messages {
  background: linear-gradient(180deg, var(--background-light) 0%, var(--background-white) 100%);
}

:global(.dark-theme) .message.ai .content {
  background: var(--background-light);
  border-color: var(--border-color);
  color: var(--text-primary);
}

:global(.dark-theme) .welcome-msg {
  color: var(--text-secondary);
}

:global(.dark-theme) .input-area {
  background: var(--background-light);
  border-top-color: var(--border-color);
}

:global(.dark-theme) .input-area :deep(.el-textarea__inner) {
  background: var(--background-white);
  border-color: var(--border-color);
  color: var(--text-primary);
}

:global(.dark-theme) .quick-questions {
  background: var(--background-white);
  border-color: var(--border-color);
}

:global(.dark-theme) .quick-questions h3 {
  color: var(--text-primary);
}

:global(.dark-theme) .quick-questions .el-button {
  background: var(--background-light);
  border-color: var(--border-color);
  color: var(--text-secondary);
}

:global(.dark-theme) .quick-questions .el-button:hover {
  background: var(--primary-lightest);
  border-color: var(--primary-color);
  color: var(--primary-light);
}

:global(.dark-theme) .package-item {
  background: var(--background-light);
  border-color: var(--border-color);
}

:global(.dark-theme) .package-name {
  color: var(--text-primary);
}

:global(.dark-theme) .package-desc {
  color: var(--text-secondary);
}

/* ==================== 响应式设计 ==================== */

@media (max-width: 768px) {
  .container {
    padding: var(--spacing-md);
  }
  
  .messages {
    height: 350px;
  }
  
  .content {
    max-width: 85%;
  }
  
  .avatar {
    width: 36px;
    height: 36px;
    font-size: 16px;
    margin: 0 var(--spacing-sm);
  }
  
  .package-item {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-sm);
  }
  
  .package-desc {
    margin: var(--spacing-xs) 0;
  }
}
</style>