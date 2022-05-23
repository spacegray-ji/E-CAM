import express, { Express, Response, Request } from "express"
import { GeneralResponse, Status } from "./general-resp.mjs"
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

/**
 * 쿼리의 파라메터를 불러옵니다
 * @param param 쿼리 파라메터
 * @param dfValue 기본 값
 * @returns 기본 값 혹은 파라메터 값
 */
export function getQueryParam<T extends string | number | boolean>(param: unknown | unknown[] | null | undefined, dfValue: T) {
  if (param == null) {
    return dfValue
  }
  if (Array.isArray(param)) {
    if (param.length < 0) {
      return dfValue
    }
    param = param[0]
  }
  /*
  if (isOfType(dfValue, "string")) {
    if ([ "string", "number", "boolean" ].includes(typeof param)) {
      return (param as string | number | boolean).toString()
    } else {
      return dfValue
    }
  } else if (isOfType(dfValue, "number")) {
    if (isOfType(param, "string")) return $let(parseInt(param), num => Number.isNaN(num) ? dfValue : num)
    else if (isOfType(param, "number")) return param
    else if (isOfType(param, "boolean")) return param ? 1 : 0
    else return dfValue
  } else if (isOfType(dfValue, "boolean")) {
    if (isOfType(param, "string")) return param.toLowerCase() === "true"
    else if (isOfType(param, "number")) return param === 1
    else if (isOfType(param, 'boolean')) return param
    else return dfValue
  } else {
    return dfValue
  }
  */
  if (typeof dfValue === "string") {
    switch (typeof param) {
      case "string":
        return param as T
      case "number":
        return param.toString() as T
      case "boolean":
        return param.toString() as T
      default:
        return dfValue
    }
  } else if (typeof dfValue === "number") {
    switch (typeof param) {
      case "string":
        let num = parseInt(param as string)
        return (Number.isNaN(num) || (!Number.isFinite(num)) ? dfValue : num) as T
      case "number":
        return param as T
      case "boolean":
        return (param ? 1 : 0) as T
      default:
        return dfValue
    }
  } else if (typeof dfValue === "boolean") {
    switch (typeof param) {
      case "string":
        return (param.toLowerCase() === "true") as T
      case "number":
        return (param === 1) as T
      case "boolean":
        return param as T
      default:
        return dfValue
    }
  }
  return dfValue
}

export const oneDay = 1000 * 60 * 60 * 24
export const oneMiB = 1024 * 1024

interface ResultOfTypeOf {
  bigint: bigint
  boolean: boolean
  function: (...args: Array<any>) => any
  number: number
  object: object
  string: string
  symbol: symbol
  undefined: undefined
}
const isOfType = <
  T extends keyof ResultOfTypeOf
>(value: any, type: T): value is ResultOfTypeOf[T] =>
  typeof value === type

const $let = <T, R>(value: T, fn: (value: T) => R) => fn(value)
