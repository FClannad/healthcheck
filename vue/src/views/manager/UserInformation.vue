<template>
  <div>
    <div class="card" style="font-size: 20px; margin-bottom: 10px">健康科普</div>
    <el-row :gutter="10">
      <el-col :span="24" v-for="item in data.tableData" :key="item.id">
        <div class="card" style="padding: 15px; margin-bottom: 10px; display: flex; grid-gap: 20px">
          <img :src="item.cover" alt="" style="width: 360px; height: 280px">
          <div style="flex: 1; width: 0">
            <div style="font-size: 22px" class="line1 title" @click="goPage('/manager/informationDetail?id=' + item.id)">{{ item.title }}</div>
            <div style="color: #666; margin: 20px 0; line-height: 24px; text-align: justify" class="line3" v-html="item.content"></div>
            <div style="color: #666">
              <span style="margin-right: 20px"><el-icon size="20" style="top: 2px"><User /></el-icon> {{ item.authorName }}</span>
              <span style="margin-right: 20px"><el-icon size="20" style="top: 2px"><View /></el-icon> {{ item.viewCount }}</span>
              <span><el-icon size="14" style="top: 2px"><Clock /></el-icon> {{ item.publishTime }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="card" style="width: fit-content" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request.js";

const data = reactive({
  tableData: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const goPage = (path) => {
  location.href = path
}

const load = () => {
  request.get('/information/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list || []
      data.total = res.data?.total
    }
  })
}
load()
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