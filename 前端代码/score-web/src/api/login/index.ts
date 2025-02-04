import { request } from "@/utils/service"
import type * as Login from "./types/login"

/** 获取登录验证码 */
export function getLoginCodeApi() {
  return request<Login.LoginCodeResponseData>({
    url: "getCode",
    method: "get"
  })
}

/** 登录并返回 Token */
export function loginApi(data: Login.LoginRequestData) {
  return request<Login.LoginResponseData>({
    url: "login",
    method: "post",
    data
  })
}

/** 注销登录 */
export function logoutApi() {
  return request({
    url: "logout",
    method: "get",
  })
}

/** 获取用户详情 */
export function getUserInfoApi(data: Login.UserRequestData) {
  return request<Login.UserInfoResponseData>({
    url: "users/info",
    method: "post",
    data
  })
}

/** 获取管理员信息 */
export function getAdminInfoApi(id: number) {
  return request<Login.AdminResponseData>({
    url: `getAdminInfo/${id}`,
    method: "get",
  })
}

/** 修改管理员信息 */
export function upAdminInfoApi(data: Login.AdminData) {
  return request({
    url: "upAdminInfo",
    method: "post",
    data
  })
}

/** 修改管理员密码 */
export function upAdminPwdApi(data: Login.AdminData) {
  return request({
    url: "upAdminPwd",
    method: "post",
    data
  })
}
