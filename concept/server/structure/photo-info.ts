import { UserInfo } from "./user-info"
import { ObjectId } from "mongodb"

export interface BasicPhotoInfo {
  user: UserInfo
  filename: string
  createdAt: Date
}

export interface DBPhotoInfo extends BasicPhotoInfo, PhotoModelSingle {

}

export type PhotoInfo = Omit<DBPhotoInfo, "user">

/**
 * REST 응답 구조체
 */
export interface PhotoRes {
  uploader: UserInfo
  results: Array<Omit<PhotoInfo, "user">>
}

/**
 * 모델 서버 응답 구조체
 */
export interface PhotoModelRes {
  error: boolean,
  message: string,
  statusCode: number,
  data:PhotoModelSingle[],
}

/**
 * 모델 서버 응답 - 파일 하나의 구조체
 */
export interface PhotoModelSingle {
  filename:string,
  pixelSize:number,
}