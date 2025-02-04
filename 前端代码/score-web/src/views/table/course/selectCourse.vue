<script lang="ts" setup>
import { computed, ref, watch, h } from "vue"
import { useUserStore } from "@/store/modules/user"
import { getStuClassPageDataApi } from "@/api/stu-class"
import { type StuClassData } from "@/api/stu-class/types/stu-class"
import { getTableGroupDataApi } from "@/api/teacher"
import { type TeacherGroupData } from "@/api/teacher/types/teacher"
import { getCourseGroupDataApi } from "@/api/course"
import { type CourseGroupData } from "@/api/course/types/course"
import { addSCourseDataApi, delSCourseDataApi, delBatchesDataApi, updateSCourseDataApi, getSCourseByIdDataApi, exportFileApi } from "@/api/select-course"
import { type SCourseData } from "@/api/select-course/types/select-course"
import { type FormInstance, type FormRules, ElMessage, ElMessageBox, UploadInstance } from "element-plus"
import { Refresh, Upload, Download, RefreshRight, Search } from "@element-plus/icons-vue"
import { usePagination } from "@/hooks/usePagination"
import { cloneDeep } from "lodash-es"

defineOptions({
  // 命名当前组件
  name: "SelectCourse"
})

const loading = ref<boolean>(false)
const { paginationData, handleCurrentChange, handleSizeChange } = usePagination()

//#region 增
const DEFAULT_FORM_DATA: SCourseData = {
  type: undefined,
  id: 0,
  classId: 0,
  className: "",
  courseId: undefined,
  courseName: "",
  qname: "",
  teacherId: undefined,
  teacherName: "",
  date: ""
}
const year = ref<string>("")
const semester = ref<string>("")
// 当前年份
const currentYear = new Date().getFullYear();
// 生成学年范围
const academicYears = ref<string[]>([]);
for (let year = currentYear; year < currentYear + 6; year++) {
  academicYears.value.push(`${year}-${year + 1}`);
}
const changeDate = () => {
  formData.value.date = year.value + '年' + semester.value
}

const dialogVisible = ref<boolean>(false)
const formRef = ref<FormInstance | null>(null)
const formData = ref<SCourseData>(cloneDeep(DEFAULT_FORM_DATA))
//校验学期
const validateDate = (rule: any, value: any, callback: any) => {
  if (!year.value) {
    callback(new Error("学年不能为空"))
  } else if (!semester.value) {
    callback(new Error("学期不能为空"))
  }  else if (!value) {
    callback(new Error("上课时间不能为空"))
  } else {
    callback()
  }
}
const formRules: FormRules<SCourseData> = {
  courseName: [{ required: true, trigger: "blur", message: "请选择课程名称" }],
  teacherName: [{ required: true, trigger: "blur", message: "请选择任课教师姓名" }],
  date: [{ required: true, validator: validateDate, trigger: "blur" }],
}
const handleAdd = (row: SCourseData) => {
  year.value = ""
  semester.value = ""
  dialogVisible.value = true
  formData.value = cloneDeep(row)
  getTeacherData()
  getCourseGroupData()
}
const handleCreateOrUpdate = () => {
  formRef.value?.validate((valid: boolean, fields) => {
    if (!valid) return console.error("表单校验不通过", fields)
    loading.value = true
    const api = formData.value.type === undefined ? addSCourseDataApi : updateSCourseDataApi
    if(oldCourseName.value === formData.value.courseName) {
      formData.value.courseId = undefined
    }
    api(formData.value)
      .then((res) => {
        ElMessage.success(res.message)
        dialogVisible.value = false
        getSelectCourseData(formData.value.classId)
      })
      .finally(() => {
        loading.value = false
      })
  })
}
const resetForm = () => {
  formRef.value?.clearValidate()
  formData.value = cloneDeep(DEFAULT_FORM_DATA)
  year.value = ""
  semester.value = ""
}
//#endregion

