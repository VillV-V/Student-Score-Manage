<script lang="ts" setup>
import { computed, ref, watch, h } from "vue"
import { useUserStore } from "@/store/modules/user"
import { addTableDataApi, delTableDataApi, delBatchesDataApi, updateTableDataApi, getTablePageDataApi, exportFileApi } from "@/api/teacher"
import { type TeacherData } from "@/api/teacher/types/teacher"
import { getSchoolDataApi } from "@/api/school"
import { type SchoolData } from "@/api/school/types/school"
import { type FormInstance, type FormRules, ElMessage, ElMessageBox, UploadInstance } from "element-plus"
import { Refresh, CirclePlus, Delete, Download, RefreshRight, Upload, UploadFilled, Search } from "@element-plus/icons-vue"
import { usePagination } from "@/hooks/usePagination"
import { cloneDeep } from "lodash-es"

defineOptions({
  // 命名当前组件
  name: "Teacher"
})

const loading = ref<boolean>(false)
const { paginationData, handleCurrentChange, handleSizeChange } = usePagination()

//#region 增
const DEFAULT_FORM_DATA: TeacherData = {
  id: undefined,
  teacherId: 0,
  teacherName: "",
  gender: "",
  title: "无",
  telephone: "",
  email: "",
  schoolId: undefined,
  schoolName: "",
  password: ""
}
const dialogVisible = ref<boolean>(false)
const formRef = ref<FormInstance | null>(null)
const formData = ref<TeacherData>(cloneDeep(DEFAULT_FORM_DATA))
//校验手机号
const validatePhone = (rule: any, value: any, callback: any) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!value) {
    callback(new Error("手机号不能为空"))
  } else if (!phoneRegex.test(value)) {
    callback(new Error("请输入正确的手机号"))
  } else {
    callback() // 校验成功
  }
}
//校验邮箱
const validateEmail = (rule: any, value: any, callback: any) => {
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  if (!value) {
    callback(new Error("邮箱不能为空"))
  } else if (!emailRegex.test(value)) {
    callback(new Error("请输入有效的邮箱地址"))
  } else {
    callback()
  }
}
//校验密码
const validatePass = (rule: any, value: any, callback: any) => {
  const passRegex = /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d]{6,16}$/
  if (!value) {
    callback(new Error("请输入密码"))
  } else if (value.length < 6) {
    callback(new Error("密码至少为6个字符"))
  } else if (value.length > 16) {
    callback(new Error("密码不能超过16个字符"))
  } else if (!passRegex.test(value)) {
    callback(new Error("密码必须包含字母和数字"))
  } else {
    callback()
  }
}
const formRules: FormRules<TeacherData> = {
  teacherName: [{ required: true, trigger: "blur", message: "请输入姓名" }],
  gender: [{ required: true, trigger: "blur", message: "请输入性别" }],
  title: [{ required: true, trigger: "blur", message: "请输入职称" }],
  telephone: [{ required: true, validator: validatePhone, trigger: "blur" }],
  email: [{ required: true, validator: validateEmail, trigger: "blur" }],
  schoolName: [{ required: true, trigger: "blur", message: "请选择所属学院" }],
  password: [{ required: true, validator: validatePass, trigger: "blur" }]
}
const title = ref<string>("")
const handleAdd = () => {
  getSchoolData()
  title.value = "新增教师"
  dialogVisible.value = true
}
const handleCreateOrUpdate = () => {
  formRef.value?.validate((valid: boolean, fields) => {
    if (!valid) return console.error("表单校验不通过", fields)
    loading.value = true
    const api = formData.value.id === undefined ? addTableDataApi : updateTableDataApi
    api(formData.value)
      .then((res) => {
        ElMessage.success(res.message)
        dialogVisible.value = false
        getTableData()
      })
      .finally(() => {
        loading.value = false
      })
  })
}
const resetForm = () => {
  isLock.value = false
  formRef.value?.clearValidate()
  formData.value = cloneDeep(DEFAULT_FORM_DATA)
}
//#endregion

