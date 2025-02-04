export interface ScoreData {
    type?: string
    sid: number
    studentId: number
    studentName?: string
    telephone?: string
    email?: string
    classId: number
    className?: string
    courseId: number
    courseName?: string
    qname?: string
    usualScore: number
    skillScore: number
    disScore: number
    totalScore: number
}

export interface ScoreRequestData {
    id?: number
    classId?: number
    courseId?: number
    studentId?: number
}

export type ScoreResponseData = ApiResponseData<{
    records: ScoreData[]
    total: number
    label?: [
        {
            uPLabel: string
            sALabel: string
            dALabel: string
        }
    ]
}>
