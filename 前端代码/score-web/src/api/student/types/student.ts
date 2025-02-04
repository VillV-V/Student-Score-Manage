export interface StudentData {
    id?: string
    studentId: number
    studentName: string
    gender: string
    telephone: string
    email: string
    classId?: number
    className: string
    majorId?: number
    majorName: string
    schoolId?: number
    schoolName: string
    password?: string
}

export interface StudentRequestData {
    /** 当前页码 */
    currentPage: number
    /** 查询条数 */
    size: number
    /** 查询参数：关键词 */
    keyword?: string
}

export type StudentResponseData = ApiResponseData<{
    records: StudentData[]
    total: number
}>