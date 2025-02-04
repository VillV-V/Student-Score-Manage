import { request } from "@/utils/service"
import type * as Table from "./types/score"

/** 增 */
export function addScoreDataApi(data: Table.ScoreData) {
  return request({
    url: "addScore",
    method: "post",
    data
  })
}

/** 删 */
export function delScoreDataApi(id: number) {
  return request({
    url: `delScore/${id}`,
    method: "delete"
  })
}

/** 批量删除 */
export function delBatchesDataApi(data: number[]){
    return request({
      url: `delBatchesScore`,
      method: "delete",
      data
    })
}

/** 改 */
export function updateScoreDataApi(data: Table.ScoreData) {
  return request({
    url: "updateScore",
    method: "put",
    data
  })
}

/** 查 */
export function getAllScoreDataApi() {
  return request<Table.ScoreResponseData>({
    url: "getAllScoreList",
    method: "get",
  })
}

export function getScoreDataByCIdApi(params: Table.ScoreRequestData) {
  return request<Table.ScoreResponseData>({
    url: "getScoreListByClassId",
    method: "get",
    params
  })
}

export function getScoreDataByCCIdApi(params: Table.ScoreRequestData) {
  return request<Table.ScoreResponseData>({
    url: "getScoreListByCCId",
    method: "get",
    params
  })
}

//按学号查询成绩信息
export function getScoreDataByStuIdApi(params: Table.ScoreRequestData) {
  return request<Table.ScoreResponseData>({
    url: "getScoreListByStuId",
    method: "get",
    params
  })
}

//按成绩排名
export function getScoreRankApi(params: Table.ScoreRequestData) {
  return request<Table.ScoreResponseData>({
    url: "getScoreRank",
    method: "get",
    params
  })
}

//导出各班级所有选课的所有学生成绩
export function exportFileApi(url: string, params: Table.ScoreRequestData) {
  return request({
    url: url,
    method: "get",
    params,
    responseType: 'blob'  // 设置响应类型为 blob
  })
}
