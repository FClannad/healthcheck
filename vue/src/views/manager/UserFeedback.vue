<template>
  <div class="feedback-container">
    <!-- Feedback Form -->
    <div class="feedback-card">
      <div class="card-header">
        <span>请留下您的反馈与建议</span>
      </div>
      <div class="input-container">
        <el-input type="textarea" :rows="5" v-model="data.form.content" placeholder="请输入内容..." class="input-area"></el-input>
      </div>
      <div class="submit-btn-container">
        <el-button type="primary" @click="addFeedback" class="submit-btn">发布</el-button>
      </div>
    </div>

    <!-- Feedback List -->
    <div class="feedback-card">
      <div v-for="item in data.tableData" :key="item.id" class="feedback-item">
        <!-- User Avatar and Info -->
        <div class="user-info">
          <img :src="item.userAvatar" alt="avatar" class="user-avatar" />
          <div class="user-name">{{ item.userName }}</div>
        </div>

        <!-- Feedback Content -->
        <div class="feedback-content">
          <div class="content-text">
            <span class="content">{{ item.content }}</span>
            <span class="time">{{ item.time }}</span>
            <span v-if="item.userId === data.user.id" class="delete-btn" @click="del(item.id)">删除</span>
          </div>

          <!-- Admin Reply -->
          <div v-if="item.status === '已回复'" class="admin-reply">
            <span class="reply-text">管理员回复：{{ item.replyContent }}</span>
            <span class="reply-time">回复时间：{{ item.replyTime }}</span>
          </div>

        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination-container" v-if="data.total">
        <el-pagination
            @current-change="load"
            background
            layout="total, prev, pager, next"
            :page-size="data.pageSize"
            v-model:current-page="data.pageNum"
            :total="data.total"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request.js";
import { ElMessage, ElMessageBox } from "element-plus";

const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  form: {},
  tableData: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
});

const load = () => {
  request.get('/feedback/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      content: data.content,
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list || [];
      data.total = res.data?.total;
    }
  });
};
load();

const addFeedback = () => {
  request.post('/feedback/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('发布成功');
      load();
      data.form = {};
    } else {
      ElMessage.error(res.msg);
    }
  });
};

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/feedback/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success("删除成功");
        load();
      } else {
        ElMessage.error(res.msg);
      }
    });
  }).catch(err => {
    console.error(err);
  });
};

const reply = (id) => {
  const item = data.tableData.find(item => item.id === id);
  if (item.replyContent) {
    request.post('/feedback/reply', { id, replyContent: item.replyContent }).then(res => {
      if (res.code === '200') {
        ElMessage.success("回复成功");
        load();
      } else {
        ElMessage.error(res.msg);
      }
    });
  } else {
    ElMessage.warning("请输入回复内容");
  }
};
</script>

<style scoped>
.feedback-container {
  width: 70%;
  margin: 20px auto;
}

.feedback-card {
  background-color: #f9f9f9;
  border-radius: 15px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.card-header {
  font-size: 22px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
}

.input-container {
  margin-bottom: 20px;
}

.input-area {
  border-radius: 10px;
  padding: 10px;
}

.submit-btn-container {
  text-align: right;
}

.submit-btn {
  border-radius: 20px;
}

.feedback-item {
  display: flex;
  margin-bottom: 20px;
  padding: 10px;
  border-radius: 10px;
  background-color: #fff;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.user-info {
  width: 60px;
  text-align: center;
  margin-right: 20px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.user-name {
  font-size: 12px;
  color: #555;
}

.feedback-content {
  flex: 1;
}

.content-text {
  display: flex;
  align-items: center;
}

.content {
  font-size: 16px;
  color: #333;
  margin-right: 20px;
}

.time {
  font-size: 12px;
  color: #999;
}

.delete-btn {
  font-size: 12px;
  color: red;
  cursor: pointer;
  margin-left: 10px;
}

.admin-reply {
  margin-top: 10px;
  font-size: 14px;
  color: lightpink;
}

.reply-container {
  margin-top: 10px;
}

.reply-input {
  border-radius: 10px;
  padding: 10px;
  width: 100%;
}

.reply-btn {
  margin-top: 10px;
  border-radius: 20px;
}

.pagination-container {
  text-align: center;
}
</style>
