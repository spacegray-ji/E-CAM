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
import SocketIO, { Server } from "socket.io"
import EventEmitter from "events"
import sharp from "sharp"

/**
 * Express 생성
 */
const app = express()
app.set("port", 3200)
app.use(fileUpload({
  useTempFiles: true,
  tempFileDir: "./tmp/",
}))

const socketio = new Server(3201)
const cameraSockets = new Map<string, SocketIO.Socket>()

const globalEvent = new EventEmitter()

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
  exp.get("/verifytoken", async (req, res) => {
    const token = req.query.token ?? ""
    const result = {
      valid: false,
      type: "",
      serial: "",
    }
    if (typeof token !== "string") {
      responseError(res, Status.INVALID_PARAMETER, "Invalid parameter.", result)
      return
    }
    const user = verifyUserToken(token)
    const camera = verifyCameraToken(token)
    if (user == null && camera == null) {
      responseJSON(res, Status.OK, "[TOKEN_NOT_FOUND] Token is invalid", result)
    } else {
      result.valid = true
      result.type = user != null ? "user" : "camera"
      result.serial = user != null ? user.serial : (camera as string)
      responseJSON(res, Status.OK, "Token is valid", result)
    }
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
      responseError(res, Status.NOT_FOUND, "[TOKEN_NOT_FOUND] Invalid token.")
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
      // check size
      if (photo.size >= 10 * oneMiB) {
        responseError(res, Status.INVALID_PARAMETER, "Photo is too large.")
        return
      }
      try {
        const photoData = await fsp.readFile(photo.tempFilePath)
        const image = await sharp(photoData).jpeg({
          mozjpeg: true
        }).toBuffer()

        // generate id
        const imageId = createID()
        // generate filename
        const filename = `${imageId}.jpg`
        await fsp.writeFile(`./photos/${filename}`, image)

        // push info
        const pInfo: BasicPhotoInfo = {
          id: imageId,
          user,
          filename,
          createdAt: new Date(Date.now()),
        }
        basicPhotos.push(pInfo)
      } catch (err) {
        console.error(err)
        responseError(res, Status.INVALID_PARAMETER, `${photo.name} Image isn't supported.`)
        return
      }
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
      globalEvent.emit("uploadPhoto", photoRes)
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
      responseError(res, Status.NOT_FOUND, "[TOKEN_NOT_FOUND] No user assigned.", result)
      return
    }
    const photos = await photoDB.find({ user }).limit(maxCount).toArray()
    result.photos.push(...removeDBInfoFromPhoto(photos as any as DBPhotoInfo[]))
    responseJSON(res, Status.OK, "Photo list.", result)
  })
  exp.post("/photo/request", async (req, res) => {
    const token = req.body.token ?? ""
    const result = {

    }

    if (typeof token !== "string") {
      responseError(res, Status.INVALID_PARAMETER, "Invalid parameter.", result)
      return
    }
    const user = verifyUserToken(token)
    if (user == null) {
      responseError(res, Status.NOT_FOUND, "[TOKEN_NOT_FOUND] No user assigned.", result)
      return
    }

    const cameraSocket = getCameraSocket(user.serial)
    if (cameraSocket == null) {
      responseError(res, Status.NOT_FOUND, "No camera assigned.", result)
      return
    }

    try {
      cameraSocket.emit("takePhotoAction")
      responseJSON(res, Status.OK, "Request success.", result)
    } catch (err) {
      responseError(res, Status.INTERNAL_SERVER_ERROR, "Cannot communicate with camera.", result)
      return
    }
  })
  exp.get("/photo/take", async (req, res) => {
    const token = req.query.token ?? ""
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
      responseError(res, Status.NOT_FOUND, "[TOKEN_NOT_FOUND] No user assigned.", result)
      return
    }

    const cameraSocket = getCameraSocket(user.serial)
    if (cameraSocket == null) {
      responseError(res, Status.NOT_FOUND, "No camera assigned.", result)
      return
    }

    try {
      cameraSocket.emit("takePhoto", user)
      const photoRes = await new Promise<PhotoRes>((res, rej) => {
        let lambda: (pres: PhotoRes) => void
        const timeout = setTimeout(() => {
          globalEvent.off("uploadPhoto", lambda)
          rej("timeout")
        }, 10000)
        lambda = (pres: PhotoRes) => {
          if (pres.uploader.serial === user.serial) {
            clearTimeout(timeout)
            globalEvent.off("uploadPhoto", lambda)
            res(pres)
          }
        }
        globalEvent.on("uploadPhoto", lambda)
      })
      result.photos.push(...photoRes.results)
      responseJSON(res, Status.OK, "Captured photo from camera.", result)
    } catch (err) {
      responseError(res, Status.INTERNAL_SERVER_ERROR, "Cannot communicate with camera.", result)
      return
    }
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
  // register socketio
  socketio.on("connection", (socket) => {
    const token = socket.handshake.query.token ?? ""
    if (typeof token !== "string") {
      socket.emit("error", "Invalid token.")
      socket.disconnect()
      return
    }
    const serial = verifyCameraToken(token)
    if (serial == null) {
      socket.emit("error", "[TOKEN_NOT_FOUND] No device found with token " + token)
      socket.disconnect()
      return
    }
    const oldSocket = cameraSockets.get(serial)
    oldSocket?.disconnect()

    // set socket
    cameraSockets.set(serial, socket)
    socket.emit("connected", {
      token,
      serial,
    })
  })
  // register route
  const db = await connectDB("127.0.0.1", "local", "concept1")
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

function getCameraSocket(serial: string) {
  const socket = cameraSockets.get(serial)
  if (socket == null) {
    return null
  }
  if (socket.disconnected) {
    cameraSockets.delete(serial)
    return null
  }
  return socket
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