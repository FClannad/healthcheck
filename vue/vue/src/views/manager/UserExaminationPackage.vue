<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.name" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入项目名称查询"></el-input>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
    </div>
    <el-row :gutter="10">
      <el-col :span="6" v-for="item in data.tableData" :key="item.id">
        <div class="card" style="margin-bottom: 10px; padding: 15px">
          <img :src="item.cover" alt="" style="width: 100%; height: 300px">
          <div style="margin: 10px 0"><span style="color: #666">项目名：</span>{{ item.name }}</div>
          <div style="margin: 10px 0"><span style="color: #666">包含项目：</span><span style="color: #2562ec; cursor: pointer" @click="viewExaminationList(item.examinationList)">点击查看</span></div>
          <div style="margin: 10px 0"><span style="color: #666">负责医生：</span>{{ item.doctorName }}</div>
          <el-tooltip :content="item.descr">
            <div style="margin: 10px 0" class="line1"><span style="color: #666">套餐介绍：</span>{{ item.descr }}</div>
          </el-tooltip>
          <div style="margin: 10px 0"><span style="color: #666">体检地址：</span>{{ item.address }}</div>
          <div style="display: flex; align-items: center">
            <div style="flex: 1">
              <span style="color: #666">检查价格：</span><span style="color: red; font-size: 20px">￥{{ item.money }}</span>
            </div>
            <el-button type="primary" @click="handleReserve(item)">立即预约</el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="card" style="width: fit-content" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>

    <el-dialog title="预约体检" v-model="data.formVisible" width="40%" destroy-on-close>
      <el-form ref="formRef" :rules="data.rules" :model="data.form" label-width="100px" style="padding: 20px">
        <el-form-item label="预约项目">
          <div>{{ data.form.name }}</div>
        </el-form-item>
        <el-form-item prop="reserveDate">
          <template #label>
            <span>预约日期</span>
            <el-tooltip content="注意：必须在预约日期之前支付费用，否则会影响您的正常体检">
              <el-icon size="14" style="top: 8px; left: 2px"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <el-date-picker :disabled-date="disabledDate" style="width: 100%" v-model="data.form.reserveDate" type="date" placeholder="预约日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="到达时间" prop="time">
          <div style="display: flex; align-items: center; grid-gap: 10px">
            <el-time-picker style="flex: 1" start="08:30" end="18:30" step="01:00" v-model="data.form.startTime" format="HH:mm" value-format="HH:mm" placeholder="开始时间" />
            <el-time-picker style="flex: 1" start="08:30" end="18:30" step="01:00" v-model="data.form.endTime" format="HH:mm" value-format="HH:mm" placeholder="结束时间" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="addReserve">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="套餐体检项目列表" v-model="data.formVisible1" width="70%" destroy-on-close>
      <el-table  tooltip-effect="dark myTooltip" border stripe :data="data.examinationList">
        <el-table-column label="项目名称" prop="name"></el-table-column>
        <el-table-column label="项目类型" prop="examinationTypeName"></el-table-column>
        <el-table-column label="检测目的" prop="purpose" show-overflow-tooltip></el-table-column>
        <el-table-column label="注意事项" prop="attention" show-overflow-tooltip></el-table-column>
        <el-table-column label="适宜人群" prop="people" show-overflow-tooltip width="100"></el-table-column>
        <el-table-column label="所属科室" prop="officeName"  width="100"></el-table-column>
        <el-table-column label="项目价格" prop="money"  width="100"></el-table-column>
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible1 = false">关 闭</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";

const timeValidator = (rule, value, callback) => {
  if (!data.form.startTime) {
    callback(new Error('请选择预约开始时间'))
  } else if (!data.form.endTime) {
    callback(new Error('请选择预约结束时间'))
  } else {
    callback()
  }
}

const formRef = ref()
const data = reactive({
  tableData: [],
  pageNum: 1,
  pageSize: 8,
  total: 0,
  name: null,
  formVisible: false,
  form: {},
  formVisible1: false,
  examinationList: [],
  rules: {
    reserveDate: [
      { required: true, message: '请选择预约日期', trigger: 'change' }
    ],
    time: [
      { validator: timeValidator, trigger: 'change' }
    ],
  }
})

const viewExaminationList = (examinationList) => {
  data.examinationList = examinationList
  data.formVisible1 = true
}

const disabledDate = (date) => {
  return date.getTime() < new Date().getTime()  // 在今天之前的日期无法预约
}

const handleReserve = (examination) => {
  data.form = { name: examination.name,  orderType: '套餐体检', examinationId: examination.id }
  data.formVisible = true
}

const addReserve = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      request.post('/examinationOrder/add', data.form).then(res => {
        if (res.code === '200') {
          ElMessage.success('预约成功')
          data.formVisible = false
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}

const load = () => {
  request.get('/examinationPackage/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list || []
      data.total = res.data?.total
    }
  })
}
load()

const reset = () => {
  data.name = null
  load()
}
</script>

<style>
.myTooltip {
  width: 500px;
}
</style>