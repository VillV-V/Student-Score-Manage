<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { useUserStore } from "@/store/modules/user"
import { getScoreRankApi, getScoreDataByStuIdApi, exportFileApi } from "@/api/score"
import { type ScoreData } from "@/api/score/types/score"
import { getScoreDataBySCIdApi } from "@/api/score-detail"
import { type ScoreDetailData } from "@/api/score-detail/types/score-detail"
import { type FormInstance, type FormRules, ElMessage } from "element-plus"
import { Refresh, Download, RefreshRight } from "@element-plus/icons-vue"
import { usePagination } from "@/hooks/usePagination"
import { cloneDeep } from "lodash-es"
import Bar from "@/components/Echarts/Bar.vue"

defineOptions({
    // 命名当前组件
    name: "SelectScore"
})

const loading = ref<boolean>(false)
const { paginationData, handleCurrentChange, handleSizeChange } = usePagination()
const userStore = useUserStore()
const ID = userStore.id

//#region 查
const StudentListData = ref<ScoreData[]>([])
const getStudentData = (classId: number, courseId: number) => {
    getScoreRankApi({
        classId: classId,
        courseId: courseId
    })
        .then(({ data }) => {
            StudentListData.value = data.records
            data.records.map((item) => {
                xAxisData.value.push(item.studentName)
                seriesData.value.push(item.totalScore)
            })
        })
        .catch(() => {
            StudentListData.value = []
        })
}

