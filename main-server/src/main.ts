/**
 * 사진 업로드 Sample
 */

import express, { Express } from "express"
import fileUpload from "express-fileupload"
import http from "http"
import { MongoClient, Db } from "mongodb"
import fsp from "fs/promises"
import fs from "fs"
import { simpleResponse, responseError, responseJSON, oneDay, connectDB, asArray, asArraySafe, oneMiB } from "./util"
import { GeneralResponse, Status } from "./general-resp"
import short from "short-uuid"
import { UserInfo } from "./structure/user-info"
import { BasicPhotoInfo, PhotoInfo, DBPhotoInfo, PhotoModelRes, PhotoModelSingle, PhotoRes } from "./structure/photo-info"
import got from "got-cjs"
import { FormData, File } from "formdata-node"
import { fileFromPath } from "formdata-node/file-from-path"
import { modelServer } from "./config"
import bcrypt from "bcryptjs"

/**
 * Express 생성
 */
const app = express()
app.set("port", 3200)
app.use(fileUpload({
  useTempFiles: true,
  tempFileDir: "./tmp/",
}))

function parseBody<T extends Record<string, string | number | boolean>>(value: Record<string, string> | null, defaultValue: T): T {
  const out = defaultValue
  const body = value
  if (body == null) {
    return defaultValue
  }
  try {
    for (const key of Object.keys(body)) {
      if (key.indexOf("prototype") >= 0) {
        return defaultValue
      }
      if (out[key] !== undefined && body[key] != null) {
        // type check
        if (typeof out[key] === "number") {
          const n = Number.parseInt(body[key])
          if (!Number.isNaN(n) && Number.isFinite(n)) {
            (out as any)[key] = n
          }
        } else if (typeof out[key] === "boolean") {
          const bs = body[key].toLowerCase()
          if (bs === "true") {
            (out as any)[key] = true
          } else if (bs === "false") {
            (out as any)[key] = false
          }
        } else if (typeof out[key] === "string") {
          (out as any)[key] = body[key]
        }
      }
    }
    return out
  } catch (err) {
    console.error(err)
    return defaultValue
  }
}

// 디버그 키
let debugKey = short.uuid().toString()
let saltKey = "salt"

const userTokens = new Map<string, UserInfo>()
const cameraTokens = new Map<string, string>()
const tokenRemoveCallbacks = new Map<string, NodeJS.Timer>()

let idIncrement = 1
let lastIDTime = -1
/**
 * 토큰 관련 express 명령어들을 등록합니다.
 * @param exp Express 객체
 * @param db 데이터베이스
 */
async function registerToken(exp: Express, db: Db) {
  /**
   * Create token by serial
   */
  exp.put("/token", async (req, res) => {
    // paramter check
    const { serial, username, isCamera } = parseBody(req.body, {
      serial: "",
      username: "Default",
      isCamera: false,
    })
    if (serial.length <= 0 || username.length <= 0 || serial.length > 32 || username.length > 32) {
      responseError(res, Status.INVALID_PARAMETER, "Invalid parameter.", { token: "" })
      return
    }
    const token = short.generate()
    if (!isCamera) {
      for (const [key, value] of userTokens) {
        if (value.serial == serial) {
          userTokens.delete(key)
          break
        }
      }
      userTokens.set(token, {
        serial,
        username,
      })
      setTimeout(() => {
        userTokens.delete(token)
      }, oneDay)
    } else {
      for (const [key, cameraSerial] of cameraTokens) {
        if (cameraSerial == serial) {
          cameraTokens.delete(key)
          break
        }
      }
      cameraTokens.set(token, serial)
    }
    responseJSON(res, Status.OK, "Token created.", { token })
  })
}

