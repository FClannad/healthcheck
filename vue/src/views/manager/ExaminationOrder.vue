<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.orderNo" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入订单编号查询"></el-input>
      <el-select placeholder="请选择检查项目类型" v-model="data.orderType" style="width: 240px; margin-right: 10px">
        <el-option value="普通体检"></el-option>
        <el-option value="套餐体检"></el-option>
      </el-select>
      <el-select placeholder="请选择订单状态"  v-model="data.status" style="width: 240px; margin-right: 10px">
        <el-option value="已取消"></el-option>
        <el-option value="待审批"></el-option>
        <el-option value="审批拒绝"></el-option>
        <el-option value="待检查"></el-option>
        <el-option value="待上传报告"></el-option>
        <el-option value="已完成"></el-option>
      </el-select>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
    </div>
    <div class="card" style="margin-bottom: 5px" v-if="data.user.role === 'ADMIN'">
      <el-button type="danger" plain @click="delBatch" >批量删除</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table stripe :data="data.tableData" @selection-change="handleSelectionChange">
        <el-table-column label="展开" width="60" type="expand">
          <template #default="scope">
            <div style="padding: 10px">
              <el-descriptions title="订单详细信息" border>
                <el-descriptions-item label="订单号">{{ scope.row.orderNo }} </el-descriptions-item>
                <el-descriptions-item label="用户">{{ scope.row.userName }} </el-descriptions-item>
                <el-descriptions-item label="医生">{{ scope.row.doctorName }} </el-descriptions-item>
                <el-descriptions-item label="预约日期">{{ scope.row.reserveDate }} </el-descriptions-item>
                <el-descriptions-item label="预约开始时间">{{ scope.row.startTime }} </el-descriptions-item>
                <el-descriptions-item label="预约结束时间">{{ scope.row.endTime }} </el-descriptions-item>
                <el-descriptions-item label="体检项目">
                  <span v-if="scope.row.orderType === '普通体检'">{{ scope.row.examinationName }}</span>
                  <span @click="viewExaminationList(scope.row.examinationList)" style="cursor: pointer; color: #2562ec" v-else>点击查看体检项目列表</span>
                </el-descriptions-item>
                <el-descriptions-item label="体检项目类型">{{ scope.row.orderType }} </el-descriptions-item>
                <el-descriptions-item label="费用">{{ scope.row.money }} </el-descriptions-item>
                <el-descriptions-item label="订单状态">{{ scope.row.status }} </el-descriptions-item>
                <el-descriptions-item label="反馈">{{ scope.row.feedback }} </el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ scope.row.createTime }} </el-descriptions-item>
                <el-descriptions-item label="检查时间">{{ scope.row.checkTime }} </el-descriptions-item>
                <el-descriptions-item label="评语">{{ scope.row.comment }} </el-descriptions-item>
              </el-descriptions>
            </div>
          </template>
        </el-table-column>
        <el-table-column type="selection" width="55"  v-if="data.user.role === 'ADMIN'" />
        <el-table-column prop="orderNo" label="订单号" width="200"></el-table-column>
        <el-table-column prop="userName" label="用户"></el-table-column>
        <el-table-column prop="doctorName" label="医生"></el-table-column>
        <el-table-column prop="reserveDate" label="预约日期"></el-table-column>
        <el-table-column prop="file" label="检测报告">
          <template #default="scope">
            <el-button type="primary" plain v-if="scope.row.file" @click="download(scope.row.file)">下载</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="money" label="费用"></el-table-column>
        <el-table-column prop="status" label="订单状态">
          <template #default="scope">
            <el-tag type="danger" v-if="scope.row.status === '已取消'">已取消</el-tag>
            <el-tag type="primary"  v-if="scope.row.status === '待审批'">待审批</el-tag>
            <el-tag type="danger"  v-if="scope.row.status === '审批拒绝'">审批拒绝</el-tag>
            <el-tag type="info"  v-if="scope.row.status === '待支付'">待支付</el-tag>
            <el-tag type="primary"  v-if="scope.row.status === '待检查'">待检查</el-tag>
            <el-tag type="info"  v-if="scope.row.status === '待上传报告'">待上传报告</el-tag>
            <el-tag type="success"  v-if="scope.row.status === '已完成'">已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template v-slot="scope">
            <div v-if="data.user.role === 'USER'">
              <el-button type="danger" size="small" @click="changeStatus(scope.row, '已取消')" v-if="scope.row.status === '待审批'">取消</el-button>
              <el-button type="info" size="small" @click="changeStatus(scope.row, '待检查')" v-if="scope.row.status === '待支付'">支付</el-button>
            </div>
            <div v-if="data.user.role === 'DOCTOR'">
              <el-button type="primary" size="small" @click="audit(scope.row)" v-if="scope.row.status === '待审批'">审批</el-button>
              <el-button type="success" size="small" @click="changeStatus(scope.row, '待上传报告')" v-if="scope.row.status === '待检查'">检查完成</el-button>
              <el-button type="primary" size="small" @click="audit(scope.row)" v-if="scope.row.status === '待上传报告'">上传报告</el-button>
            </div>
            <div v-if="data.user.role === 'ADMIN'">
              <el-button type="danger" circle :icon="Delete" @click="del(scope.row.id)"></el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>

    <el-dialog title="订单信息" v-model="data.formVisible" width="40%" destroy-on-close>
      <el-form ref="form" :model="data.form" label-width="70px" style="padding: 20px">
        <el-form-item prop="status" label="订单状态" v-if="data.form.status === '待审批'">
          <el-radio-group v-model="data.form.status1">
            <el-radio-button value="审批通过" label="审批通过"></el-radio-button>
            <el-radio-button value="审批拒绝" label="审批拒绝"></el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="feedback" label="反馈" v-if="data.form.status === '待审批'">
          <el-input type="textarea" :rows="3" placeholder="请输入反馈信息" v-model="data.form.feedback"></el-input>
        </el-form-item>
        <el-form-item label="上传报告" v-if="data.form.status === '待上传报告'">
          <el-upload
              :action="baseUrl + '/files/upload'"
              :headers="{ 'token': data.user.token }"
              :on-success="handleFileUpload"
              list-type="picture"
          >
            <el-button type="primary">上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item prop="comment" label="评语" v-if="data.form.status === '待上传报告'">
          <el-input type="textarea" :rows="5" placeholder="请输入反馈信息" v-model="data.form.comment"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="saveOrder">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="套餐体检项目列表" v-model="data.formVisible1" width="70%" destroy-on-close>
      <el-table tooltip-effect="dark myTooltip" border stripe :data="data.examinationList">
        <el-table-column label="项目名" prop="name"></el-table-column>
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

