<script lang="ts" setup>
import { ref, watch } from 'vue'
import { useRouter } from "vue-router"
import { updateTableDataApi, updateStuPwdApi, getStudentInfoApi } from "@/api/student"
import { type StudentData } from "@/api/student/types/student"
import { useUserStore } from '@/store/modules/user'
import { type FormInstance, type FormRules, ElMessage, ElMessageBox } from "element-plus"
import { cloneDeep } from "lodash-es"

const info: StudentData = {
  id: undefined,
  studentId: 0,
  studentName: "",
  gender: "",
  telephone: "",
  email: "",
  className: "",
  majorId: undefined,
  majorName: "",
  schoolId: undefined,
  schoolName: "",
  password: ""
}
const userStore = useUserStore()
const ID = userStore.id
const dialogVisible = ref<boolean>(false)
const formRef = ref<FormInstance | null>(null)
const formData = ref<StudentData>(cloneDeep(info))
const flag = ref<boolean>(false)

//修改个人信息
const handleUpdate = () => {
  dialogVisible.value = true
  formData.value = cloneDeep(tableData.value[0])
  flag.value = false
}
//修改密码
const handleUpPwd = () => {
  dialogVisible.value = true
  formData.value = cloneDeep(tableData.value[0])
  formData.value.id = "修改密码"
  flag.value = true
}
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
const formRules: FormRules<StudentData> = {
  telephone: [{ required: true, validator: validatePhone, trigger: "blur" }],
  email: [{ required: true, validator: validateEmail, trigger: "blur" }],
  password: [{ required: true, validator: validatePass, trigger: "blur" }]
}
const handleCreateOrUpdate = () => {
  formRef.value?.validate((valid: boolean, fields) => {
    if (!valid) return console.error("表单校验不通过", fields)
    const api = formData.value.id === undefined ? updateTableDataApi : updateStuPwdApi
    api(formData.value)
      .then((res) => {
        ElMessage.success(res.message)
        getTableData()
        dialogVisible.value = false
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
const tableData = ref<StudentData[]>([])
const getTableData = () => {
  let studentId = ID
  getStudentInfoApi(studentId)
    .then(({ data }) => {
        tableData.value = data.records
    })
    .catch(() => {
      ElMessage.error("获取个人信息失败")
    })
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
          <p class="item">
            <label>学号：</label>
            <span>{{ item.studentId }}</span>
          </p>
          <p class="item">
            <label>姓名：</label>
            <span>{{ item.studentName }}</span>
          </p>
          <p class="item">
            <label>性别：</label>
            <span>{{ item.gender }}</span>
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
            <label>所属班级：</label>
            <span>{{ item.className }}</span>
          </p>
          <p class="item">
            <label>所属专业：</label>
            <span>{{ item.majorName }}</span>
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
      :title="formData.id === undefined ? '修改个人信息' : '修改密码'"
      @closed="resetForm"
      width="30%"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px" label-position="left">
        <el-form-item prop="studentId" label="教师工号">
          <el-input v-model="formData.studentId" disabled />
        </el-form-item>
        <el-form-item prop="studentName" label="姓名">
          <el-input v-model="formData.studentName" placeholder="请输入" disabled/>
        </el-form-item>
        <el-form-item prop="gender" label="性别">
          <el-select v-model="formData.gender" placeholder="请选择性别" clearable disabled>
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item prop="telephone" label="手机号">
          <el-input v-model="formData.telephone" placeholder="请输入" :disabled="formData.id === '修改密码'" />
        </el-form-item>
        <el-form-item prop="email" label="邮箱">
          <el-input v-model="formData.email" placeholder="请输入" :disabled="formData.id === '修改密码'" />
        </el-form-item>
        <el-form-item prop="className" label="所属班级">
          <el-input v-model="formData.className" placeholder="请输入" disabled/>
        </el-form-item>
        <el-form-item prop="majorName" label="所属专业">
          <el-input v-model="formData.majorName" placeholder="请输入" disabled/>
        </el-form-item>
        <el-form-item prop="schoolName" label="所属学院">
          <el-input v-model="formData.schoolName" placeholder="请输入" disabled />
        </el-form-item>
        <el-form-item prop="password" label="密码" v-if="formData.id === '修改密码'">
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
  width: 80px;
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