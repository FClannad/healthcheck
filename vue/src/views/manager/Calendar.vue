<template>
  <div>
    <el-calendar #date-cell="{ data }">
      <div>{{ data.day.split('-').slice(1).join('-') }}</div>
      <div @click="viewOrder(data.day)" style="color: #2562ec; padding: 10px 0" v-if="globalData.orderList?.filter(v => v.reserveDate === data.day).length > 0">
        有 <strong style="color: red">{{ globalData.orderList.filter(v => v.reserveDate === data.day).length }}</strong> 例待检查的患者
      </div>
    </el-calendar>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request.js";
import router from "@/router/index.js";

const globalData = reactive({
  orderList: []
})

const viewOrder = (reserveDate) => {
  router.push('/manager/examinationOrder?reserveDate=' + reserveDate + '&status=待检查')
}

request.get('/examinationOrder/schedule').then(res => {
  globalData.orderList = res.data
})
</script>