import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {Delete, Edit} from "@element-plus/icons-vue";
import router from "@/router/index.js";


const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  formVisible: false,
  form: {},
  tableData: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  orderNo: null,
  orderType: null,
  status: null,
  ids: [],
  examinationList: [],
  formVisible1: false
})

const viewExaminationList = (examinationList) => {
  data.examinationList = examinationList
  data.formVisible1 = true
}

const baseUrl = import.meta.env.VITE_BASE_URL
const handleFileUpload = (res) => {
  data.form.file = res.data
}

const download = (file) => {
  window.open(file)
}

const audit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

const saveOrder = () => {
  data.form.status = data.form.status1
  if (data.form.status === '审批通过') {
    data.form.status = '待支付'
  }
  if (data.form.file) {  // 已上传报告
    data.form.status = '已完成'
  }
  request.put('/examinationOrder/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      load()
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const changeStatus = (row, status) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.form.status = status
  ElMessageBox.confirm('您确认操作吗？', '确认', { type: "warning" }).then(res => {
    request.put('/examinationOrder/update', data.form).then(res => {
      if (res.code === '200') {
        ElMessage.success('操作成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch()
}

const load = () => {
  request.get('/examinationOrder/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      orderNo: data.orderNo,
      orderType: data.orderType,
      status: router.currentRoute.value.query.status || data.status,
      reserveDate: router.currentRoute.value.query.reserveDate || null
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list || []
      data.total = res.data?.total
    } else {
      ElMessage.error(res.msg)
    }
  })
}
const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}
const add = () => {
  request.post('/examinationOrder/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {
  request.put('/examinationOrder/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    }
  })
}

const save = () => {
  data.form.id ? update() : add()
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/examinationOrder/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success("删除成功")
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
    console.error(err)
  })
}
const delBatch = () => {
  if (!data.ids.length) {
    ElMessage.warning("请选择数据")
    return
  }
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete("/examinationOrder/delete/batch", {data: data.ids}).then(res => {
      if (res.code === '200') {
        ElMessage.success('操作成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
    console.error(err)
  })
}
const handleSelectionChange = (rows) => {
  data.ids = rows.map(v => v.id)
}

const reset = () => {
  location.href = '/manager/examinationOrder'
  load()
}

load()
</script>

<style>
.myTooltip {
  width: 500px;
}
</style>