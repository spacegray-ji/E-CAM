import express, { Express, Response, Request } from "express"
import { GeneralResponse, Status } from "./general-resp"
import { MongoClient, Db } from "mongodb"

export function simpleResponse(res: express.Response, status: number, message: string) {
  res.status(status).send(`<h1>${message}</h1>`)
}

export function asArray<T>(param: T | Array<T>): Array<T> {
  return Array.isArray(param) ? param : [param]
}
export function asArraySafe<T>(param: T | Array<T> | null | undefined): Array<T> | null {
  if (param == null) {
    return null
  }
  return Array.isArray(param) ? param : [param]
}

/**
 * REST 응답으로 에러를 반환합니다.
 * @param res Express Response
 * @param status 상태 코드
 * @param message 에러 메세지
 */
export function responseError<T>(res: Response, status: Status, message: string, typedData?: T) {
  const data: GeneralResponse<T | {}> = {
    status,
    message,
    data: typedData ?? {},
    isError: true,
  }
  res.status(status).json(data)
}

/**
 * REST 응답으로 JSON 형식의 데이터를 반환합니다.
 * @param res Express Response
 * @param status 상태 코드
 * @param message 자료 메세지
 * @param data JSON 데이터
 */
export function responseJSON<T>(res: Response, status: Status, message: string, data: T) {
  const respData: GeneralResponse<T> = {
    status,
    message,
    data,
    isError: false,
  }
  res.status(status).json(respData)
}

export async function connectDB(host: string, pagename: string, dbname: string) {
  const _database = await MongoClient.connect(`mongodb://${host}/${pagename}`)
  const _dbase = _database.db(dbname)
  return _dbase
}

export const oneDay = 1000 * 60 * 60 * 24
export const oneMiB = 1024 * 1024