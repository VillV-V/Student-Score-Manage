export interface SCourseData {
  type?: string
  id: number
  classId: number
  className: string
  courseId: number
  courseName: string
  schoolName?: string
  teacherId: number
  teacherName: string
  qname?: string
  date: string
}

export interface TableRequestData {
  /** 当前页码 */
  currentPage?: number
  /** 查询条数 */
  size?: number
  classId?: number
  courseId?: number
  teacherId?: number
}

export type SCourseResponseData = ApiResponseData<{
  records: SCourseData[]
  total: number
}>
