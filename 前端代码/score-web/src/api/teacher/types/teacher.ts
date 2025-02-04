export interface TeacherData {
    id?: string
    teacherId: number
    teacherName: string
    gender: string
    title: string
    telephone: string
    email: string
    schoolId?: number
    schoolName: string
    password?: string
}

export interface TableRequestData {
    /** 当前页码 */
    currentPage: number
    /** 查询条数 */
    size: number
    /** 查询参数：关键词 */
    keyword?: string
}

export interface TeacherGroupData {
    schoolName: string
    teacherList: [
        {
            teacherId: number,
            teacherName: string
        }
    ]
}

export type TeacherGroupResponseData = ApiResponseData<{
    records: TeacherGroupData[]
}>

export type TableResponseData = ApiResponseData<{
    records: TeacherData[]
    total: number
}>