//#region 删
const handleDelete = (row: SCourseData) => {
  ElMessageBox.confirm(`正在删除，确认删除？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    delSCourseDataApi(row.id).then(() => {
      ElMessage.success("删除成功")
      getSelectCourseData(row.classId)
    })
  })
}
//批量删除
//获取选中的数据
const selectArr = ref<number[]>([])
const handleSelectionChange = (val: SCourseData[]) => {
  let arr: number[] = []
  val.forEach((item) => {
    arr.push(item.id)
  })
  selectArr.value = arr
}
const delBatches = (row: SCourseData) => {
  if (selectArr.value.length > 0) {
    ElMessageBox.confirm(`确认删除？`, "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    }).then(() => {
      delBatchesDataApi(selectArr.value).then(() => {
        ElMessage.success("删除成功")
        getSelectCourseData(row.classId)
      })
    })
  } else {
    ElMessage.warning("请选择要删除的行")
  }
}
//#endregion

//#region 改
const oldCourseName = ref<string>("")
const handleUpdate = (row: SCourseData) => {
  dialogVisible.value = true
  getTeacherData()
  getCourseGroupData()
  formData.value = cloneDeep(row)
  let arr = formData.value.date.split("年")
  year.value = arr[0]
  semester.value = arr[1]
  console.log(arr)
  oldCourseName.value = formData.value.courseName
  formData.value.type = "修改选课信息"
}
//#region 查
//查询班级信息
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
//查询选课信息
const selectCourseData = ref<SCourseData[]>([])
const getSelectCourseData = (id: number) => {
  loading.value = true
  getSCourseByIdDataApi({
    classId: id
  })
    .then(({ data }) => {
      selectCourseData.value = data.records
    })
    .catch(() => {
      selectCourseData.value = []
    })
    .finally(() => {
      loading.value = false
    })
}
//查询教师信息
const TeacherListData = ref<TeacherGroupData[]>([])
const getTeacherData = () => {
  getTableGroupDataApi()
    .then(({ data }) => {
      TeacherListData.value = data.records
    })
    .catch(() => {
      TeacherListData.value = []
    })
}
const saveTName = (e: string) => {
  formData.value.teacherName = e
}
//查询课程信息
const CourseListData = ref<CourseGroupData[]>([])
const getCourseGroupData = () => {
  getCourseGroupDataApi()
    .then(({ data }) => {
      CourseListData.value = data.records
    })
    .catch(() => {
      CourseListData.value = []
    })
}
const saveCName = (e: string) => {
  formData.value.courseName = e
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
  expands.value = []
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
  window.open("http://localhost:3333/api/download?fileName=选课信息表")
}

/** 监听分页参数的变化 */
watch([() => paginationData.currentPage, () => paginationData.pageSize], getTableData, { immediate: true })

/** 设置表格展开时只展开一行 */
// 用于存储展开行的唯一标识符
const getRowKeys = (row: SCourseData) => {
  return row.classId.toString()
}
// 当前展开的行的 key
const expands = ref<string[]>([]);
// 处理行展开和折叠事件
const expandChangeHandler = (row: SCourseData, expandedRows: any) => {
  if(expandedRows.length) {
    expands.value = []
    if(row) {
      // 展开某一行时，仅保留该行的key
      expands.value.push(row.classId.toString())
    }
  } else {
    // 折叠该行时，清空展开行
    expands.value = []
  }
}
// 处理点击行事件
const handleRowClick = (row: SCourseData) => {
  //展开时获取对应班级的选课信息
  getSelectCourseData(row.classId)
  const key = row.classId.toString()  // 获取行的 key
  if (expands.value.includes(key)) {
    // 如果该行已展开，折叠该行
    expands.value = [];
  } else {
    // 否则展开该行并折叠其他行
    expands.value = [key];
  }
}

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
        <el-table 
          :data="tableData" 
          :expand-row-keys="expands" 
          :row-key="getRowKeys" 
          @expand-change="expandChangeHandler"
          @row-click="handleRowClick">  <!-- 监听行点击事件 -->
          <el-table-column width="50" align="center" />
          <el-table-column prop="classId" label="班级编号" sortable align="center" />
          <el-table-column prop="className" label="班级名称" align="center" />
          <el-table-column prop="grade" label="年级" align="center" :filters="gradeFilter" :filter-method="filterGrade" />
          <el-table-column prop="majorName" label="所属专业" align="center" :filters="majorFilter" :filter-method="filterMajor" />
          <el-table-column type="expand">
            <template #default="scope">
              <div class="box">
                <el-table :data="selectCourseData" @selection-change="handleSelectionChange" :border="true">
                  <el-table-column label="课程安排表" align="center">
                    <el-table-column type="selection" width="80" align="center" />
                    <!-- <el-table-column label="编号" prop="id" align="center" /> -->
                    <el-table-column label="课程编号" prop="courseId" align="center" />
                    <el-table-column label="课程名称" prop="courseName" align="center" />
                    <el-table-column label="成绩量化标准" prop="qname" align="center" />
                    <el-table-column label="任课教师工号" prop="teacherId" align="center" />
                    <el-table-column label="任课教师" prop="teacherName" align="center" />
                    <el-table-column label="学期" prop="date" width="200" align="center" />
                    <el-table-column fixed="right" label="操作" width="150" align="center">
                      <template #default="props">
                        <el-button type="primary" text bg size="small" @click="handleUpdate(props.row)">修改</el-button>
                        <el-button type="danger" text bg size="small" @click="handleDelete(props.row)">删除</el-button>
                      </template>
                    </el-table-column>
                  </el-table-column>
                </el-table>
                <div class="box-btn">
                  <el-button type="primary" @click="handleAdd(scope.row)">新增</el-button>
                  <el-button type="danger" @click="delBatches(scope.row)">批量删除</el-button>
                </div>
              </div>
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
        action="http://localhost:3333/api/importSCourse"
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
      :title="formData.type === undefined ? '新增选课' : '修改选课信息'"
      @closed="resetForm"
      width="30%"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px" label-position="left">
        <el-form-item prop="classId" label="班级编号">
          <el-input v-model="formData.classId" disabled/>
        </el-form-item>
        <el-form-item prop="className" label="班级名称">
          <el-input v-model="formData.className" disabled/>
        </el-form-item>
        <el-form-item prop="courseName" label="课程">
          <el-select v-model="formData.courseId" placeholder="请选择课程" filterable clearable @change="saveCName">
            <el-option-group
              v-for="group in CourseListData"
              :key="group.schoolName"
              :label="group.schoolName">
              <el-option
                v-for="item in group.courseList"
                :key="item.courseId"
                :label="item.courseName"
                :value="item.courseId"/>
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item prop="teacherName" label="任课教师">
          <el-select v-model="formData.teacherId" placeholder="请选择任课教师" filterable clearable  @change="saveTName">
            <el-option-group
              v-for="group in TeacherListData"
              :key="group.schoolName"
              :label="group.schoolName">
              <el-option
                v-for="item in group.teacherList"
                :key="item.teacherId"
                :label="item.teacherName"
                :value="item.teacherId"/>
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item prop="date" label="上课时间">
          <el-select v-model="year" placeholder="学年" clearable style="max-width: 130px;" @change="changeDate">
            <el-option
            v-for="(year, index) in academicYears"
            :key="index"
            :label="year"
            :value="year"/>
          </el-select>
          <el-select v-model="semester" placeholder="学期" clearable style="max-width: 100px; margin-left: 20px;" @change="changeDate">
            <el-option label="上学期" value="上学期" />
            <el-option label="下学期" value="下学期" />
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
  flex-direction: row-reverse;
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

.box {
  width: 96%;
  margin: 20px auto 60px;
}

.box-btn {
  margin-top: 10px;
  display: flex;
  justify-content: center;
  align-items: center;    
}
</style>
