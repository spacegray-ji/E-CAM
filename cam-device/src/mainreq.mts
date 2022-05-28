import FormData from "form-data"
import got from "got"
import { mainServerHost } from "./struct/conf.mjs"

export type TokenObj = { token: string, serial: string }
export type MainServerResp<T> = { status: number, isError: boolean, message: string, data: T }
export type SocketHandshakeRes = { token: string, serial: string }

export async function getCameraToken(serial: string) {
  const tokenBody = new FormData()
  tokenBody.append("serial", serial)
  tokenBody.append("isCamera", "true")
  const tokenRes: { data: { token: string }, isError: boolean } = await got(`${mainServerHost}/token`, {
    method: "PUT",
    throwHttpErrors: false,
    body: tokenBody
  }).json()
  if (tokenRes.isError) {
    console.error(JSON.stringify(tokenRes, null, 2))
    throw new Error("Server error")
  }
  const token = tokenRes.data.token
  return token
}

export async function ensureCameraToken<T extends MainServerResp<unknown>>(tokenobj: TokenObj, callback: (token: string) => Promise<T>): Promise<T> {
  let result = await callback(tokenobj.token)
  if (result.isError && result.message.indexOf("TOKEN_NOT_FOUND") >= 0) {
    tokenobj.token = await getCameraToken(tokenobj.serial)
    result = await callback(tokenobj.token)
  }
  return result
}