export interface ScoreDetailData {
    type?: string
    sdId: number
    studentId: number
    studentName?: string
    classId: number
    className?: string
    courseId: number
    courseName?: string
    qname?: string
    date?: Date
    usualItem: string
    skillItem: string
    disItem: string
    usualScore: number
    skillScore: number
    disScore: number
    totalScore: number
    sdRemark: string
    createTime?: string
    updateTime?: string
}

export interface SDRequestData {
    classId?: number
    courseId?: number
    studentId?: number
}

export type SDResponseData = ApiResponseData<{
    records: ScoreDetailData[]
    total: number
}>