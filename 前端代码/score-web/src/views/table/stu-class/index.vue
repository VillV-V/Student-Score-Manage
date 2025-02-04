<script lang="ts" setup>
import { computed, ref, watch, h } from "vue"
import { useUserStore } from "@/store/modules/user"
import { addStuClassDataApi, delStuClassDataApi, delBatchesDataApi, updateStuClassDataApi, getStuClassPageDataApi, exportFileApi } from "@/api/stu-class"
import { type StuClassData } from "@/api/stu-class/types/stu-class"
import { getMajorDataApi } from "@/api/major"
import { type MajorData } from "@/api/major/types/major"
import { type FormInstance, type FormRules, ElMessage, ElMessageBox, UploadInstance } from "element-plus"
import { Refresh, CirclePlus, Delete, Upload, Download, RefreshRight, Search } from "@element-plus/icons-vue"
import { usePagination } from "@/hooks/usePagination"
import { cloneDeep } from "lodash-es"

defineOptions({
  // 命名当前组件
  name: "StuClass"
})

const loading = ref<boolean>(false)
const { paginationData, handleCurrentChange, handleSizeChange } = usePagination()

//#region 增
const DEFAULT_FORM_DATA: StuClassData = {
  id: undefined,
  classId: 0,
  className: "",
  grade: "",
  majorId: undefined,
  majorName: "",
}
const dialogVisible = ref<boolean>(false)
const formRef = ref<FormInstance | null>(null)
const formData = ref<StuClassData>(cloneDeep(DEFAULT_FORM_DATA))
const formRules: FormRules<StuClassData> = {
  className: [{ required: true, trigger: "blur", message: "请输入班级名称" }],
  grade: [{ required: true, trigger: "blur", message: "请输入年级" }],
  majorName: [{ required: true, trigger: "blur", message: "请输入所属专业名称" }],
}
const handleAdd = () => {
  getMajorData()
  dialogVisible.value = true
}
const handleCreateOrUpdate = () => {
  formRef.value?.validate((valid: boolean, fields) => {
    if (!valid) return console.error("表单校验不通过", fields)
    loading.value = true
    const api = formData.value.id === undefined ? addStuClassDataApi : updateStuClassDataApi
    if(oldClassName.value === formData.value.className) {
      formData.value.className = ""
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
const handleDelete = (row: StuClassData) => {
  ElMessageBox.confirm(`正在删除：${row.className}，确认删除？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    delStuClassDataApi(row.classId).then(() => {
      ElMessage.success("删除成功")
      getTableData()
    })
  })
}
//批量删除
//获取选中的数据
const selectArr = ref<number[]>([])
const handleSelectionChange = (val: StuClassData[]) => {
  let arr: number[] = [] 
  val.forEach((item) => {
    arr.push(item.classId)
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
const oldClassName = ref<string>("")
const handleUpdate = (row: StuClassData) => {
  getMajorData()
  dialogVisible.value = true
  formData.value = cloneDeep(row)
  oldClassName.value = formData.value.className
  formData.value.id = "修改专业名称"
}
//获取院系列表
const majorData = ref<MajorData[]>([])
  const getMajorData = () => {
  getMajorDataApi().then(({ data }) => {
    majorData.value = data.records
  }).catch(() => {
    majorData.value = []
  })
}
//#region 查
const tableData = ref<StuClassData[]>([])
const keyword = ref("")
const getTableData = () => {
  loading.value = true
  getStuClassPageDataApi({
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

//筛选年级
const grade = ref([
  { text: "", value: "" }
])
const gradeFilter = computed(
  () => {
    const gradeSet = new Set(tableData.value.map(item => item.grade))
    return grade.value = Array.from(gradeSet).map(grade => ({
      text: grade,
      value: grade,
    }));
  }
)
const filterGrade = (value: string, row: StuClassData) => {
  return row.grade === value;
}

//筛选专业
const major = ref([
  { text: "", value: "" }
])
const majorFilter = computed(
  () => {
    const majorSet = new Set(tableData.value.map(item => item.majorName))
    return major.value = Array.from(majorSet).map(majorName => ({
      text: majorName,
      value: majorName,
    }));
  }
)
const filterMajor = (value: string, row: StuClassData) => {
  return row.majorName === value;
}

const resetSearch = () => {
  keyword.value = ""
  getTableData()
}
const saveName = (e: string) => {
  formData.value.majorName = e
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
  window.open("http://localhost:3333/api/download?fileName=班级信息表")
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
        <el-table :data="tableData" @selection-change="handleSelectionChange" max-height="350">
          <el-table-column type="selection" fixed width="50" align="center" />
          <el-table-column prop="classId" label="班级编号" sortable align="center" />
          <el-table-column prop="className" label="班级名称" align="center" />
          <el-table-column prop="grade" label="年级" align="center" :filters="gradeFilter" :filter-method="filterGrade" />
          <el-table-column prop="majorName" label="所属专业" align="center" :filters="majorFilter" :filter-method="filterMajor" />
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
        action="http://localhost:3333/api/importStuClass"
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
      :title="formData.id === undefined ? '新增班级' : '修改班级名称'"
      @closed="resetForm"
      width="30%"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px" label-position="left">
        <el-form-item prop="classId" label="班级编号" v-if="formData.id === '修改班级名称'">
          <el-input v-model="formData.classId" disabled/>
        </el-form-item>
        <el-form-item prop="className" label="班级名称">
          <el-input v-model="formData.className" placeholder="请输入" />
        </el-form-item>
        <el-form-item prop="grade" label="年级">
          <el-input v-model="formData.grade" placeholder="请输入" />
        </el-form-item>
        <el-form-item prop="majorName" label="所属专业">
          <el-select v-model="formData.majorId" placeholder="请选择所属专业" filterable clearable @change="saveName">
            <el-option v-for="item in majorData" :label=item.majorName :value=item.majorId />
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
