import { request } from "@/utils/service"
import type * as Table from "./types/major"

/** 增 */
export function addMajorDataApi(data: Table.MajorData) {
  return request({
    url: "addMajor",
    method: "post",
    data
  })
}

/** 删 */
export function delMajorDataApi(id: number) {
  return request({
    url: `delMajor/${id}`,
    method: "delete"
  })
}

/** 批量删除 */
export function delBatchesDataApi(data: number[]){
    return request({
      url: `delBatchesMajor`,
      method: "delete",
      data
    })
}

/** 改 */
export function updateMajorDataApi(data: Table.MajorData) {
  return request({
    url: "updateMajor",
    method: "put",
    data
  })
}

/** 查 */
export function getMajorDataApi() {
  return request<Table.TableResponseData>({
    url: "getMajorList",
    method: "get",
  })
}

export function getMajorPageDataApi(params: Table.TableRequestData) {
  return request<Table.TableResponseData>({
    url: "getMajorPage",
    method: "get",
    params
  })
}

export function exportFileApi() {
  return request({
    url: "exportMajor",
    method: "get",
    responseType: 'blob'
  })
}
