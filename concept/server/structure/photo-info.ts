import { UserInfo } from "./user-info"

export interface PhotoInfo {
  user: UserInfo
  filename: string
  createdAt: Date
}

export interface PhotoRes {
  uploader: UserInfo
  filenames: string[]
}