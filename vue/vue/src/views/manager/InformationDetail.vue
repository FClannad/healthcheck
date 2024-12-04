<template>
  <div>
    <div class="card" style="padding: 20px">
      <div><el-icon size="26" style="cursor: pointer" @click="router.back()"><Back /></el-icon></div>
      <div style="text-align: center; font-size: 24px; font-weight: bold">{{ data.information.title }}</div>

      <div style="color: #666; text-align: center; margin: 20px 0">
        <span style="margin-right: 20px"><el-icon size="14" style="top: 2px"><User /></el-icon> {{ data.information.authorName }}</span>
        <span style="margin-right: 20px"><el-icon size="14" style="top: 2px"><View /></el-icon> {{ data.information.viewCount }}</span>
        <span><el-icon size="14" style="top: 2px"><Clock /></el-icon> {{ data.information.publishTime }}</span>
      </div>

      <div style="text-align: justify; line-height: 24px; padding: 0 50px">
        <div v-html="data.information.content"></div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request.js";
import router from "@/router/index.js";

const data = reactive({
  id: router.currentRoute.value.query.id,
  information: {}
})

// 先更新阅读量  再查询数据
request.put('/information/updateViewCount/' + data.id).then(res => {
  load()
})

const load = () => {
  request.get('/information/selectById/' + data.id).then(res => {
    if (res.code === '200') {
      data.information = res.data
    }
  })
}

</script>

<style>
.title {
  cursor: pointer;
}
.title:hover {
  color: red;
  text-decoration: underline;
}
</style>