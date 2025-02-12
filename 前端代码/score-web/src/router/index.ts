import { type RouteRecordRaw, createRouter } from "vue-router"
import { history, flatMultiLevelRoutes } from "./helper"
import routeSettings from "@/config/route"

const Layouts = () => import("@/layouts/index.vue")

/**
 * 常驻路由
 * 除了 redirect/403/404/login 等隐藏页面，其他页面建议设置 Name 属性
 */
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: "/redirect",
    component: Layouts,
    meta: {
      hidden: true
    },
    children: [
      {
        path: ":path(.*)",
        component: () => import("@/views/redirect/index.vue")
      }
    ]
  },
  {
    path: "/403",
    component: () => import("@/views/error-page/403.vue"),
    meta: {
      hidden: true
    }
  },
  {
    path: "/404",
    component: () => import("@/views/error-page/404.vue"),
    meta: {
      hidden: true
    },
    alias: "/:pathMatch(.*)*"
  },
  {
    path: "/login",
    component: () => import("@/views/login/index.vue"),
    meta: {
      hidden: true
    }
  },
  {
    path: "/",
    component: Layouts,
    redirect: "/dashboard",
    children: [
      {
        path: "dashboard",
        component: () => import("@/views/dashboard/index.vue"),
        name: "Dashboard",
        meta: {
          title: "个人信息",
          elIcon: "House",
          affix: true
        }
      }
    ]
  },
]

/**
 * 动态路由
 * 用来放置有权限 (Roles 属性) 的路由
 * 必须带有 Name 属性
 */
export const dynamicRoutes: RouteRecordRaw[] = [
  {
    path: "/user",
    component: Layouts,
    redirect: "/user/teacher-table",
    name: "UserManage",
    meta: {
      title: "用户管理",
      elIcon: "User",
      roles: ["admin"],
      alwaysShow: true // 将始终显示根菜单
    },
    children: [
      {
        path: "teacher-table",
        component: () => import("@/views/table/teacher/index.vue"),
        name: "Teacher",
        meta: {
          title: "教师管理",
          roles: ["admin"],
          keepAlive: true
        }
      },
      {
        path: "student-table",
        component: () => import("@/views/table/student/index.vue"),
        name: "Student",
        meta: {
          title: "学生管理",
          roles: ["admin"],
          keepAlive: true
        }
      }
    ]
  },
  {
    path: "/school",
    component: Layouts,
    redirect: "/school/school-table",
    name: "SchoolManage",
    meta: {
      title: "学院管理",
      roles: ["admin"], // 可以在根路由中设置角色
    },
    children: [
      {
        path: "school-table",
        component: () => import("@/views/table/school/index.vue"),
        name: "School",
        meta: {
          title: "学院管理",
          elIcon: "School",
          roles: ["admin"] // 或者在子导航中设置角色
        }
      }
    ]
  },
  {
    path: "/major",
    component: Layouts,
    redirect: "/major/major-table",
    name: "MajorManage",
    meta: {
      title: "专业管理",
      roles: ["admin"], // 可以在根路由中设置角色
    },
    children: [
      {
        path: "major-table",
        component: () => import("@/views/table/major/index.vue"),
        name: "Major",
        meta: {
          title: "专业管理",
          elIcon: "ScaleToOriginal",
          roles: ["admin"] // 或者在子导航中设置角色
        }
      }
    ]
  },
  {
    path: "/class",
    component: Layouts,
    redirect: "/class/class-table",
    name: "ClassManage",
    meta: {
      title: "班级管理",
      roles: ["admin"], // 可以在根路由中设置角色
    },
    children: [
      {
        path: "class-table",
        component: () => import("@/views/table/stu-class/index.vue"),
        name: "StuClass",
        meta: {
          title: "班级管理",
          elIcon: "CreditCard",
          roles: ["admin"] // 或者在子导航中设置角色
        }
      }
    ]
  },
  {
    path: "/course",
    component: Layouts,
    redirect: "/course/course-table",
    name: "CourseManage",
    meta: {
      title: "课程管理",
      elIcon: "Notebook",
      roles: ["admin"],
      alwaysShow: true // 将始终显示根菜单
    },
    children: [
      {
        path: "course-table",
        component: () => import("@/views/table/course/course.vue"),
        name: "Course",
        meta: {
          title: "课程信息",
          roles: ["admin"],
          keepAlive: true
        }
      },
      {
        path: "selectCourse-table",
        component: () => import("@/views/table/course/selectCourse.vue"),
        name: "SelectCourse",
        meta: {
          title: "安排课程",
          roles: ["admin"],
          keepAlive: true
        }
      },
      {
        path: "quantitative-table",
        component: () => import("@/views/table/course/quantitative.vue"),
        name: "Quantitative",
        meta: {
          title: "成绩量化标准",
          roles: ["admin"],
          keepAlive: true
        }
      }
    ]
  },
  {
    path: "/score",
    component: Layouts,
    redirect: "/score/score-table",
    name: "ScoreManage",
    meta: {
      title: "成绩管理",
      elIcon: "ScaleToOriginal",
      roles: ["admin", "teacher", "student"],
      alwaysShow: true
    },
    children: [
      {
        path: "score-table",
        component: () => import("@/views/table/score/score.vue"),
        name: "Score",
        meta: {
          title: "成绩管理",
          roles: ["admin", "teacher"]
        }
      },
      {
        path: "select-score",
        component: () => import("@/views/table/score/selectScore.vue"),
        name: "SelectScore",
        meta: {
          title: "成绩查询",
          roles: ["student"]
        }
      }
    ]
  }
]

const router = createRouter({
  history,
  routes: routeSettings.thirdLevelRouteCache ? flatMultiLevelRoutes(constantRoutes) : constantRoutes
})

/** 重置路由 */
export function resetRouter() {
  // 注意：所有动态路由路由必须带有 Name 属性，否则可能会不能完全重置干净
  try {
    router.getRoutes().forEach((route) => {
      const { name, meta } = route
      if (name && meta.roles?.length) {
        router.hasRoute(name) && router.removeRoute(name)
      }
    })
  } catch {
    // 强制刷新浏览器也行，只是交互体验不是很好
    window.location.reload()
  }
}

export default router
