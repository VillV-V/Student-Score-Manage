import { request } from "@/utils/service"
import type * as Table from "./types/school"

/** 增 */
export function addSchoolDataApi(data: Table.SchoolData) {
  return request({
    url: "addSchool",
    method: "post",
    data
  })
}

/** 删 */
export function delSchoolDataApi(id: number) {
  return request({
    url: `delSchool/${id}`,
    method: "delete"
  })
}

/** 批量删除 */
export function delBatchesDataApi(data: number[]){
  return request({
    url: "delBatchesSchool",
    method: "delete",
    data
  })
}

/** 改 */
export function updateSchoolDataApi(data: Table.SchoolData) {
  return request({
    url: "updateSchool",
    method: "put",
    data
  })
}

/** 查 */
export function getSchoolDataApi() {
  return request<Table.TableResponseData>({
    url: "getSchoolList",
    method: "get",
  })
}

/** 查 */
export function getSchoolPageDataApi(params: Table.TableRequestData) {
  return request<Table.TableResponseData>({
    url: "getSchoolPage",
    method: "get",
    params
  })
}

export function exportFileApi() {
  return request({
    url: "exportSchool",
    method: "get",
    responseType: 'blob'
  })
}
