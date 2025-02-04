<script lang="ts" setup>
import { ref, watch } from 'vue'
import { useRouter } from "vue-router"
import { upAdminInfoApi, upAdminPwdApi, getAdminInfoApi } from "@/api/login"
import { type AdminData } from "@/api/login/types/login"
import { getTableGroupDataApi } from "@/api/teacher"
import { type TeacherGroupData } from "@/api/teacher/types/teacher"
import { useUserStore } from '@/store/modules/user'
import { type FormInstance, type FormRules, ElMessage, ElMessageBox } from "element-plus"
import { cloneDeep } from "lodash-es"

const info: AdminData = {
  type: undefined,
  id: 0,
  username: "",
  teacherId: 0,
  teacherName: "",
  gender: "",
  title: "",
  telephone: "",
  email: "",
  schoolId: undefined,
  schoolName: "",
  password: ""
}
const userStore = useUserStore()
const ID = userStore.id
const dialogVisible = ref<boolean>(false)
const formRef = ref<FormInstance | null>(null)
const formData = ref<AdminData>(cloneDeep(info))
const flag = ref<boolean>(false)

//修改个人信息
const handleUpdate = () => {
  getTeacherData()
  flag.value = false
  dialogVisible.value = true
  formData.value = cloneDeep(tableData.value[0])
}
//修改密码
const handleUpPwd = () => {
  getTeacherData()
  dialogVisible.value = true
  formData.value = cloneDeep(tableData.value[0])
  formData.value.type = "修改密码"
  flag.value = true
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
const formRules: FormRules<AdminData> = {
  username: [{ required: true, trigger: "blur", message: "请输入用户名" }],
  teacherName: [{ required: true, trigger: "blur", message: "请选择账号持有人" }],
  password: [{ required: true, validator: validatePass, trigger: "blur" }]
}
const handleCreateOrUpdate = () => {
  formRef.value?.validate((valid: boolean, fields) => {
    if (!valid) return console.error("表单校验不通过", fields)
    const api = formData.value.type === undefined ? upAdminInfoApi : upAdminPwdApi
    api(formData.value)
      .then((res) => {
        ElMessage.success(res.message)
        dialogVisible.value = false
        getTableData()
        if(flag.value) {
          ElMessageBox.confirm('密码已经修改，请重新登录', '警告', {
            confirmButtonText: '确定',
            closeOnClickModal: false,  // 禁止点击外部关闭
            showClose: false,          // 去掉右上角关闭按钮
            showCancelButton: false    // 去掉取消按钮
          }).then(() => {
            logout()
          });
        }
      })
  })
}
const resetForm = () => {
  formRef.value?.clearValidate()
  formData.value = cloneDeep(cloneDeep(tableData.value[0]))
}

//获取个人信息
const tableData = ref<AdminData[]>([])
const getTableData = () => {
  let id = ID
  getAdminInfoApi(id)
    .then(({ data }) => {
        tableData.value = data.records
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

/** 登出 */
const router = useRouter()
const logout = () => {
  userStore.logout()
  router.push("/login")
}

watch([() => userStore], getTableData, { immediate: true })
</script>

<template>
  <div class="app-container">
    <el-card class="info-card" :body-style="{ padding: '20px' }">
      <template #header>
        <div class="card-header">
          <h2>个人信息</h2>
        </div>
      </template>
      <div class="card-body">
        <div v-for="item in tableData">
          <!-- <p class="item">
            <label>管理员编号：</label>
            <span>{{ item.id }}</span>
          </p> -->
          <p class="item">
            <label>管理员账号：</label>
            <span>{{ item.username }}</span>
          </p>
          <p class="item">
            <label>管理员账号持有人：</label>
            <span>{{ item.teacherName }}</span>
          </p>
          <p class="item">
            <label>教师工号：</label>
            <span>{{ item.teacherId }}</span>
          </p>
          <p class="item">
            <label>性别：</label>
            <span>{{ item.gender }}</span>
          </p>
          <p class="item">
            <label>职称：</label>
            <span>{{ item.title }}</span>
          </p>
          <p class="item">
            <label>手机号：</label>
            <span>{{ item.telephone }}</span>
          </p>
          <p class="item">
            <label>邮箱：</label>
            <span>{{ item.email }}</span>
          </p>
          <p class="item">
            <label>所属学院：</label>
            <span>{{ item.schoolName }}</span>
          </p>
        </div>
      </div>
      <div class="card-footer">
        <el-button type="primary" @click="handleUpdate">编辑信息</el-button>
        <el-button type="primary" @click="handleUpPwd">修改密码</el-button>
      </div>
    </el-card>

    <!-- 修改 -->
    <el-dialog
      v-model="dialogVisible"
      :title="formData.type === undefined ? '修改个人信息' : '修改密码'"
      @closed="resetForm"
      width="30%"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="140px" label-position="left">
        <el-form-item prop="id" label="管理员编号">
          <el-input v-model="formData.id" disabled />
        </el-form-item>
        <el-form-item prop="username" label="账号">
          <el-input v-model="formData.username" placeholder="请输入" :disabled="formData.type === '修改密码'"/>
        </el-form-item>
        <el-form-item prop="teacherName" label="管理员账号持有人">
          <el-select v-model="formData.teacherId" placeholder="请选择任课教师" filterable clearable  @change="saveTName" :disabled="formData.type === '修改密码'">
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
        <el-form-item prop="password" label="密码" v-if="formData.type === '修改密码'">
          <el-input type="password" v-model="formData.password" placeholder="请输入" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateOrUpdate">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.info-card {
  width: 800px;
  margin: 20px auto;
}

h2 {
    text-align: center;
}

.card-body {
  margin: 0 30px;
}

.card-body .item {
  margin-bottom: 20px;
}

.card-body .item label {
  display: inline-block;
  width: 150px;
  margin-right: 10px;
  font-weight: bold;
}

.card-footer {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>