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

            <!-- AI回复中的体检套餐推荐 -->
            <div v-if="msg.type === 'ai' && msg.content.includes('体检')" class="package-recommendations">
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
          </div>
        </div>

        <div v-if="isLoading" class="message ai">
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
import { ref, nextTick } from 'vue'
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

    const quickQuestions = [
      '我经常头痛，应该做什么检查？',
      '胸闷气短需要检查什么？',
      '腹痛应该做哪些体检项目？',
      '想做全身体检，推荐什么套餐？',
      '血压高需要做什么检查？'
    ]

    const sendMessage = async () => {
      if (!currentQuestion.value.trim() || isLoading.value) return

      const question = currentQuestion.value.trim()

      messages.value.push({
        type: 'user',
        content: question
      })

      currentQuestion.value = ''
      isLoading.value = true

      try {
        // 暂时使用本地模拟响应，确保功能正常
        await new Promise(resolve => setTimeout(resolve, 1000)) // 模拟网络延迟

        let aiResponse = generateLocalResponse(question)

        messages.value.push({
          type: 'ai',
          content: aiResponse
        })

      } catch (error) {
        console.error('发送消息失败:', error)
        ElMessage.error('发送失败，请稍后重试')

        messages.value.push({
          type: 'ai',
          content: '抱歉，服务暂时不可用。请稍后再试。'
        })
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

    const goToBooking = (packageType) => {
      // 跳转到体检套餐预约页面
      router.push('/manager/userExaminationPackage')
      ElMessage.success(`正在跳转到${packageType === 'basic' ? '基础' : '全面'}体检套餐预约页面...`)
    }

    return {
      currentQuestion,
      messages,
      isLoading,
      messageList,
      quickQuestions,
      sendMessage,
      selectQuickQuestion,
      clearChat,
      goToBooking
    }
  }
}
</script>

<style scoped>
.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

h2 {
  text-align: center;
  color: #409eff;
  margin-bottom: 20px;
}

.chat-box {
  border: 1px solid #ddd;
  border-radius: 8px;
  background: white;
  margin-bottom: 20px;
}

.messages {
  height: 400px;
  overflow-y: auto;
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.welcome-msg {
  text-align: center;
  color: #666;
  padding: 50px 20px;
}

.message {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-start;
}

.message.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  margin: 0 10px;
}

.message.user .avatar {
  background: #67c23a;
}

.content {
  max-width: 67%;
  padding: 10px 15px;
  border-radius: 12px;
  word-wrap: break-word;
  background: white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.message.user .content {
  background: #409eff;
  color: white;
}

.message.ai .content {
  background: #f5f5f5;
  color: #333;
}

.input-area {
  padding: 15px;
}

.buttons {
  margin-top: 10px;
  text-align: right;
}

.buttons .el-button {
  margin-left: 10px;
}

.quick-questions {
  background: white;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #ddd;
}

.quick-questions h3 {
  margin: 0 0 15px 0;
  color: #333;
}

/* 体检套餐推荐样式 */
.package-recommendations {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.package-recommendations h4 {
  margin: 0 0 10px 0;
  color: #409eff;
  font-size: 14px;
}

.package-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.package-item {
  background: #f9f9f9;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.package-name {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.package-price {
  color: #e6a23c;
  font-weight: bold;
  font-size: 16px;
}

.package-desc {
  color: #666;
  font-size: 12px;
  margin: 5px 0;
  flex: 1;
  margin-left: 10px;
  margin-right: 10px;
}

.text {
  line-height: 1.6;
}
</style>