<script lang="ts" setup>
import { computed, ref, watch, h } from "vue"
import { useUserStore } from "@/store/modules/user"
import { addCourseDataApi, delCourseDataApi, delBatchesDataApi, updateCourseDataApi, getCoursePageDataApi, exportFileApi } from "@/api/course"
import { type CourseData } from "@/api/course/types/course"
import { getQuantitativeDataApi } from "@/api/quantitative"
import { type QuantitativeData } from "@/api/quantitative/types/quantitative"
import { getSchoolDataApi } from "@/api/school"
import { type SchoolData } from "@/api/school/types/school"
import { type FormInstance, type FormRules, ElMessage, ElMessageBox, UploadInstance } from "element-plus"
import { Refresh, CirclePlus, Delete, Upload, Download, RefreshRight, Search } from "@element-plus/icons-vue"
import { usePagination } from "@/hooks/usePagination"
import { cloneDeep } from "lodash-es"

defineOptions({
  // 命名当前组件
  name: "Course"
})

const loading = ref<boolean>(false)
const { paginationData, handleCurrentChange, handleSizeChange } = usePagination()

//#region 增
const DEFAULT_FORM_DATA: CourseData = {
  id: undefined,
  courseId: 0,
  courseName: "",
  type: "",
  schoolId: undefined,
  schoolName: "",
  classPeriod: 0,
  practicalClassPeriod: 0,
  qid: undefined,
  qname: ""
}
const dialogVisible = ref<boolean>(false)
const formRef = ref<FormInstance | null>(null)
const formData = ref<CourseData>(cloneDeep(DEFAULT_FORM_DATA))
//校验课时
const validateCPeriod = (rule: any, value: any, callback: any) => {
  if (value == 0 || value < 0) {
    callback(new Error("总课时不能小于等于0"))
  } else {
    callback()
  }
}
const validatePeriod = (rule: any, value: any, callback: any) => {
  if (value == 0 || value < 0) {
    callback(new Error("实践课时不能小于等于0"))
  } else if (value > formData.value.classPeriod) {
    callback(new Error("实践课时不能大于总课时"))
  } else {
    callback()
  }
}
const formRules: FormRules<CourseData> = {
  courseName: [{ required: true, trigger: "blur", message: "请输入课程名称" }],
  type: [{ required: true, trigger: "change", message: "请选择课程类型" }],
  schoolName: [{ required: true, trigger: "change", message: "请选择所属学院" }],
  classPeriod: [{ required: true, validator: validateCPeriod, trigger: "blur" }],
  practicalClassPeriod: [{ required: true, validator: validatePeriod, trigger: "blur" }],
  qname: [{ required: true, trigger: "change", message: "请选择成绩量化标准" }],
}
const changeQ = () => {
  console.log("1")
  if(formData.value.id !== undefined) {
    return
  }
  console.log("2")
  let type = formData.value.type
  let cp = formData.value.classPeriod
  let pcp = formData.value.practicalClassPeriod
  if(cp == 0 || pcp == 0) {
    return
  } else if(pcp > cp) {
    return
  }
  let num = pcp /cp
  if(num <= 0.1) {
    formData.value.qname = "A"
  } else if(num > 0.1 && num <= 0.3) {
    formData.value.qname = "B"
  } else if(num > 0.3 && num <= 0.5) {
    formData.value.qname = "C"
  } else if(num > 0.5) {
    formData.value.qname = "D"
  }
  if(type == "综合实践") {
    formData.value.qname = "E"
  }
  if(formData.value.courseName.includes("大学英语")) {
    formData.value.qname = "F"
  }
  console.log("3")
}
//watch([() => formData.value.type, () => formData.value.classPeriod, () => formData.value.practicalClassPeriod], changeQ)
const handleAdd = () => {
  getSchoolData()
  getQuantitativData()
  dialogVisible.value = true
}
const handleCreateOrUpdate = () => {
  formRef.value?.validate((valid: boolean, fields) => {
    if (!valid) return console.error("表单校验不通过", fields)
    loading.value = true
    const api = formData.value.id === undefined ? addCourseDataApi : updateCourseDataApi
    if(oldCourseName.value === formData.value.courseName) {
      formData.value.courseName = ""
    }
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
  formRef.value?.clearValidate()
  formData.value = cloneDeep(DEFAULT_FORM_DATA)
}
//#endregion

//#region 删
const handleDelete = (row: CourseData) => {
  ElMessageBox.confirm(`正在删除课程：${row.courseName}，确认删除？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    delCourseDataApi(row.courseId).then(() => {
      ElMessage.success("删除成功")
      getTableData()
    })
  })
}
//批量删除
//获取选中的数据
const selectArr = ref<number[]>([])
const handleSelectionChange = (val: CourseData[]) => {
  let arr: number[] = [] 
  val.forEach((item) => {
    arr.push(item.courseId)
  })
  selectArr.value = arr
}
const delBatches = () => {
  if(selectArr.value.length > 0) {
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
//#endregion

//#region 改
const oldCourseName = ref<string>("")
const handleUpdate = (row: CourseData) => {
  getSchoolData()
  getQuantitativData()
  dialogVisible.value = true
  formData.value = cloneDeep(row)
  oldCourseName.value = formData.value.courseName
  formData.value.id = "修改课程信息"
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
//获取成绩评价标准
const quantitativeData = ref<QuantitativeData[]>([])
const getQuantitativData = () => {
  getQuantitativeDataApi()
    .then(({ data }) => {
      quantitativeData.value = data.records
    })
    .catch(() => {
      quantitativeData.value = []
    })
}
//#region 查
const tableData = ref<CourseData[]>([])
const keyword = ref("")
const getTableData = () => {
  loading.value = true
  getCoursePageDataApi({
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

//筛选课程类型
const type = ref([
  { text: "", value: "" }
])
const typeFilter = computed(
  () => {
    const schoolSet = new Set(tableData.value.map(item => item.type))
    return type.value = Array.from(schoolSet).map(type => ({
      text: type,
      value: type,
    }));
  }
)
const filterType = (value: string, row: CourseData) => {
  return row.type === value;
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
const filterSchool = (value: string, row: CourseData) => {
  return row.schoolName === value;
}
//筛选成绩量化标准
const qname = ref([
  { text: "", value: "" }
])
const qnameFilter = computed(
  () => {
    const schoolSet = new Set(tableData.value.map(item => item.qname))
    return school.value = Array.from(schoolSet).map(qname => ({
      text: qname,
      value: qname,
    }));
  }
)
const filterQname = (value: string, row: CourseData) => {
  return row.qname === value;
}

const resetSearch = () => {
  keyword.value = ""
  getTableData()
}
const saveName = (e: string) => {
  formData.value.schoolName = e
}

/** 导入文件 */
//设置请求头
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
  window.open("http://localhost:3333/api/download?fileName=课程信息表")
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
          <el-button type="primary" :icon="CirclePlus" @click="handleAdd">新增</el-button>
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
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column prop="courseId" label="课程编号" sortable align="center" />
          <el-table-column prop="courseName" label="课程名称" align="center" />
          <el-table-column prop="type" label="课程类型" align="center" :filters="typeFilter" :filter-method="filterType" />
          <el-table-column prop="schoolName" label="所属学院" align="center" :filters="schoolFilter" :filter-method="filterSchool" />
          <el-table-column prop="classPeriod" label="总课时" align="center" />
          <el-table-column prop="practicalClassPeriod" label="实践课时" align="center" />
          <el-table-column prop="qname" label="成绩量化标准" align="center" :filters="qnameFilter" :filter-method="filterQname" />
          <el-table-column fixed="right" label="操作" width="150" align="center">
            <template #default="scope">
              <el-button type="primary" text bg size="small" @click="handleUpdate(scope.row)">修改</el-button>
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
        action="http://localhost:3333/api/importCourse"
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
      :title="formData.id === undefined ? '新增课程' : '修改课程名称'"
      @closed="resetForm"
      width="30%"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="106px" label-position="left">
        <el-form-item prop="courseId" label="课程编号" v-if="formData.id === '修改课程名称'">
          <el-input v-model="formData.courseId" disabled/>
        </el-form-item>
        <el-form-item prop="courseName" label="课程名称">
          <el-input v-model="formData.courseName" placeholder="请输入" @blur="changeQ" />
        </el-form-item>
        <el-form-item prop="type" label="课程类型">
          <el-select v-model="formData.type" placeholder="请选择课程类型" clearable @change="changeQ">
            <el-option label="公共基础必修课" value="公共基础必修课" />
            <el-option label="公共基础选修课" value="公共基础选修课" />
            <el-option label="专业基础课" value="专业基础课" />
            <el-option label="专业核心课" value="专业核心课" />
            <el-option label="专业选修（拓展）课	" value="专业选修（拓展）课	" />
            <el-option label="综合实践" value="综合实践" />
          </el-select>
        </el-form-item>
        <el-form-item prop="schoolName" label="所属学院">
          <el-select v-model="formData.schoolId" placeholder="请选择所属学院" clearable @change="saveName">
            <el-option v-for="item in schoolData" :label=item.schoolName :value=item.schoolId />
          </el-select>
        </el-form-item>
        <el-form-item prop="classPeriod" label="总课时">
          <el-input-number v-model="formData.classPeriod" :min="0" @change="changeQ" />
        </el-form-item>
        <el-form-item prop="practicalClassPeriod" label="实践课时">
          <el-input-number v-model="formData.practicalClassPeriod" :min="0" @change="changeQ" />
        </el-form-item>
        <el-form-item prop="qname" label="成绩量化标准">
          <el-select v-model="formData.qid" placeholder="请选择成绩量化标准" clearable>
            <el-option v-for="item in quantitativeData" :label=item.qname :value=item.qid />
          </el-select>
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
