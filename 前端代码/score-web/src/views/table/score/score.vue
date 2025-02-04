<script lang="ts" setup>
import { computed, ref, watch, h } from "vue"
import { useUserStore } from "@/store/modules/user"
import { getCoursePageDataApi, getCourseDataByTIdApi } from "@/api/course"
import { type CourseData } from "@/api/course/types/course"
import { getSCourseListDataByCoIdApi, getSCourseListDataByCTIdApi } from "@/api/select-course"
import { type SCourseData } from "@/api/select-course/types/select-course"
import { updateScoreDataApi, getScoreDataByCCIdApi, exportFileApi } from "@/api/score"
import { type ScoreData } from "@/api/score/types/score"
import { addSDDataApi, delSDDataApi, delBatchesDataApi, updateSDDataApi, getScoreDataBySCIdApi } from "@/api/score-detail"
import { type ScoreDetailData } from "@/api/score-detail/types/score-detail"
import { type FormInstance, type FormRules, ElMessage, ElMessageBox, UploadInstance } from "element-plus"
import { Refresh, Upload, Download, RefreshRight, Search, Top } from "@element-plus/icons-vue"
import { usePagination } from "@/hooks/usePagination"
import { cloneDeep } from "lodash-es"
import Bar from "@/components/Echarts/Bar.vue"

defineOptions({
  // 命名当前组件
  name: "Score"
})

