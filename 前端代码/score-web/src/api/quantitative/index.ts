import { request } from "@/utils/service"
import type * as Table from "./types/quantitative"

/** 增 */
export function addQuantitativeDataApi(data: Table.QuantitativeData) {
  return request({
    url: "addQuantitative",
    method: "post",
    data
  })
}

/** 删 */
export function delQuantitativeDataApi(id: number) {
  return request({
    url: `deleteQuantitative/${id}`,
    method: "delete"
  })
}

/** 批量删除 */
export function delBatchesDataApi(data: number[]){
  return request({
    url: "delBatchesQuantitative",
    method: "delete",
    data
  })
}

/** 改 */
export function updateQuantitativeDataApi(data: Table.QuantitativeData) {
  return request({
    url: "updateQuantitative",
    method: "put",
    data
  })
}

/** 查 */
export function getQuantitativeDataApi() {
  return request<Table.QuantitativeResponseData>({
    url: "getQuantitativeList",
    method: "get",
  })
}

/** 查 */
export function getQPageDataApi(params: Table.QuantitativeRequestData) {
  return request<Table.QuantitativeResponseData>({
    url: "getQPage",
    method: "get",
    params
  })
}