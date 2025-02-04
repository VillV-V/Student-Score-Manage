export interface QuantitativeData {
    type?: string
    qid: number
    qname: string
    usualScore: number
    skillScore: number
    disScore: number
    remark: string
}

export interface QuantitativeRequestData {
    /** 当前页码 */
    currentPage?: number
    /** 查询条数 */
    size?: number
    keyword?: string
}

export type QuantitativeResponseData = ApiResponseData<{
    records: QuantitativeData[]
    total: number
}>