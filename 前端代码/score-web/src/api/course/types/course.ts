export interface CourseData {
    id?: string
    courseId: number
    courseName: string
    type: string
    schoolId?: number
    schoolName: string
    classPeriod: number
    practicalClassPeriod: number
    qid?: string,
    qname: string
}

export interface TableRequestData {
    /** 当前页码 */
    currentPage?: number
    /** 查询条数 */
    size?: number
    teacherId?: number
    keyword?: string
}

export interface CourseGroupData {
    schoolName: string
    courseList: [
        {
            courseId: number,
            courseName: string
        }
    ]
}

export type CourseGroupResponseData = ApiResponseData<{
    records: CourseGroupData[]
}>

export type TableResponseData = ApiResponseData<{
    records: CourseData[]
    total: number
}>
