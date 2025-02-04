import { request } from "@/utils/service"
import type * as Table from "./types/score-detail"

/** 增 */
export function addSDDataApi(data: Table.ScoreDetailData) {
  return request({
    url: "addScoreDetail",
    method: "post",
    data
  })
}

/** 删 */
export function delSDDataApi(id: number) {
  return request({
    url: `delScoreDetail/${id}`,
    method: "delete"
  })
}

/** 批量删除 */
export function delBatchesDataApi(data: number[]){
    return request({
      url: `delBatchesScoreDetail`,
      method: "delete",
      data
    })
}

/** 改 */
export function updateSDDataApi(data: Table.ScoreDetailData) {
  return request({
    url: "updateScoreDetail",
    method: "put",
    data
  })
}

/** 查 */
export function getScoreDataBySCIdApi(params: Table.SDRequestData) {
  return request<Table.SDResponseData>({
    url: "getSDListBySCId",
    method: "get",
    params
  })
}
