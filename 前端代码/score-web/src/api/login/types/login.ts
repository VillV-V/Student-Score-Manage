export interface LoginRequestData {
  /** 用户名*/
  username: string
  /** 密码 */
  password: string
  /** 验证码 */
  code: string
  /** 用户类型 */
  role: string
}

export interface UserRequestData {
  id: number
  /** 用户名*/
  username: string
  /** 用户类型 */
  roles: string[]
}

export interface AdminData {
  type?: string
  id: number
  username: string
  teacherId: number
  teacherName?: string
  gender?: string
  title?: string
  telephone?: string
  email?: string
  schoolId?: number
  schoolName?: string
  password?: string
}

export type AdminResponseData = ApiResponseData<{
  records: AdminData[]
  total: number
}>

export type LoginCodeResponseData = ApiResponseData<string>

export type LoginResponseData = ApiResponseData<{ token: string, username: string, roles: string[] }>

export type UserInfoResponseData = ApiResponseData<{ id:number, username: string, roles: string[] }>
