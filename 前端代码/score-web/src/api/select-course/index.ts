import { request } from "@/utils/service"
import type * as Table from "./types/select-course"

/** 增 */
export function addSCourseDataApi(data: Table.SCourseData) {
  return request({
    url: "addSCourse",
    method: "post",
    data
  })
}

/** 删 */
export function delSCourseDataApi(id: number) {
  return request({
    url: `delSCourse/${id}`,
    method: "delete"
  })
}

/** 批量删除 */
export function delBatchesDataApi(data: number[]) {
  return request({
    url: "delBatchesSCourse",
    method: "delete",
    data
  })
}

/** 改 */
export function updateSCourseDataApi(data: Table.SCourseData) {
  return request({
    url: "updateSCourse",
    method: "put",
    data
  })
}

/** 查 */
//查询所有课程信息
export function getAllSCourseDataApi() {
  return request<Table.SCourseResponseData>({
    url: 'getAllSCourseList',
    method: "get",
  })
}

//查询选择了该课程的班级信息
//查询已选课程的班级信息
export function getSCourseListDataByCoIdApi(params: Table.TableRequestData) {
  return request<Table.SCourseResponseData>({
    url: 'getSCourseListByCId',
    method: "get",
    params
  })
}

export function getSCourseListDataByCTIdApi(params: Table.TableRequestData) {
  return request<Table.SCourseResponseData>({
    url: 'getSCourseListByCTId',
    method: "get",
    params
  })
}

export function getSCourseListDataByCIdApi(params: Table.TableRequestData) {
  return request<Table.SCourseResponseData>({
    url: 'getSCourseListByCId',
    method: "get",
    params
  })
}

export function getSCourseListDataByTIdApi(params: Table.TableRequestData) {
  return request<Table.SCourseResponseData>({
    url: 'getSCourseListByTId',
    method: "get",
    params
  })
}

export function getSCourseByIdDataApi(params: Table.TableRequestData) {
  return request<Table.SCourseResponseData>({
    url: 'getSCourseListById',
    method: "get",
    params
  })
}

export function getSCoursePageDataApi(params: Table.TableRequestData) {
  return request<Table.SCourseResponseData>({
    url: "getSCoursePage",
    method: "get",
    params
  })
}

export function exportFileApi() {
  return request({
    url: "exportSCourse",
    method: "get",
    responseType: 'blob'
  })
}
