<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.name" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入名称查询"></el-input>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
    </div>
    <div class="card" style="margin-bottom: 5px">
      <el-button type="primary" plain @click="handleAdd">新增</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table stripe :data="data.tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="项目名" show-overflow-tooltip></el-table-column>
        <el-table-column prop="descr" label="套餐简介" show-overflow-tooltip></el-table-column>
        <el-table-column prop="cover" label="项目封面">
          <template #default="scope">
            <el-image style="width: 50px; height: 50px; border-radius: 5px" :src="scope.row.cover" :preview-src-list="[scope.row.cover]" preview-teleported></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="money" label="费用"></el-table-column>
        <el-table-column prop="doctorName" label="负责医生"></el-table-column>
        <el-table-column prop="address" label="地址" show-overflow-tooltip></el-table-column>
        <el-table-column prop="examinations" label="体检项目列表">
          <template #default="scope">
            <el-button type="primary" plain @click="viewExaminationList(scope.row.examinationList)">查看项目列表</el-button>
          </template>
        </el-table-column>
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

    <el-dialog title="套餐体检信息" v-model="data.formVisible" width="55%" destroy-on-close>
      <el-form ref="form" :model="data.form" label-width="100px" style="padding: 20px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="data.form.name" placeholder="项目名称"></el-input>
        </el-form-item>
        <el-form-item label="套餐简介" prop="descr">
          <el-input type="textarea" :rows="3" v-model="data.form.descr" placeholder="套餐简介"></el-input>
        </el-form-item>
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
        <el-form-item label="费用" prop="money">
          <el-input v-model="data.form.money" placeholder="费用"></el-input>
        </el-form-item>
        <el-form-item label="负责医生" prop="doctorId">
          <el-select style="width: 100%" v-model="data.form.doctorId">
            <el-option v-for="item in data.doctorList" :key="item.id" :value="item.id" :label="item.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input type="textarea" :rows="3" v-model="data.form.address" placeholder="请输入地址"></el-input>
        </el-form-item>
        <el-form-item label="体检项目列表" prop="examinations">
          <el-transfer  :titles="['体检项目', '套餐体检项目']" filterable :button-texts="['移除', '添加']"
                        :filter-method="filterMethod" v-model="data.packages" :data="data.transferData" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="套餐体检项目列表" v-model="data.formVisible1" width="50%" destroy-on-close>
      <el-table border stripe :data="data.examinationList">
        <el-table-column label="项目名" prop="name"></el-table-column>
        <el-table-column label="项目类型" prop="examinationTypeName"></el-table-column>
        <el-table-column label="所属科室" prop="officeName"></el-table-column>
        <el-table-column label="项目价格" prop="money"></el-table-column>
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


const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  formVisible: false,
  form: {},
  tableData: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  name: null,
  ids: [],
  doctorList: [],
  packages: [],
  transferData: [],
  examinationList: [],
  formVisible1: false
})

const viewExaminationList = (examinationList) => {
  data.examinationList = examinationList
  data.formVisible1 = true
  console.log(examinationList)
}

const baseUrl = import.meta.env.VITE_BASE_URL
const handleFileUpload = (res) => {
  data.form.cover = res.data
}

const filterMethod = (query, item) => {
  return item.label.includes(query)
}

request.get('/doctor/selectAll').then(res => {
  data.doctorList = res.data
})

request.get('/physicalExamination/selectAll').then(res => {
  data.transferData = []
  res.data.forEach(item => {
    data.transferData.push({ key: item.id, label: item.name })
  })
})

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
const handleAdd = () => {
  data.form = {}
  data.packages = []
  data.formVisible = true
}
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.packages = JSON.parse(data.form.examinations || '[]')  // 把数据库的json数组字符串 转成json数组
  data.formVisible = true
}
const add = () => {
  request.post('/examinationPackage/add', data.form).then(res => {
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
  request.put('/examinationPackage/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    }
  })
}

const save = () => {
  // 从数组转成 json数组字符串  存储到数据库
  data.form.examinations = JSON.stringify(data.packages)
  data.form.id ? update() : add()
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/examinationPackage/delete/' + id).then(res => {
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
    request.delete("/examinationPackage/delete/batch", {data: data.ids}).then(res => {
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