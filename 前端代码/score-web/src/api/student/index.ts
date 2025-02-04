import { request } from "@/utils/service"
import type * as Table from "./types/student"

/** 增 */
export function addTableDataApi(data: Table.StudentData) {
  return request({
    url: "addStudent",
    method: "post",
    data
  })
}

/** 删 */
export function delTableDataApi(id: number) {
  return request({
    url: `delStudent/${id}`,
    method: "delete"
  })
}

/** 批量删除 */
export function delBatchesDataApi(data: number[]){
  return request({
    url: `delBatchesStudent`,
    method: "delete",
    data
  })
}

/** 改 */
export function updateTableDataApi(data: Table.StudentData) {
  return request({
    url: "updateStudent",
    method: "put",
    data
  })
}

/** 修改学生密码 */
export function updateStuPwdApi(data: Table.StudentData) {
  return request({
    url: "updateStuPwd",
    method: "put",
    data
  })
}

/** 查 */
export function getStudentByIdDataApi(id: number) {
  return request<Table.StudentResponseData>({
    url: `getStudentList/${id}`,
    method: "get",
  })
}

export function getStudentByClassIdDataApi(id: number) {
  return request<Table.StudentResponseData>({
    url: `getStudentByClassId/${id}`,
    method: "get",
  })
}

export function getTableDataApi(params: Table.StudentRequestData) {
  return request<Table.StudentResponseData>({
    url: "getStudentPage",
    method: "get",
    params
  })
}

export function getStudentInfoApi(studentId: number) {
  return request<Table.StudentResponseData>({
    url: `getStudentById/${studentId}`,
    method: "get",
  })
}

export function exportFileApi() {
  return request({
    url: "exportStudent",
    method: "get",
    responseType: 'blob'
  })
}