//#region 增
const DEFAULT_FORM_DATA: ScoreData = {
  sid: 0,
  studentId: 0,
  studentName: "",
  classId: 0,
  className: "",
  courseId: 0,
  courseName: "",
  qname: "",
  usualScore: 0,
  skillScore: 0,
  disScore: 0,
  totalScore: 0,
}
const dialogVisible = ref<boolean>(false)  //学生成绩列表弹窗
const formRef = ref<FormInstance | null>(null)
const formData = ref<ScoreData>(cloneDeep(DEFAULT_FORM_DATA))
const formRules: FormRules<ScoreData> = {
  usualScore: [{ required: true, trigger: "blur", message: "请输入课堂表现成绩" }],
  skillScore: [{ required: true, trigger: "blur", message: "请输入技能成绩" }],
  disScore: [{ required: true, trigger: "blur", message: "请输入纪律成绩" }],
}
const scoreDialog = ref<boolean>(false)  //新增修改成绩弹窗
const handleCreateOrUpdate = () => {
  formRef.value?.validate((valid: boolean, fields) => {
    if (!valid) return console.error("表单校验不通过", fields)
    loading.value = true
    updateScoreDataApi(formData.value)
      .then((res) => {
        ElMessage.success(res.message)
        scoreDialog.value = false
        getStudentData(formData.value.classId, formData.value.courseId)
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

//#region 改
const handleUpdate = (row: ScoreData) => {
  scoreDialog.value = true
  formData.value = cloneDeep(row)
}

const userStore = useUserStore()
const isAdmin = userStore.roles.includes("admin")
const ID = userStore.id
// 加载动画
const loading = ref<boolean>(false)
const classloading = ref<boolean>(false)
const { paginationData, handleCurrentChange, handleSizeChange } = usePagination()


//#region 查
//查询课程信息
const courseData = ref<CourseData[]>([])
const keyword = ref("")
const getCourseData = () => {
  loading.value = true
  const courseApi = isAdmin === true ?
    getCoursePageDataApi({ currentPage: paginationData.currentPage, size: paginationData.pageSize, keyword: keyword.value })
    : getCourseDataByTIdApi({ currentPage: paginationData.currentPage, size: paginationData.pageSize, teacherId: ID, keyword: keyword.value })
  courseApi.then(({ data }) => {
    paginationData.total = data.total
    courseData.value = data.records
  })
    .catch(() => {
      courseData.value = []
    })
    .finally(() => {
      loading.value = false
    })
}

//获取选择该课程的班级信息
const classData = ref<SCourseData[]>([])
const getClassData = (id: number) => {
  classloading.value = true
  const courseApi = isAdmin === true ? getSCourseListDataByCoIdApi({ courseId: id })
    : getSCourseListDataByCTIdApi({ courseId: id, teacherId: ID })
  courseApi.then(({ data }) => {
    classData.value = data.records
  })
    .catch(() => {
      classData.value = []
    })
    .finally(() => {
      classloading.value = false
    })
}

//筛选学院
const semester = ref([
  { text: "", value: "" }
])
const semesterFilter = computed(
  () => {
    const semesterSet = new Set(classData.value.map(item => item.date))
    return semester.value = Array.from(semesterSet).map(date => ({
      text: date,
      value: date,
    }));
  }
)
const filterSemester = (value: string, row: SCourseData) => {
  return row.date === value;
}

//按班级查询学生成绩信息
//学生列表标题
const scoreDetailDialog = ref<boolean>(false)
const stuListTitle = ref<string>("")
const selectStudent = (row: SCourseData) => {
  dialogVisible.value = true
  stuListTitle.value = row.className + "《" + row.courseName + "》平时成绩"
  getStudentData(row.classId, row.courseId)
}
const stuLoading = ref<boolean>(false)
const StudentListData = ref<ScoreData[]>([])
const getStudentData = (classId: number, courseId: number) => {
  stuLoading.value = true
  getScoreDataByCCIdApi({
    classId: classId,
    courseId: courseId
  })
    .then(({ data }) => {
      StudentListData.value = data.records
      data.records.map((item) => {
        xAxisData.value.push(item.studentName)
        seriesData.value.push(item.totalScore)
      })
      stuLoading.value = false
    })
    .catch(() => {
      StudentListData.value = []
    })
    .finally(() => {
      stuLoading.value = false
    })
}

//筛选平时成绩数据
const scoreKeyword = ref<string>("")
const resetSSearch = () => {
  scoreKeyword.value = ""
}
const filterScoreData = computed(() =>
  StudentListData.value.filter(
    (data) =>
      !scoreKeyword.value ||
      data.studentId.toString().toLowerCase().includes(scoreKeyword.value.toLowerCase()) ||
      data.studentName?.toLowerCase().includes(scoreKeyword.value.toLowerCase()) ||
      data.email?.toLowerCase().includes(scoreKeyword.value.toLowerCase()) ||
      data.telephone?.toLowerCase().includes(scoreKeyword.value.toLowerCase()) ||
      data.totalScore.toString().toLowerCase().includes(scoreKeyword.value.toLowerCase())
  )
)

//查询学生成绩明细
const SD_FORM_DATA: ScoreDetailData = {
  type: undefined,
  sdId: 0,
  studentId: 0,
  studentName: "",
  classId: 0,
  className: "",
  courseId: 0,
  courseName: "",
  qname: "",
  date: undefined,
  usualItem: "",
  skillItem: "",
  disItem: "",
  usualScore: 0,
  skillScore: 0,
  disScore: 0,
  totalScore: 0,
  sdRemark: "",
  createTime: undefined,
  updateTime: undefined
}

const sdDialog = ref<boolean>(false)
const sdformRef = ref<FormInstance | null>(null)
const sdformData = ref<ScoreDetailData>(cloneDeep(SD_FORM_DATA))
const sdformRules: FormRules<ScoreDetailData> = {
  date: [{ required: true, trigger: "blur", message: "请选择上课日期" }],
  usualItem: [{ required: true, trigger: "blur", message: "请输入课堂表现成绩明细(如：签到：5，答题：10)" }],
  skillItem: [{ required: true, trigger: "blur", message: "请输入技能成绩明细(如：签到：5，答题：10)" }],
  disItem: [{ required: true, trigger: "blur", message: "请输入纪律技能明细(如：签到：5，答题：10)" }],
  sdRemark: [{ required: true, trigger: "blur", message: "请输入上课节数(如：第一节至第五节课)" }]
}
const resetSDForm = () => {
  sdformRef.value?.clearValidate()
  sdformData.value = cloneDeep(SD_FORM_DATA)
}
const studentId1 = ref<number>()
const studentName1 = ref<string>("")
const classId1 = ref<number>()
const courseId1 = ref<number>()
const courseName1 = ref<string>("")
const qName1 = ref<string>("")
const sdtitle = ref<string>("")  //成绩明细弹窗标题
const sdLoading = ref<boolean>(false)  //成绩明细加载动画
const selectStuSD = (row: ScoreData) => {
  scoreDetailDialog.value = true
  sdtitle.value = row.studentName + "《" + row.courseName + "》平时成绩明细"
  studentId1.value = row.studentId
  studentName1.value = row.studentName
  classId1.value = row.classId
  courseId1.value = row.courseId
  courseName1.value = row.courseName
  qName1.value = row.qname
  getSSDetailData(row.studentId, row.courseId)
}
const stuScoreDetailData = ref<ScoreDetailData[]>([])
const getSSDetailData = (studentId: number, courseId: number) => {
  sdLoading.value = true
  getScoreDataBySCIdApi({
    studentId: studentId,
    courseId: courseId
  })
    .then(({ data }) => {
      stuScoreDetailData.value = data.records
      sdLoading.value = false
      data.records.map((item) => {
        xAxisData.value.push(item.date?.toString())
        seriesData.value.push(item.totalScore)
      })
    })
    .catch(() => {
      stuScoreDetailData.value = []
    })
    .finally(() => {
      sdLoading.value = false
    })
}

//筛选明细数据
const sdKeyword = ref<string>("")
const resetSDSearch = () => {
  sdKeyword.value = ""
}
const filterSDData = computed(() =>
  stuScoreDetailData.value.filter(
    (data) =>
      !sdKeyword.value ||
      data.date?.toString().toLowerCase().includes(sdKeyword.value.toLowerCase()) ||
      data.sdRemark.toString().toLowerCase().includes(sdKeyword.value.toLowerCase())
  )
)

//#region 删除成绩明细
const deleteSD = (row: ScoreDetailData) => {
  ElMessageBox.confirm(`正在删除，确认删除？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    delSDDataApi(row.sdId).then(() => {
      ElMessage.success("删除成功")
      getSSDetailData(row.studentId, row.courseId)
      getStudentData(row.classId, row.courseId)
    })
  })
}
//批量删除成绩明细
//获取选中的数据
const selectArr = ref<number[]>([])
const handleSelectionChange = (val: ScoreDetailData[]) => {
  let arr: number[] = []
  val.forEach((item) => {
    arr.push(item.sdId)
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
        getSSDetailData(studentId1.value, courseId1.value)
        getStudentData(classId1.value, courseId1.value)
      })
    })
  } else {
    ElMessage.warning("请选择要删除的行")
  }
}
//#endregion
//新增成绩明细
const addScoreDetail = () => {
  sdDialog.value = true
  sdformData.value.studentId = studentId1.value
  sdformData.value.studentName = studentName1.value
  sdformData.value.classId = classId1.value
  sdformData.value.courseId = courseId1.value
  sdformData.value.courseName = courseName1.value
  sdformData.value.qname = qName1.value
}
//修改成绩明细
const updateSD = (row: ScoreDetailData) => {
  sdDialog.value = true
  sdformData.value = cloneDeep(row)
  sdformData.value.classId = classId1.value
  sdformData.value.type = "修改成绩明细"
}
const SDCreateOrUpdate = () => {
  sdformRef.value?.validate((valid: boolean, fields) => {
    if (!valid) return console.error("表单校验不通过", fields)
    loading.value = true
    const api = sdformData.value.type === undefined ? addSDDataApi : updateSDDataApi
    api(sdformData.value)
      .then((res) => {
        ElMessage.success(res.message)
        sdDialog.value = false
        getSSDetailData(sdformData.value.studentId, sdformData.value.courseId)
        getStudentData(sdformData.value.classId, sdformData.value.courseId)
      })
      .finally(() => {
        loading.value = false
      })
  })
}

//筛选课程类型
const type = ref([
  { text: "", value: "" }
])
const typeFilter = computed(
  () => {
    const typeSet = new Set(courseData.value.map(item => item.type))
    return type.value = Array.from(typeSet).map(type => ({
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
    const schoolSet = new Set(courseData.value.map(item => item.schoolName))
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
    const qnameSet = new Set(courseData.value.map(item => item.qname))
    return qname.value = Array.from(qnameSet).map(qname => ({
      text: qname,
      value: qname,
    }));
  }
)
const filterQname = (value: string, row: CourseData) => {
  return row.qname === value;
}

//筛选日期
const sddate = ref([
  { text: "", value: "" }
])
const sdDateFilter = computed(
  () => {
    const sddateSet = new Set(stuScoreDetailData.value.map(item => item.date))
    return sddate.value = Array.from(sddateSet).map(date => ({
      text: date?.toString(),
      value: date?.toString(),
    }));
  }
)
const filterDate = (value: string, row: ScoreDetailData) => {
  return row.date?.toString() === value;
}

const resetSearch = () => {
  keyword.value = ""
  getCourseData()
}

/** 导入文件 */
const uploadVisible = ref<boolean>(false)
const uploadTitle = ref<string>("平时成绩上传")
const uploadUrl = ref<string>("/importScore")
const uploadSD = () => {
  uploadTitle.value = "平时成绩明细上传"
  uploadUrl.value = "importSD"
  uploadVisible.value = true
}
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
  getCourseData()
  ElMessageBox({
    title: "导入结果",
    message: h("p", null, [
      h("p", null, "导入总数：" + res.data.totalRows + "条"),
      h("p", null, "导入成功：" + res.data.successRows + "条"),
      h("p", null, "导入失败：" + res.data.errorMsgList.length + "条"),
      h("div", null, [
        h("p", null, "失败原因："),
        h("p", null, res.data.errorMsgList.length > 0 ? res.data.errorMsgList.map(
          (item: string) => h('p', { class: 'item' }, item)) : "无"
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
//设置请求头
const token = userStore.token
const config = {
  'Authorization': `Bearer ${token}`,  // 假设你有一个 token
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
//导出各班级所有选课的所有学生成绩
const exportFile = () => {
  exportFileApi(
    "exportScore",{id: ID}
  ).then((res) => {
    download(res)
  }).catch(error => {
    ElMessage.error('导出失败');
  })
}
//导出该课程所有班级的成绩
const exportFileByCourseId = (row: SCourseData) => {
  exportFileApi(
    "exportScoreByCourseId",{courseId: row.courseId, id: ID}
  ).then((res) => {
    download(res)
  }).catch(error => {
    ElMessage.error('导出失败');
  })
}
//导出该班级该课程的所有学生成绩
const exportFileByCCId = (row: SCourseData) => {
  exportFileApi(
    "exportScoreByCCId",{classId: row.classId, courseId: row.courseId, id: ID}
  ).then((res) => {
    download(res)
  }).catch(error => {
    ElMessage.error('导出失败');
  })
}
//导出该课程该学生的平时成绩明细
const exportSD = () => {
  exportFileApi(
    "exportSDBySCId",{studentId: studentId1.value, courseId: courseId1.value}
  ).then((res) => {
    download(res)
  }).catch(error => {
    ElMessage.error('导出失败');
  })
}

/** 下载模板 */
const downloadTemplate1 = () => {
  window.open("http://localhost:3333/api/download?fileName=课程成绩表")
}
const downloadTemplate2 = () => {
  window.open("http://localhost:3333/api/download?fileName=课程平时成绩明细表")
}

/** 监听分页参数的变化 */
watch([() => paginationData.currentPage, () => paginationData.pageSize], getCourseData, { immediate: true })

/** 设置表格展开时只展开一行 */
// 用于存储展开行的唯一标识符
const getRowKeys = (row: SCourseData) => {
  return row.courseId.toString()
}
// 当前展开的行的 key
const expands = ref<string[]>([]);
// 处理行展开和折叠事件
const expandChangeHandler = (row: SCourseData, expandedRows: any) => {
  if (expandedRows.length) {
    expands.value = []
    if (row) {
      // 展开某一行时，仅保留该行的key
      expands.value.push(row.courseId.toString())
    }
  } else {
    // 折叠该行时，清空展开行
    expands.value = []
  }
}
// 处理点击行事件
const handleRowClick = (row: SCourseData) => {
  //展开时获取选课对应的班级信息
  getClassData(row.courseId)
  const key = row.courseId.toString()  // 获取行的 key
  if (expands.value.includes(key)) {
    // 如果该行已展开，折叠该行
    expands.value = [];
  } else {
    // 否则展开该行并折叠其他行
    expands.value = [key];
  }
}

//成绩分析
const analysisTitle = ref<string>("")
const scoreADialog = ref<boolean>(false)
// 柱状图数据
const xAxisData = ref<string[]>([])
const seriesData = ref<number[]>([])
const charttype = ref<string>("bar")
//分析该班级平时成绩
const scoreAnalysis = (row: SCourseData) => {
  xAxisData.value = []
  seriesData.value = []
  analysisTitle.value = row.className + "《" + row.courseName + "》平时成绩分析"
  charttype.value = "bar"
  getStudentData(row.classId, row.courseId)
  scoreADialog.value = true
}
//分析该学生各课时平时成绩  折线图
const sdAnalysis = (row: ScoreData) => {
  xAxisData.value = []
  seriesData.value = []
  analysisTitle.value = row.studentName + "《" + row.courseName + "》各课次平时成绩分析"
  charttype.value = "line"
  getSSDetailData(row.studentId, row.courseId)
  scoreADialog.value = true
}
const resetTitle = () => {
  stuListTitle.value = ""
  xAxisData.value = []
  seriesData.value = []
}
const option = ref({
  tooltip: {
    trigger: 'item',
  },
  xAxis: {
    data: xAxisData,
    axisLabel: {
      rotate: 45,  // 将标签旋转 45 度
      interval: 0  // 确保每个标签都显示
    }
  },
  yAxis: {},
  series: [
    {
      type: charttype,
      data: seriesData
    }
  ],
  label: {
    show: true,
    position: 'top',
    textStyle: {
      fontSize: '14px',
    },
  },
  toolbox: {
    feature: {
      magicType: {
        // 在柱状图和折线图之间切换
        type: ['bar', 'line']
      }
    }
  },
})
const width = ref("800px")
const height = ref("600px")
</script>

<template>
  <div class="app-container">
    <el-card v-loading="loading" shadow="never" class="search-wrapper">
      <el-form :inline="true">
        <el-form-item prop="username" label="关键词">
          <el-input v-model="keyword" placeholder="请输入关键词" />
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" @click="getCourseData">查询</el-button>
          <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card v-loading="loading" shadow="never">
      <div class="toolbar-wrapper">
        <div>
          <el-tooltip content="下载课程成绩表模板">
            <el-button type="primary" @click="downloadTemplate1">成绩模板</el-button>
          </el-tooltip>
          <el-tooltip content="下载课程平时成绩明细表模板">
            <el-button type="primary" @click="downloadTemplate2">明细模板</el-button>
          </el-tooltip>
          <el-tooltip content="导入平时成绩">
            <el-button type="primary" :icon="Upload" circle @click="uploadVisible = true" />
          </el-tooltip>
          <el-tooltip content="导入平时成绩明细">
            <el-button type="primary" :icon="Top" circle @click="uploadSD" />
          </el-tooltip>
          <el-tooltip content="导出">
            <el-button type="primary" :icon="Download" circle @click="exportFile" />
          </el-tooltip>
          <el-tooltip content="刷新当前页">
            <el-button type="primary" :icon="RefreshRight" circle @click="getCourseData" />
          </el-tooltip>
        </div>
      </div>
      <div class="table-wrapper">
        <el-table v-loading="loading" :data="courseData" :expand-row-keys="expands" :row-key="getRowKeys"
          @expand-change="expandChangeHandler" @row-click="handleRowClick">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column prop="courseId" label="课程编号" sortable align="center" />
          <el-table-column prop="courseName" label="课程名称" align="center" />
          <el-table-column prop="type" label="课程类型" align="center" :filters="typeFilter" :filter-method="filterType" />
          <el-table-column prop="schoolName" label="所属学院" align="center" :filters="schoolFilter" :filter-method="filterSchool" />
          <el-table-column prop="classPeriod" label="总课时" align="center" />
          <el-table-column prop="practicalClassPeriod" label="实践课时" align="center" />
          <el-table-column prop="qname" label="成绩量化标准" align="center" :filters="qnameFilter" :filter-method="filterQname" />
          <el-table-column type="expand">
            <template #default="scope">
              <div class="box">
                <el-table :data="classData" :border="true" v-loading="classloading">
                  <el-table-column label="选课班级表" align="center">
                    <el-table-column type="selection" width="80" align="center" />
                    <el-table-column label="班级编号" prop="classId" align="center" />
                    <el-table-column label="班级名称" prop="className" align="center" />
                    <el-table-column label="任课教师工号" prop="teacherId" align="center" />
                    <el-table-column label="任课教师" prop="teacherName" align="center" />
                    <el-table-column label="学期" prop="date" align="center" :filters="semesterFilter" :filter-method="filterSemester" />
                    <el-table-column fixed="right" label="操作" width="280" align="center">
                      <template #default="props">
                        <el-button type="primary" text bg size="small"
                          @click="scoreAnalysis(props.row)">成绩分析</el-button>
                        <el-button type="primary" text bg size="small"
                          @click="selectStudent(props.row)">查看成绩</el-button>
                        <el-button type="primary" text bg size="small"
                          @click="exportFileByCCId(props.row)">导出成绩</el-button>
                      </template>
                    </el-table-column>
                  </el-table-column>
                </el-table>
                <div class="box-btn">
                  <el-tooltip content="导出该课程所有班级的成绩">
                    <el-button type="primary" bg @click="exportFileByCourseId(scope.row)">导出成绩</el-button>
                  </el-tooltip>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pager-wrapper">
        <el-pagination background :layout="paginationData.layout" :page-sizes="paginationData.pageSizes"
          :total="paginationData.total" :page-size="paginationData.pageSize" :currentPage="paginationData.currentPage"
          @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>
    <!-- 文件上传 -->
    <el-dialog v-model="uploadVisible" :title="uploadTitle" width="30%" @closed="resetFile">
      <el-upload ref="uploadRef" :action="'http://localhost:3333/api/' + uploadUrl + '?id=' + ID" class="upload-demo" drag
        accept=".xlsx, .xls" :file-list="fileList" :auto-upload="false" :on-exceed="exceedFile"
        :before-upload="beforeUpload" :on-success="successUpload" :on-error="errorUpload" :show-file-list="true"
        :limit="1" :headers="config">
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
    <!-- 学生列表 -->
    <el-dialog v-model="dialogVisible" align-center :title="stuListTitle" @closed="resetTitle" width="80%" center>
      <el-card v-loading="loading" shadow="never" class="search-wrapper">
        <el-form :inline="true">
          <el-form-item prop="" label="请输入关键词">
            <el-input v-model="scoreKeyword" placeholder="请输入关键词" />
          </el-form-item>
          <el-form-item>
            <el-button :icon="Refresh" @click="resetSSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      <el-table v-loading="stuLoading" :data="filterScoreData" max-height="500">
        <!-- <el-table-column type="selection" fixed width="50" align="center" /> -->
        <el-table-column label="学生信息" align="center">
          <el-table-column prop="studentId" label="学号" sortable align="center" />
          <el-table-column prop="studentName" label="姓名" align="center" />
          <el-table-column prop="telephone" label="手机号" align="center" />
          <el-table-column prop="email" label="邮箱" align="center" />
        </el-table-column>
        <el-table-column label="平时成绩" align="center">
          <el-table-column prop="usualScore" label="课堂表现成绩" align="center" />
          <el-table-column prop="skillScore" label="技能成绩" align="center" />
          <el-table-column prop="disScore" label="纪律成绩" align="center" />
          <el-table-column prop="totalScore" label="平时成绩总分" align="center" />
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="220" align="center">
          <template #default="scope1">
            <el-button type="primary" text bg size="small" @click="sdAnalysis(scope1.row)">分析</el-button>
            <el-button type="primary" text bg size="small" @click="handleUpdate(scope1.row)">修改</el-button>
            <el-button type="primary" text bg size="small" @click="selectStuSD(scope1.row)">成绩明细</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
    <!-- 新增或修改成绩 -->
    <el-dialog v-model="scoreDialog" align-center title="修改平时成绩" @closed="resetForm" width="30%">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="106px" label-position="left">
        <el-form-item prop="studentId" label="学号">
          <el-input v-model="formData.studentId" disabled />
        </el-form-item>
        <el-form-item prop="studentName" label="学生姓名">
          <el-input v-model="formData.studentName" disabled />
        </el-form-item>
        <el-form-item prop="courseName" label="课程名称">
          <el-input v-model="formData.courseName" disabled />
        </el-form-item>
        <el-form-item prop="usualScore" label="课堂表现成绩">
          <!-- <el-input v-model="formData.usualScore" placeholder="请输入" /> -->
          <el-input-number v-model="formData.usualScore" :min="0" :max="100" />
        </el-form-item>
        <el-form-item prop="skillScore" label="技能成绩">
          <!-- <el-input v-model="formData.skillScore" placeholder="请输入" /> -->
          <el-input-number v-model="formData.skillScore" :min="0" :max="100" />
        </el-form-item>
        <el-form-item prop="disScore" label="纪律成绩">
          <!-- <el-input v-model="formData.disScore" placeholder="请输入" /> -->
          <el-input-number v-model="formData.disScore" :min="0" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scoreDialog = false">取消</el-button>
        <el-tooltip content="直接修改会导致平时成绩与平时成绩明细的数据对不上！">
          <el-button type="danger" @click="handleCreateOrUpdate" :loading="loading">确认</el-button>
        </el-tooltip>
      </template>
    </el-dialog>
    <!-- 成绩明细 -->
    <el-dialog v-model="scoreDetailDialog" align-center :title="sdtitle" @closed="" width="90%" center>
      <el-card v-loading="loading" shadow="never" class="search-wrapper">
        <el-form :inline="true">
          <el-form-item prop="" label="请输入关键词">
            <el-input v-model="sdKeyword" placeholder="请输入关键词" />
          </el-form-item>
          <el-form-item>
            <el-button :icon="Refresh" @click="resetSDSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      <el-table :data="filterSDData" max-height="500" v-loading="stuLoading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" fixed width="50" align="center" />
        <!-- <el-table-column prop="sdId" label="编号" sortable align="center" /> -->
        <el-table-column prop="date" label="上课日期" sortable align="center" :filters="sdDateFilter"
          :filter-method="filterDate" />
        <!-- <el-table-column prop="item" label="课时名称" sortable align="center" /> -->
        <el-table-column prop="usualScore" label="课堂表现成绩" align="center" />
        <el-table-column prop="skillScore" label="技能成绩" align="center" />
        <el-table-column prop="disScore" label="纪律成绩" align="center" />
        <!-- <el-table-column prop="totalScore" label="当前课时平时成绩总分" align="center" /> -->
        <el-table-column prop="sdRemark" label="备注" align="center" />
        <el-table-column prop="createTime" label="创建时间" align="center" />
        <el-table-column prop="updateTime" label="修改时间" align="center" />
        <el-table-column fixed="right" label="操作" width="150" align="center">
          <template #default="props">
            <el-button type="primary" text bg size="small" @click="updateSD(props.row)">明细</el-button>
            <el-button type="danger" text bg size="small" @click="deleteSD(props.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="box-btn">
        <el-button type="primary" @click="exportSD">导出</el-button>
        <el-button type="primary" @click="addScoreDetail">新增</el-button>
        <el-button type="danger" @click="delBatches">批量删除</el-button>
      </div>
    </el-dialog>
    <!-- 新增修改成绩明细 -->
    <el-dialog v-model="sdDialog" align-center :title="sdformData.type === undefined ? '新增平时成绩明细' : '修改平时成绩明细'"
      @closed="resetSDForm" width="30%">
      <el-form ref="sdformRef" :model="sdformData" :rules="sdformRules" label-width="136px" label-position="left">
        <el-form-item prop="studentId" label="学号">
          <el-input v-model="sdformData.studentId" disabled />
        </el-form-item>
        <el-form-item prop="studentName" label="学生姓名">
          <el-input v-model="sdformData.studentName" disabled />
        </el-form-item>
        <el-form-item prop="courseName" label="课程名称">
          <el-input v-model="sdformData.courseName" disabled />
        </el-form-item>
        <el-form-item prop="date" label="上课日期">
          <el-date-picker v-model="sdformData.date" type="date" format="YYYY/MM/DD" value-format="YYYY-MM-DD"
            placeholder="请选择上课日期" />
        </el-form-item>
        <!-- <el-form-item prop="item" label="课时名称">
          <el-input v-model="sdformData.item" />
        </el-form-item> -->
        <el-form-item prop="usualItem" label="课堂表现成绩明细">
          <el-input v-model="sdformData.usualItem" placeholder="请输入课堂表现成绩明细（如：签到：5，答题：10）" autosize type="textarea" />
        </el-form-item>
        <!-- <el-form-item prop="usualScore" label="课堂表现成绩">
          <el-input-number v-model="sdformData.usualScore" :min="0" />
        </el-form-item> -->
        <el-form-item prop="skillItem" label="技能成绩明细">
          <el-input v-model="sdformData.skillItem" placeholder="请输入技能成绩明细（如：签到：5，答题：10）" autosize type="textarea" />
        </el-form-item>
        <!-- <el-form-item prop="skillScore" label="课堂表现成绩">
          <el-input-number v-model="sdformData.skillScore" :min="0" />
        </el-form-item> -->
        <el-form-item prop="disItem" label="纪律成绩明细">
          <el-input v-model="sdformData.disItem" placeholder="请输入纪律成绩明细（如：签到：5，答题：10）" autosize type="textarea" />
        </el-form-item>
        <!-- <el-form-item prop="disScore" label="课堂表现成绩">
          <el-input-number v-model="sdformData.disScore" :min="0" />
        </el-form-item> -->
        <el-form-item prop="sdRemark" label="备注">
          <el-input v-model="sdformData.sdRemark" placeholder="请输入课堂表现成绩明细（如：第一节至第五节课）" autosize type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sdDialog = false">取消</el-button>
        <el-button type="primary" @click="SDCreateOrUpdate" :loading="loading">确认</el-button>
      </template>
    </el-dialog>
    <!-- 成绩分析 -->
    <el-dialog align-center v-model="scoreADialog" :title="analysisTitle" @closed="resetTitle" width="60%" center>
      <Bar :option="option" :width="width" :height="height"></Bar>
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

.box-btn {
  margin-top: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
