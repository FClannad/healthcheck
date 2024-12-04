<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.name" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入项目名称查询"></el-input>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
    </div>
    <div class="card" style="margin-bottom: 5px">
      <el-button type="primary" plain @click="handleAdd">新增</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table tooltip-effect="dark myTooltip" stripe :data="data.tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column label="项目封面">
          <template #default="scope">
            <el-image style="width: 100px; height: 80px; border-radius: 5px" :src="scope.row.cover" :preview-src-list="[scope.row.cover]" preview-teleported></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="项目名" show-overflow-tooltip></el-table-column>
        <el-table-column prop="englishName" label="英文名" show-overflow-tooltip></el-table-column>

        <el-table-column prop="examinationTypeName" label="项目类型"></el-table-column>
        <el-table-column prop="content" label="项目内容" show-overflow-tooltip></el-table-column>
        <el-table-column prop="people" label="适宜人群"></el-table-column>
        <el-table-column prop="purpose" label="检测目的" show-overflow-tooltip></el-table-column>
        <el-table-column prop="officeName" label="所属科室"></el-table-column>
        <el-table-column prop="money" label="费用"></el-table-column>
        <el-table-column prop="doctorName" label="负责医生"></el-table-column>
        <el-table-column prop="attention" label="注意事项" show-overflow-tooltip></el-table-column>
        <el-table-column prop="address" label="地址" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template v-slot="scope">
            <el-button type="primary" circle :icon="Edit" @click="handleEdit(scope.row)"></el-button>
            <el-button type="danger" circle :icon="Delete" @click="del(scope.row.id)"></el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>

    <el-dialog title="体检项目信息" v-model="data.formVisible" width="40%" destroy-on-close>
      <el-form ref="form" :model="data.form" label-width="70px" style="padding: 20px">

        <el-form-item label="项目封面" prop="cover">
          <el-upload
              :action="baseUrl + '/files/upload'"
              :headers="{ 'token': data.user.token }"
              :on-success="handleFileUpload"
              list-type="picture"
          >
            <el-button type="primary">上传</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="项目名" prop="name">
          <el-input v-model="data.form.name" placeholder="项目名"></el-input>
        </el-form-item>
        <el-form-item label="英文名" prop="englishName">
          <el-input v-model="data.form.englishName" placeholder="英文名"></el-input>
        </el-form-item>

        <el-form-item label="项目类型" prop="examinationTypeId">
          <el-select style="width: 100%" v-model="data.form.examinationTypeId">
            <el-option v-for="item in data.typeList" :key="item.id" :value="item.id" :label="item.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="项目内容" prop="content">
          <el-input type="textarea" :rows="6" v-model="data.form.content" placeholder="项目内容"></el-input>
        </el-form-item>
        <el-form-item label="适宜人群" prop="people">
          <el-input v-model="data.form.people" placeholder="适宜人群"></el-input>
        </el-form-item>
        <el-form-item label="检测目的" prop="purpose">
          <el-input type="textarea" :rows="6" v-model="data.form.purpose" placeholder="检测目的"></el-input>
        </el-form-item>
        <el-form-item label="所属科室" prop="officeId">
          <el-select style="width: 100%" v-model="data.form.officeId">
            <el-option v-for="item in data.officeList" :key="item.id" :value="item.id" :label="item.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="费用" prop="money">
          <el-input v-model="data.form.money" placeholder="费用"></el-input>
        </el-form-item>
        <el-form-item label="负责医生" prop="doctorId">
          <el-select style="width: 100%" v-model="data.form.doctorId">
            <el-option v-for="item in data.doctorList" :key="item.id" :value="item.id" :label="item.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="注意事项" prop="attention">
          <el-input type="textarea" :rows="6" v-model="data.form.attention" placeholder="注意事项"></el-input>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input type="textarea" :rows="3" v-model="data.form.address" placeholder="地址"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">确 定</el-button>
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


const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  formVisible: false,
  form: {},
  tableData: [],
  pageNum: 1,
  pageSize: 5,
  total: 0,
  name: null,
  ids: [],
  typeList: [],
  doctorList: [],
  officeList: []
})

request.get('/examinationType/selectAll').then(res => {
  data.typeList = res.data
})

request.get('/doctor/selectAll').then(res => {
  data.doctorList = res.data
})

request.get('/office/selectAll').then(res => {
  data.officeList = res.data
})

const baseUrl = import.meta.env.VITE_BASE_URL
const handleFileUpload = (res) => {
  data.form.cover = res.data
}

const load = () => {
  request.get('/physicalExamination/selectPage', {
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
const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}
const add = () => {
  request.post('/physicalExamination/add', data.form).then(res => {
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
  request.put('/physicalExamination/update', data.form).then(res => {
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
    request.delete('/physicalExamination/delete/' + id).then(res => {
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
    request.delete("/physicalExamination/delete/batch", {data: data.ids}).then(res => {
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
  data.name = null
  load()
}

load()
</script>

<style>
.myTooltip {
  max-width: 500px;
}
</style>