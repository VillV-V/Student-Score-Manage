import { request } from "@/utils/service"
import type * as Table from "./types/stu-class"

/** 增 */
export function addStuClassDataApi(data: Table.StuClassData) {
  return request({
    url: "addStuClass",
    method: "post",
    data
  })
}

/** 删 */
export function delStuClassDataApi(id: number) {
  return request({
    url: `delStuClass/${id}`,
    method: "delete"
  })
}

/** 批量删除 */
export function delBatchesDataApi(data: number[]){
    return request({
      url: `delBatchesStuClass`,
      method: "delete",
      data
    })
}

/** 改 */
export function updateStuClassDataApi(data: Table.StuClassData) {
  return request({
    url: "updateStuClass",
    method: "put",
    data
  })
}

/** 查 */
export function getStuClassDataApi() {
  return request<Table.TableResponseData>({
    url: "getStuClassList",
    method: "get",
  })
}

export function getStuClassPageDataApi(params: Table.TableRequestData) {
    return request<Table.TableResponseData>({
      url: "getStuClassPage",
      method: "get",
      params
    })
}

export function getStuClassPageByIdDataApi(params: Table.TableRequestData) {
  return request<Table.TableResponseData>({
    url: "getStuClassByIdPage",
    method: "get",
    params
  })
}

export function exportFileApi() {
  return request({
    url: "exportStuClass",
    method: "get",
    responseType: 'blob'
  })
}