async function registerPhoto(exp: Express, db: Db) {
  const photoDB = db.collection("photos")

  app.use("/photo/static/", express.static("./photos"))
  exp.put("/photo", async (req, res) => {
    // paramter check
    const { token } = parseBody(req.body, {
      token: ""
    })
    const cameraSerial = verifyCameraToken(token)
    if (cameraSerial == null) {
      responseError(res, Status.NOT_FOUND, "Invalid token.")
      return
    }
    const user = queryUserBySerial(cameraSerial)
    if (user == null) {
      responseError(res, Status.NOT_FOUND, "No user assigned.")
      return
    }
    const photos = asArraySafe(req.files?.photo)
    if (photos == null) {
      responseError(res, Status.INVALID_PARAMETER, "No photo was provided.")
      return
    }
    // check file size
    if (photos.length >= 10) {
      responseError(res, Status.INVALID_PARAMETER, "Too many photos.")
      return
    }
    // check fields
    const basicPhotos: BasicPhotoInfo[] = []
    for (const photo of photos) {
      // check jpeg
      if (photo.mimetype != "image/jpeg") {
        responseError(res, Status.INVALID_PARAMETER, "Only jpeg is supported.")
        return
      }
      // check size
      if (photo.size >= 10 * oneMiB) {
        responseError(res, Status.INVALID_PARAMETER, "Photo is too large.")
        return
      }
      // generate id
      const imageId = createID()
      // generate filename
      const filename = `${imageId}.jpg`
      await photo.mv(`./photos/${filename}`)

      // push info
      const pInfo: BasicPhotoInfo = {
        id: imageId,
        user,
        filename,
        createdAt: new Date(Date.now()),
      }
      basicPhotos.push(pInfo)
      // await photoDB.insertOne(pInfo)
    }
    // parse from model server
    const photoForm = new FormData()
    const labeledPhotos: Array<DBPhotoInfo> = []
    for (const bphoto of basicPhotos) {
      photoForm.append("images", await fileFromPath(`./photos/${bphoto.filename}`))
    }
    try {
      const modelRes = await got.post(`${modelServer}/process`, {
        body: photoForm
      }).json() as PhotoModelRes
      if (modelRes.error) {
        responseError(res, Status.INVALID_PARAMETER, "Wrong uploaded file.")
        return
      }
      // map photo info
      const valueMap = new Map<string, PhotoModelSingle>()
      for (const respData of modelRes.data) {
        valueMap.set(respData.filename, respData)
      }
      // push to database
      for (const bphoto of basicPhotos) {
        const valueInfo = valueMap.get(bphoto.filename)
        if (valueInfo == null) {
          responseError(res, Status.INTERNAL_SERVER_ERROR, `Unexpected error. from getting model server. ${JSON.stringify(modelRes)}`)
          return
        }
        const pInfo: DBPhotoInfo = {
          ...bphoto,
          ...valueInfo,
        }
        labeledPhotos.push(pInfo)
      }
      await photoDB.insertMany(labeledPhotos)

      const photoRes: PhotoRes = {
        uploader: user,
        results: removeDBInfoFromPhoto(labeledPhotos),
      }
      responseJSON(res, Status.OK, "Photo uploaded.", photoRes)
    } catch (err) {
      console.error(err)
      responseError(res, Status.INTERNAL_SERVER_ERROR, "Internal Server Error. (Model server)")
    }
  })
  exp.get("/photo", async (req, res) => {
    const token = req.query.token ?? ""
    const maxCount: number = safeNumber(req.query.count as unknown, 10)
    const result = {
      dirpath: "/photo/static/",
      photos: [] as PhotoInfo[],
    }
    if (typeof token !== "string") {
      responseError(res, Status.INVALID_PARAMETER, "Invalid parameter.", result)
      return
    }
    const user = verifyUserToken(token)
    if (user == null) {
      responseError(res, Status.NOT_FOUND, "No user assigned.", result)
      return
    }
    const photos = await photoDB.find({ user }).limit(maxCount).toArray()
    result.photos.push(...removeDBInfoFromPhoto(photos as any as DBPhotoInfo[]))
    responseJSON(res, Status.OK, "Photo list.", result)
  })
}

