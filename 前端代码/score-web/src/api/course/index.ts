import { request } from "@/utils/service"
import type * as Table from "./types/course"

/** 增 */
export function addCourseDataApi(data: Table.CourseData) {
  return request({
    url: "addCourse",
    method: "post",
    data
  })
}

/** 删 */
export function delCourseDataApi(id: number) {
  return request({
    url: `delCourse/${id}`,
    method: "delete"
  })
}

/** 批量删除 */
export function delBatchesDataApi(data: number[]){
  return request({
    url: "delBatchesCourse",
    method: "delete",
    data
  })
}

/** 改 */
export function updateCourseDataApi(data: Table.CourseData) {
  return request({
    url: "updateCourse",
    method: "put",
    data
  })
}

/** 查 */
//根据教师编号获取教师所授课程
export function getCourseDataByTIdApi(params: Table.TableRequestData) {
  return request<Table.TableResponseData>({
    url: "getCourseByTId",
    method: "get",
    params
  })
}

export function getCourseDataApi() {
  return request<Table.TableResponseData>({
    url: "getCourseList",
    method: "get",
  })
}

export function getCourseGroupDataApi() {
  return request<Table.CourseGroupResponseData>({
    url: "getCourseGroupList",
    method: "get",
  })
}

export function getCoursePageDataApi(params: Table.TableRequestData) {
  return request<Table.TableResponseData>({
    url: "getCoursePage",
    method: "get",
    params
  })
}

export function exportFileApi() {
  return request({
    url: "exportCourse",
    method: "get",
    responseType: 'blob'
  })
}
