<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { addQuantitativeDataApi, delQuantitativeDataApi, delBatchesDataApi, updateQuantitativeDataApi, getQPageDataApi } from "@/api/quantitative"
import { type QuantitativeData } from "@/api/quantitative/types/quantitative"
import { type FormInstance, type FormRules, ElMessage, ElMessageBox } from "element-plus"
import { Refresh, CirclePlus, Delete, RefreshRight, Search } from "@element-plus/icons-vue"
import { usePagination } from "@/hooks/usePagination"
import { cloneDeep } from "lodash-es"

defineOptions({
  // 命名当前组件
  name: "Quantitative"
})

const loading = ref<boolean>(false)
const { paginationData, handleCurrentChange, handleSizeChange } = usePagination()

//#region 增
const DEFAULT_FORM_DATA: QuantitativeData = {
    type: undefined,
    qid: 0,
    qname: "",
    usualScore: 0,
    skillScore: 0,
    disScore: 0,
    remark: "无"
}
const dialogVisible = ref<boolean>(false)
const formRef = ref<FormInstance | null>(null)
const formData = ref<QuantitativeData>(cloneDeep(DEFAULT_FORM_DATA))
const formRules: FormRules<QuantitativeData> = {
    qname: [{ required: true, trigger: "blur", message: "请输入成绩量化标准名称" }],
    usualScore: [{ required: true, trigger: "blur", message: "请输入课堂表现成绩占比" }],
    skillScore: [{ required: true, trigger: "blur", message: "请输入技能成绩占比" }],
    disScore: [{ required: true, trigger: "blur", message: "请输入纪律成绩占比" }],
    remark: [{ required: true, trigger: "blur", message: "请输入学院名称" }]
}
const handleCreateOrUpdate = () => {
  formRef.value?.validate((valid: boolean, fields) => {
    if (!valid) return console.error("表单校验不通过", fields)
    loading.value = true
    const api = formData.value.type === undefined ? addQuantitativeDataApi : updateQuantitativeDataApi
    if(oldName.value === formData.value.qname) {
      formData.value.qname = ""
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
const handleDelete = (row: QuantitativeData) => {
  ElMessageBox.confirm(`正在删除，确认删除？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    delQuantitativeDataApi(row.qid).then(() => {
      ElMessage.success("删除成功")
      getTableData()
    })
  })
}
//批量删除
//获取选中的数据
const selectArr = ref<number[]>([])
const handleSelectionChange = (val: QuantitativeData[]) => {
  let arr: number[] = [] 
  val.forEach((item) => {
    arr.push(item.qid)
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
const oldName = ref<string>("")
const handleUpdate = (row: QuantitativeData) => {
  dialogVisible.value = true
  formData.value = cloneDeep(row)
  oldName.value = formData.value.qname
  formData.value.type = "修改成绩量化标准"
}
//#region 查
const tableData = ref<QuantitativeData[]>([])
const keyword = ref("")
const getTableData = () => {
  loading.value = true
  getQPageDataApi({
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

const resetSearch = () => {
  keyword.value = ""
  getTableData()
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
          <el-button type="primary" :icon="CirclePlus" @click="dialogVisible = true">新增</el-button>
          <el-button type="danger" :icon="Delete" @click="delBatches">批量删除</el-button>
        </div>
        <div>
          <el-tooltip content="刷新当前页">
            <el-button type="primary" :icon="RefreshRight" circle @click="getTableData" />
          </el-tooltip>
        </div>
      </div>
      <div class="table-wrapper">
        <el-table :data="tableData" @selection-change="handleSelectionChange" max-height="350">
          <el-table-column type="selection" fixed width="50" align="center" />
          <el-table-column prop="qid" label="编号" sortable align="center" />
          <el-table-column prop="qname" label="成绩量化标准名称" align="center" />
          <el-table-column prop="usualScore" label="课堂表现成绩占比(%)" align="center" />
          <el-table-column prop="skillScore" label="技能成绩占比(%)" align="center" />
          <el-table-column prop="disScore" label="纪律成绩占比(%)" align="center" />
          <el-table-column prop="remark" label="适用范围" align="center" />
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
    <!-- 新增/修改 -->
    <el-dialog
      v-model="dialogVisible"
      :title="formData.type === undefined ? '新增成绩量化标准' : '修改成绩量化标准'"
      @closed="resetForm"
      width="36%"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="138px" label-position="left">
        <el-form-item prop="qid" label="编号" v-if="formData.type === '修改成绩量化标准'">
          <el-input v-model="formData.qid" disabled/>
        </el-form-item>
        <el-form-item prop="qname" label="成绩量化标准名称">
          <el-input v-model="formData.qname" placeholder="请输入" />
        </el-form-item>
        <el-form-item prop="usualScore" label="课堂表现成绩占比">
          <el-input v-model="formData.usualScore" placeholder="请输入" />
        </el-form-item>
        <el-form-item prop="skillScore" label="技能成绩占比">
          <el-input v-model="formData.skillScore" placeholder="请输入" />
        </el-form-item>
        <el-form-item prop="disScore" label="纪律成绩占比">
          <el-input v-model="formData.disScore" placeholder="请输入" />
        </el-form-item>
        <el-form-item prop="remark" label="适用范围">
          <el-input type="textarea" autosize v-model="formData.remark" placeholder="请输入适用范围" />
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