async function registerDebug(exp: Express, db: Db) {
  const photoDB = db.collection("photos")
  exp.delete("/debug/photo", async (req, res) => {
    if (req.body == null) {
      responseError(res, Status.INVALID_PARAMETER, "No body received.")
      return
    }

    const authKey = req.body.authKey as string | null
    if (authKey != null && debugKey == authKey) {
      const photos = await photoDB.find().toArray()
      for (const photo of photos) {
        await fsp.unlink(`./photos/${photo.filename}`)
        await photoDB.deleteOne({ _id: photo._id })
      }
      responseJSON(res, Status.OK, "Photo deleted.", {})
    } else {
      responseJSON(res, Status.INVALID_REQUEST, "Access denied.", {})
    }
  })
  exp.post("/debug/photo/model", async (req, res) => {
    if (req.body == null) {
      responseError(res, Status.INVALID_PARAMETER, "No body received.")
      return
    }

    const authKey = req.body.authKey as string | null
    if (authKey != null && debugKey == authKey) {
      const photos = asArraySafe(req.files?.photo)
      if (photos == null) {
        responseError(res, Status.INVALID_PARAMETER, "No photo was provided.")
        return
      }
      const photoList: string[] = []
      for (const photo of photos) {
        const filename = `${short.generate()}.jpg`
        const tempPath = `./tmp/${filename}`
        await photo.mv(tempPath)
        photoList.push(tempPath)
      }
      // Upload REST
      const photoForm = new FormData()
      for (const path of photoList) {
        photoForm.append("images", await fileFromPath(path))
      }
      const modelRes = await got.post(`${modelServer}/process`, {
        body: photoForm
      }).json()

      responseJSON(res, Status.OK, "Response from model server", modelRes)
    } else {
      responseJSON(res, Status.INVALID_REQUEST, "Access denied.", {})
    }
  })
}


async function init() {
  // cleanup
  await clearTemp()
  // debug key
  debugKey = await fsp.readFile("./key.txt", "utf8").catch((rej) => short.uuid().toString())
  // salt key
  saltKey = await fsp.readFile("./globalsalt.txt", "utf8").catch((rej) => "salt")
  // register route
  const db = await connectDB("localhost", "local", "concept1")
  registerDebug(app, db)
  registerToken(app, db)
  registerPhoto(app, db)
  const server = http.createServer(app)
  server.listen(app.get("port"), () => {
    console.log(`Express server listening on port ${app.get("port")}`)
  })
}

init()
////////////////////////////////////////////////////////////////////////////////
// 유틸리티
////////////////////////////////////////////////////////////////////////////////

/**
 * 토큰이 메모리에 있는지 검사합니다.
 * @param token 토큰
 * @returns 토큰이 있으면 `UserInfo`, 없으면 `null`
 */
function verifyUserToken(token: string): UserInfo | undefined | null {
  if (userTokens.has(token)) {
    return userTokens.get(token)
  } else {
    return null
  }
}

function queryUserBySerial(serial: string) {
  for (const [, user] of userTokens) {
    if (user.serial == serial) {
      return user
    }
  }
  return null
}

function safeNumber(input: unknown | null | undefined, dfvalue: number) {
  if (input == null) {
    return dfvalue
  }
  if (typeof input == "number") {
    return input
  }
  if (typeof input == "string") {
    const value = Number.parseInt(input)
    if (Number.isNaN(value) || !Number.isFinite(value)) {
      return dfvalue
    }
    return value
  }
  return dfvalue
}

function safeBoolean(input: unknown | null | undefined, dfvalue: boolean = false) {
  if (input == null) {
    return dfvalue
  }
  if (typeof input == "boolean") {
    return input
  }
  if (typeof input == "string") {
    const value = input.toLowerCase()
    if (value == "true") {
      return true
    } else if (value == "false") {
      return false
    }
  }
  return dfvalue
}

/**
 * 카메라 토큰이 메모리에 있는지 검사합니다.
 * @param token 토큰
 * @returns 토큰이 있으면 시리얼(`string`), 없으면 `null`
 */
function verifyCameraToken(token: string): string | undefined | null {
  if (token.length <= 0) {
    return null
  }
  if (cameraTokens.has(token)) {
    return cameraTokens.get(token)
  } else {
    return null
  }
}

async function clearTemp() {
  try {
    await fsp.rm("./tmp", { recursive: true })
  } catch (err) {
  }
}

function removeDBInfoFromPhoto(pinfo: Array<DBPhotoInfo>) {
  return pinfo.map((p) => {
    if ((p as any)["_id"] != null) {
      delete (p as any)["_id"]
    }
    delete (p as any)["user"]
    return p as PhotoInfo
  })
}

function createID() {
  // Discord snowflake format
  // With some garbage value..?
  // https://discordapp.com/developers/docs/reference#snowflakes
  if (Date.now() - lastIDTime >= 1000 * 60 * 60 * 24) {
    lastIDTime = Date.now()
    idIncrement = 1
  }
  const sftime = BigInt(Date.now() - 1420070400000)
  const sfshift = sftime << 22n
  const randID = BigInt(Math.floor(Math.random() * 1024)) << 12n
  const increment = BigInt(idIncrement++)

  return (sfshift | randID | increment).toString()
}