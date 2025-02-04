export interface SchoolData {
    id?: string
    schoolId: number
    schoolName: string
}

export interface TableRequestData {
    /** 当前页码 */
    currentPage?: number
    /** 查询条数 */
    size?: number
    keyword?: string
}

export type TableResponseData = ApiResponseData<{
    records: SchoolData[]
    total: number
}>