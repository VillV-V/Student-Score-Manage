export interface StuClassData {
    id?: string
    classId: number
    className: string
    grade: string
    majorId?: number
    majorName: string
}

export interface TableRequestData {
    /** 当前页码 */
    currentPage?: number
    /** 查询条数 */
    size?: number
    teacherId?: number
    keyword?: string
}

export type TableResponseData = ApiResponseData<{
    records: StuClassData[]
    total: number
}>