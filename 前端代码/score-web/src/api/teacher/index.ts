import { request } from "@/utils/service"
import type * as Table from "./types/teacher"

/** 增 */
export function addTableDataApi(data: Table.TeacherData) {
  return request({
    url: "addTeacher",
    method: "post",
    data
  })
}

/** 删 */
export function delTableDataApi(id: number) {
  return request({
    url: `delTeacher/${id}`,
    method: "delete"
  })
}

/** 批量删除 */
export function delBatchesDataApi(data: number[]){
  return request({
    url: `delBatchesTeacher`,
    method: "delete",
    data
  })
}

/** 改 */
export function updateTableDataApi(data: Table.TeacherData) {
  return request({
    url: "updateTeacher",
    method: "put",
    data
  })
}

/** 修改密码 */
export function updateTeaPwdApi(data: Table.TeacherData) {
  return request({
    url: "updateTeaPwd",
    method: "put",
    data
  })
}

/** 查 */
export function getTableDataApi() {
  return request<Table.TableResponseData>({
    url: "getTeacherList",
    method: "get",
  })
}

export function getTableGroupDataApi() {
  return request<Table.TeacherGroupResponseData>({
    url: "getTeacherGroupList",
    method: "get",
  })
}

export function getTablePageDataApi(params: Table.TableRequestData) {
  return request<Table.TableResponseData>({
    url: "getTeacherPage",
    method: "get",
    params
  })
}

export function getTeacherInfoApi(tacherId: number) {
  return request<Table.TableResponseData>({
    url: `getTeacherById/${tacherId}`,
    method: "get",
  })
}

export function exportFileApi() {
  return request({
    url: "exportTeacher",
    method: "get",
    responseType: 'blob'
  })
}
