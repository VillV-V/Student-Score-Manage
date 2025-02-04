<script lang="ts" setup>
import { useUserStore } from "@/store/modules/user"
import Admin from "./components/Admin.vue"
import Editor from "./components/Editor.vue"
import Teacher from "./components/Teacher.vue"
import Student from "./components/Student.vue"

const userStore = useUserStore()
const isAdmin = userStore.roles.includes("admin")
const isTeacher = userStore.roles.includes("teacher")
const isStudent = userStore.roles.includes("student")
</script>

<template>
  <component v-if="isTeacher" :is="isTeacher ? Teacher : Editor" />
  <component v-else-if="isStudent" :is="isStudent ? Student : Editor" />
  <component v-else :is="isAdmin ? Admin : Editor" />
</template>