//#region 删
const handleDelete = (row: TeacherData) => {
  ElMessageBox.confirm(`正在删除教师：${row.teacherName}，确认删除？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    delTableDataApi(row.teacherId).then(() => {
      ElMessage.success("删除成功")
      getTableData()
    })
  })
}
//批量删除
//获取选中的数据
const selectArr = ref<number[]>([])
const handleSelectionChange = (val: TeacherData[]) => {
  const arr: number[] = []
  val.forEach((item) => {
    arr.push(item.teacherId)
  })
  selectArr.value = arr
}
const delBatches = () => {
  if (selectArr.value.length > 0) {
    ElMessageBox.confirm(`确认删除？`, "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    }).then(() => {
      delBatchesDataApi(selectArr.value).then(() => {
        ElMessage.success("删除成功")
        getTableData()
      })
    })
  } else {
    ElMessage.warning("请选择要删除的行")
  }
}
const isLock = ref<boolean>(false)
//#region 改
const handleUpdate = (row: TeacherData) => {
  getSchoolData()
  dialogVisible.value = true
  formData.value = cloneDeep(row)
  title.value = "修改教师信息"
  formData.value.id = "修改教师信息"
}
//修改密码
const UpdatePwd = (row: TeacherData) => {
  isLock.value = true
  dialogVisible.value = true
  formData.value = cloneDeep(row)
  title.value = "修改教师密码"
  formData.value.id = "修改教师密码"
}

//获取院系列表
const schoolData = ref<SchoolData[]>([])
const getSchoolData = () => {
  getSchoolDataApi()
    .then(({ data }) => {
      schoolData.value = data.records
    })
    .catch(() => {
      schoolData.value = []
    })
}
//#region 查
const tableData = ref<TeacherData[]>([])
const keyword = ref("")
const getTableData = () => {
  loading.value = true
  getTablePageDataApi({
    currentPage: paginationData.currentPage,
    size: paginationData.pageSize,
    keyword: keyword.value
  })
    .then(({ data }) => {
      paginationData.total = data.total
      tableData.value = data.records
    })
    .catch(() => {
      tableData.value = []
    })
    .finally(() => {
      loading.value = false
    })
}

//筛选性别
const gender = [
  { text: '男', value: '男' },
  { text: '女', value: '女' }
]
const filterGender = (value: string, row: TeacherData) => {
  return row.gender === value;
}
//筛选学院
const school = ref([
  { text: "", value: "" }
])
const schoolFilter = computed(
  () => {
    const schoolSet = new Set(tableData.value.map(item => item.schoolName))
    return school.value = Array.from(schoolSet).map(schoolName => ({
      text: schoolName,
      value: schoolName,
    }));
  }
)
const filterSchool = (value: string, row: TeacherData) => {
  return row.schoolName === value;
}

const resetSearch = () => {
  keyword.value = ""
  getTableData()
}
const saveName = (e: string) => {
  formData.value.schoolName = e
}

/** 导入文件 */
const userStore = useUserStore()
const token = userStore.token
const config = {
  'Authorization': `Bearer ${token}`,  // 假设你有一个 token
}
const uploadVisible = ref<boolean>(false)
const fileList = ref([])
//上传文件之前先判断该文件是否是Excel文件
const beforeUpload = (file: any) => {
  const isExcel =
    file.type === "application/vnd.ms-excel" ||
    file.type === "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
  const isLt1M = file.size / 1024 / 1024 < 10
  if (!isExcel)
    ElMessageBox({
      title: "温馨提示",
      message: "上传文件只能是 xls / xlsx 格式！",
      type: "warning"
    })
  if (!isLt1M)
    ElMessageBox({
      title: "温馨提示",
      message: "上传文件大小不能超过10MB!",
      type: "warning"
    })
  return isExcel && isLt1M
}
// 文件数超出提示
const exceedFile = () => {
  ElMessage.warning("最多只能上传一个文件！")
}
//上传成功
const successUpload = (res: any) => {
  ElMessage.success("上传成功")
  getTableData()
  ElMessageBox({
    title: "导入结果",
    message: h("p", null, [
      h("p", null, "导入总数：" + res.data.totalRows + "条"),
      h("p", null, "导入成功：" + res.data.successRows + "条"),
      h("p", null, "导入失败：" + res.data.errorMsgList.length + "条"),
      h("div", null, [
        h("p", null, "失败原因："),
        h("p", null, res.data.errorMsgList.length > 0?res.data.errorMsgList.map(
          (item: string) => h('p', { class: 'item' }, item)):"无"
        )
      ])
    ])
  })
}
//上传失败
const errorUpload = () => {
  ElMessage.error("上传失败")
}
//关闭时清空文件列表
const resetFile = () => {
  fileList.value = []
}
// 文件上传
const uploadRef = ref<UploadInstance>()
const uploadFile = () => {
  uploadRef.value!.submit()
}
/** 导出文件 */
const download = (res: any) => {
  // 创建一个 Blob URL
  const url = window.URL.createObjectURL(new Blob([res]));
  // 创建一个隐藏的下载链接
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', 'example.xlsx'); // 设置下载文件名
  document.body.appendChild(link);
  // 触发点击事件，开始下载
  link.click();
  // 清理资源
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}
const exportFile = () => {
  exportFileApi().then((res) => {
    download(res)
  }).catch(error => {
    ElMessage.error('导出失败');
  })
}

/** 下载模板 */
const downloadTemplate = () => {
  window.open("http://localhost:3333/api/download?fileName=教师信息表")
}

/** 监听分页参数的变化 */
watch([() => paginationData.currentPage, () => paginationData.pageSize], getTableData, { immediate: true })
</script>

<template>
  <div class="app-container">
    <el-card v-loading="loading" shadow="never" class="search-wrapper">
      <el-form :inline="true">
        <el-form-item prop="username" label="关键词">
          <el-input v-model="keyword" placeholder="请输入关键词" />
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" @click="getTableData">查询</el-button>
          <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card v-loading="loading" shadow="never">
      <div class="toolbar-wrapper">
        <div>
          <el-button type="primary" :icon="CirclePlus" @click="handleAdd">新增教师</el-button>
          <el-button type="danger" :icon="Delete" @click="delBatches">批量删除</el-button>
        </div>
        <div>
          <el-button type="primary" @click="downloadTemplate">下载模板</el-button>
          <el-tooltip content="导入">
            <el-button type="primary" :icon="Upload" circle @click="uploadVisible = true" />
          </el-tooltip>
          <el-tooltip content="导出">
            <el-button type="primary" :icon="Download" circle @click="exportFile" />
          </el-tooltip>
          <el-tooltip content="刷新当前页">
            <el-button type="primary" :icon="RefreshRight" circle @click="getTableData" />
          </el-tooltip>
        </div>
      </div>
      <div class="table-wrapper">
        <el-table :data="tableData" @selection-change="handleSelectionChange">
          <el-table-column type="selection" fixed width="50" align="center" />
          <el-table-column prop="teacherId" label="教师工号" sortable align="center" />
          <el-table-column prop="teacherName" label="姓名" align="center" />
          <el-table-column prop="gender" label="性别" align="center" :filters="gender" :filter-method="filterGender" />
          <el-table-column prop="title" label="职称" align="center" />
          <el-table-column prop="telephone" label="手机号" align="center" />
          <el-table-column prop="email" label="邮箱" align="center" />
          <el-table-column prop="schoolName" label="所属学院" align="center" :filters="schoolFilter" :filter-method="filterSchool" />
          <el-table-column fixed="right" label="操作" width="210" align="center">
            <template #default="scope">
              <el-button type="primary" text bg size="small" @click="handleUpdate(scope.row)">修改</el-button>
              <el-button type="primary" text bg size="small" @click="UpdatePwd(scope.row)">修改密码</el-button>
              <el-button type="danger" text bg size="small" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pager-wrapper">
        <el-pagination
          background
          :layout="paginationData.layout"
          :page-sizes="paginationData.pageSizes"
          :total="paginationData.total"
          :page-size="paginationData.pageSize"
          :currentPage="paginationData.currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    <!-- 文件上传 -->
    <el-dialog v-model="uploadVisible" title="文件上传" width="30%" @closed="resetFile">
      <el-upload
        ref="uploadRef"
        action="http://localhost:3333/api/importTeacher"
        class="upload-demo"
        drag
        accept=".xlsx, .xls"
        :file-list="fileList"
        :auto-upload="false"
        :on-exceed="exceedFile"
        :before-upload="beforeUpload"
        :on-success="successUpload"
        :on-error="errorUpload"
        :show-file-list="true"
        :headers="config"
        :limit="1"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">请上传 .xlsx 标准格式文件，文件大小不能超过10MB</div>
        </template>
      </el-upload>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="uploadVisible = false">取消</el-button>
          <el-button type="primary" @click="uploadFile">确认</el-button>
        </span>
      </template>
    </el-dialog>
    <!-- 新增/修改 -->
    <el-dialog
      v-model="dialogVisible"
      :title="title"
      @closed="resetForm"
      width="30%"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px" label-position="left">
        <el-form-item prop="teacherId" label="教师工号" v-if="formData.id !== undefined">
          <el-input v-model="formData.teacherId" disabled />
        </el-form-item>
        <el-form-item prop="teacherName" label="姓名">
          <el-input v-model="formData.teacherName" placeholder="请输入姓名" :disabled="isLock"/>
        </el-form-item>
        <el-form-item prop="gender" label="性别">
          <el-select v-model="formData.gender" placeholder="请选择性别" clearable :disabled="isLock">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item prop="title" label="职称">
          <el-input v-model="formData.title" placeholder="请输入职称" :disabled="isLock"/>
        </el-form-item>
        <el-form-item prop="telephone" label="手机号">
          <el-input v-model="formData.telephone" placeholder="请输入手机号" :disabled="isLock"/>
        </el-form-item>
        <el-form-item prop="email" label="邮箱">
          <el-input v-model="formData.email" placeholder="请输入邮箱" :disabled="isLock"/>
        </el-form-item>
        <el-form-item prop="schoolName" label="所属学院">
          <el-select v-model="formData.schoolId" placeholder="请选择所属学院" filterable clearable @change="saveName" :disabled="isLock">
            <el-option v-for="item in schoolData" :key="item.id" :label="item.schoolName" :value="item.schoolId" />
          </el-select>
        </el-form-item>
        <el-form-item prop="password" label="密码" v-if="formData.id != '修改教师信息'">
          <el-input type="password" v-model="formData.password" placeholder="请输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateOrUpdate" :loading="loading">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.search-wrapper {
  margin-bottom: 20px;

  :deep(.el-card__body) {
    padding-bottom: 2px;
  }
}

.toolbar-wrapper {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.table-wrapper {
  margin-bottom: 20px;
}

.pager-wrapper {
  display: flex;
  justify-content: flex-end;
}
</style>
