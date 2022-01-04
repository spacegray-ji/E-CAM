import { UserInfo } from "./user-info"

export interface PhotoInfo {
  user: UserInfo
  filename: string
  testnumber: number
}

export interface PhotoRes {
  uploader: UserInfo
  filenames: string[]
}