const tableData = ref<ScoreData[]>([])
const keyword = ref("")
const getTableData = () => {
    loading.value = true
    getScoreDataByStuIdApi({
        studentId: ID
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
//筛选表格数据
const filterTableData = computed(() =>
    tableData.value.filter(
        (data) =>
            !keyword.value ||
            data.courseName.toLowerCase().includes(keyword.value.toLowerCase())
    )
)
const resetSearch = () => {
    keyword.value = ""
    sdKeyword.value = ""
}

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
    sdRemark: "无",
    createTime: undefined,
    updateTime: undefined
}
const scoreDetailDialog = ref<boolean>(false)
const sdDialog = ref<boolean>(false)
const sdformRef = ref<FormInstance | null>(null)
const sdformData = ref<ScoreDetailData>(cloneDeep(SD_FORM_DATA))

const resetSDForm = () => {
    sdformRef.value?.clearValidate()
    sdformData.value = cloneDeep(SD_FORM_DATA)
}
const studentId1 = ref<number>()
const studentName1 = ref<string>("")
const classId1 = ref<number>()
const courseId1 = ref<number>()
const courseName1 = ref<string>("")
const totalScore = ref<number>()
const sdLoading = ref<boolean>(false)  //成绩明细加载动画
const selectStuSD = (row: ScoreData) => {
    scoreDetailDialog.value = true
    studentId1.value = row.studentId
    studentName1.value = row.studentName
    classId1.value = row.classId
    courseId1.value = row.courseId
    courseName1.value = row.courseName
    totalScore.value = row.totalScore
    getSSDetailData(row.studentId, row.courseId)
}
// 详细
const stuSdDialog = ref<boolean>(false)
const stuSD = (row: ScoreDetailData) => {
    sdformData.value = cloneDeep(row)
    stuSdDialog.value = true
}
const sdKeyword = ref<string>("")
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
const filterSDData = computed(() =>
    stuScoreDetailData.value.filter(
        (data) =>
            !sdKeyword.value ||
            data.date?.toString().toLowerCase().includes(sdKeyword.value.toLowerCase()) ||
            data.sdRemark.toString().toLowerCase().includes(sdKeyword.value.toLowerCase())
    )
)

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

/** 监听分页参数的变化 */
watch([() => paginationData.currentPage, () => paginationData.pageSize], getTableData, { immediate: true })

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
//导出所有成绩
const exportFile = () => {
  exportFileApi(
    "exportAllScoreBySCId",{studentId: ID}
  ).then((res) => {
    download(res)
  }).catch(error => {
    ElMessage.error('导出失败');
  })
}

//导出该课程的成绩明细
const exportFileBySCId = (row: ScoreDetailData) => {
  exportFileApi(
    "exportSDBySCId",{studentId: ID, courseId: row.courseId}
  ).then((res) => {
    download(res)
  }).catch(error => {
    ElMessage.error('导出失败');
  })
}

//成绩分析
const analysisTitle = ref<string>("")
const scoreADialog = ref<boolean>(false)
// 柱状图数据
const xAxisData = ref<string[]>([])
const seriesData = ref<number[]>([])
const charttype = ref<string>("bar")
const name1 = ref<string>("")
const title = ref<string>("")
//分析平时成绩
const scoreAnalysis = (row: ScoreData) => {
    xAxisData.value = []
    seriesData.value = []
    analysisTitle.value = row.className + "《" + row.courseName + "》平时成绩分析"
    title.value = row.className + "平时成绩排名"
    name1.value = row.studentName
    charttype.value = "bar"
    getStudentData(row.classId, row.courseId)
    scoreADialog.value = true
}
//分析该学生各课时平时成绩  折线图
const sdAnalysis = () => {
    xAxisData.value = []
    seriesData.value = []
    analysisTitle.value = studentName1.value + "《" + courseName1.value + "》各课次平时成绩分析"
    title.value = ""
    charttype.value = "line"
    getSSDetailData(studentId1.value, courseId1.value)
    scoreADialog.value = true
}
const resetTitle = () => {
    xAxisData.value = []
    seriesData.value = []
}

const option = ref({
    title: {
        text: title, // 主标题内容
        left: 'center', // 主标题的位置，'center'，'left'，'right'
        top: '20', // 主标题距离顶部的距离
        textStyle: {
            fontSize: 20, // 主标题字体大小
            fontWeight: 'bold', // 主标题字体粗细
            color: '#333' // 主标题颜色
        },
    },
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
            data: seriesData,
            itemStyle: {
            // 通过设置 color，来直接控制所有柱子的颜色
            normal: {
                color: function(params: any) {
                    // 判断当前柱子的值是否为特定的值
                    if (params.name === name1.value) {
                        // 如果是特定的值（例如 92），设置该柱子的颜色为红色
                        return 'red';
                    } else {
                        // 否则，设置其他柱子的颜色为绿色
                        return 'blue';
                    }
                }
            }
        }
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
                    <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
                </el-form-item>
            </el-form>
        </el-card>
        <el-card v-loading="loading" shadow="never">
            <div class="toolbar-wrapper">
                <div>
                    <el-tooltip content="导出所有成绩">
                        <el-button type="primary" :icon="Download" circle @click="exportFile" />
                    </el-tooltip>
                    <el-tooltip content="刷新当前页">
                        <el-button type="primary" :icon="RefreshRight" circle @click="getTableData" />
                    </el-tooltip>
                </div>
            </div>
            <div class="table-wrapper">
                <el-table :data="filterTableData" max-height="350">
                    <el-table-column label="平时成绩表" fixed align="center">
                        <el-table-column type="selection" width="50" align="center" />
                        <el-table-column prop="courseId" label="课程编号" sortable align="center" />
                        <el-table-column prop="courseName" label="课程名称" align="center" />
                        <el-table-column prop="usualScore" label="课堂表现成绩" align="center" />
                        <el-table-column prop="skillScore" label="技能成绩" align="center" />
                        <el-table-column prop="disScore" label="纪律成绩" align="center" />
                        <el-table-column prop="totalScore" label="平时成绩总分" align="center" />
                        <el-table-column fixed="right" label="操作" width="260" align="center">
                            <template #default="scope">
                                <el-button type="primary" text bg size="small"
                                    @click="scoreAnalysis(scope.row)">成绩分析</el-button>
                                <el-button type="primary" text bg size="small"
                                    @click="selectStuSD(scope.row)">查看明细</el-button>
                                <el-button type="primary" text bg size="small"
                                    @click="exportFileBySCId(scope.row)">导出明细</el-button>
                            </template>
                        </el-table-column>
                    </el-table-column>
                </el-table>
            </div>
            <div class="pager-wrapper">
                <el-pagination background :layout="paginationData.layout" :page-sizes="paginationData.pageSizes"
                    :total="paginationData.total" :page-size="paginationData.pageSize"
                    :currentPage="paginationData.currentPage" @size-change="handleSizeChange"
                    @current-change="handleCurrentChange" />
            </div>
        </el-card>
        <!-- 成绩明细 -->
        <el-dialog align-center v-model="scoreDetailDialog" title="平时成绩明细" @closed="" width="90%" center>
            <el-card v-loading="loading" shadow="never" class="search-wrapper">
                <el-form :inline="true">
                    <el-form-item prop="username" label="请输入关键词">
                        <el-input v-model="sdKeyword" placeholder="请输入关键词" />
                    </el-form-item>
                    <el-form-item>
                        <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
                    </el-form-item>
                </el-form>
            </el-card>
            <el-table :data="filterSDData" max-height="500">
                <el-table-column type="selection" fixed width="50" align="center" />
                <!-- <el-table-column prop="sdId" label="编号" sortable align="center" /> -->
                <el-table-column prop="date" label="上课日期" sortable align="center" :filters="sdDateFilter" :filter-method="filterDate" />
                <!-- <el-table-column prop="item" label="课时名称" sortable align="center" /> -->
                <el-table-column prop="usualScore" label="课堂表现成绩" align="center" />
                <el-table-column prop="skillScore" label="技能成绩" align="center" />
                <el-table-column prop="disScore" label="纪律成绩" align="center" />
                <!-- <el-table-column prop="totalScore" label="当前课时平时成绩总分" align="center" /> -->
                <el-table-column prop="sdRemark" label="备注" align="center" />
                <el-table-column prop="createTime" label="创建时间" align="center" />
                <el-table-column prop="updateTime" label="修改时间" align="center" />
                <el-table-column fixed="right" label="操作" width="220" align="center">
                    <template #default="scope1">
                        <el-button type="primary" text bg size="small" @click="stuSD(scope1.row)">详情</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div class="box-btn">
                <el-tooltip content="查看该课程每次课的平时成绩变化趋势">
                    <el-button type="primary" bg @click="sdAnalysis">平时成绩明细分析</el-button>
                </el-tooltip>
            </div>
        </el-dialog>
        <!-- 新增修改成绩明细 -->
        <el-dialog v-model="stuSdDialog" align-center title="平时成绩明细" @closed="resetSDForm" width="30%">
            <el-form ref="sdformRef" :model="sdformData" label-width="106px" label-position="left">
                <el-form-item prop="studentId" label="学号">
                    <el-input v-model="sdformData.studentId" disabled/>
                </el-form-item>
                <el-form-item prop="studentName" label="学生姓名">
                    <el-input v-model="sdformData.studentName" disabled/>
                </el-form-item>
                <el-form-item prop="courseName" label="课程名称">
                    <el-input v-model="sdformData.courseName" disabled/>
                </el-form-item>
                <el-form-item prop="date" label="上课日期">
                    <el-date-picker v-model="sdformData.date" type="date" format="YYYY/MM/DD" value-format="YYYY-MM-DD"
                        placeholder="请选择上课日期" disabled/>
                </el-form-item>
                <!-- <el-form-item prop="item" label="课时名称">
                    <el-input v-model="sdformData.item" />
                </el-form-item> -->
                <el-form-item prop="usualItem" label="课堂表现成绩明细">
                    <el-input v-model="sdformData.usualItem" placeholder="请输入" autosize type="textarea" disabled/>
                </el-form-item>
                <el-form-item prop="skillItem" label="技能成绩明细">
                    <el-input v-model="sdformData.skillItem" placeholder="请输入" autosize type="textarea" disabled/>
                </el-form-item>
                <el-form-item prop="disItem" label="纪律成绩明细">
                    <el-input v-model="sdformData.disItem" placeholder="请输入" autosize type="textarea" disabled/>
                </el-form-item>
                <el-form-item prop="sdRemark" label="备注">
                    <el-input v-model="sdformData.sdRemark" placeholder="请输入" autosize type="textarea" disabled/>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="stuSdDialog = false">取消</el-button>
                <el-button type="primary" @click="stuSdDialog = false">确认</el-button>
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
