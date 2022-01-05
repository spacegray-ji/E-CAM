/**
 * 사진 업로드 Sample
 */

import express, { Express } from "express"
import fileUpload from "express-fileupload"
import http from "http"
import { MongoClient, Db } from "mongodb"
import fs from "fs/promises"
import { simpleResponse, responseError, responseJSON, oneDay, connectDB, asArray, asArraySafe, oneMiB } from "./util"
import { GeneralResponse, Status } from "./general-resp"
import short from "short-uuid"
import { UserInfo } from "./structure/user-info"
import { PhotoInfo, PhotoRes } from "./structure/photo-info"

/**
 * Express 생성
 */
const app = express()
app.set("port", 3200)
app.use(fileUpload({
  useTempFiles: true,
  tempFileDir: "./tmp/",
}))

let debugKey = short.uuid().toString()


const userTokens = new Map<string, UserInfo>()
const cameraTokens = new Map<string, string>()
const tokenRemoveCallbacks = new Map<string, NodeJS.Timer>()
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
    const serial = req.body.serial as string | null
    const username: string = req.body.username ?? "default"
    const isCamera: boolean = safeBoolean(req.body.isCamera)
    if (serial == null) {
      responseError(res, Status.INVALID_PARAMETER, "No serial or username was provided.")
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
  exp.put("/photo/:token", async (req, res) => {
    // parameter check
    const token = req.params.token as string
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
    // loop
    const filenames: string[] = []
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
      const filename = `${short.generate()}.jpg`
      await photo.mv(`./photos/${filename}`)
      const pInfo: PhotoInfo = {
        user,
        filename,
        createdAt: new Date(Date.now()),
      }
      await photoDB.insertOne(pInfo)
      filenames.push(filename)
    }
    const photoRes: PhotoRes = {
      uploader: user,
      filenames,
    }
    responseJSON(res, Status.OK, "Photo uploaded.", photoRes)
  })
  exp.get("/photo/:token", async (req, res) => {
    const token = req.params.token
    const maxCount:number = safeNumber(req.query.count as unknown, 10)
    const user = verifyUserToken(token)
    if (user == null) {
      responseError(res, Status.NOT_FOUND, "No user assigned.")
      return
    }
    const photos = await photoDB.find({ user }).limit(maxCount).toArray()
    responseJSON(res, Status.OK, "Photo list.", {
      photos,
    })
  })
}

async function registerDebug(exp: Express, db: Db) {
  const photoDB = db.collection("photos")
  exp.delete("/debug/photo", async (req, res) => {
    const authKey = req.body.authKey as string | null
    if (authKey != null && debugKey == authKey) {
      const photos = await photoDB.find().toArray()
      for (const photo of photos) {
        await fs.unlink(`./photos/${photo.filename}`)
        await photoDB.deleteOne({ _id: photo._id })
      }
      responseJSON(res, Status.OK, "Photo deleted.", {})
    } else {
      responseJSON(res, Status.INVALID_REQUEST, "Access denied.", {})
    }
  })
}


async function init() {
  // cleanup
  await clearTemp()
  // debug key
  debugKey = await fs.readFile("./key.txt", "utf8").catch((rej) => short.uuid().toString())
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

function queryUserBySerial(serial: string){
  for (const [, user] of userTokens) {
    if (user.serial == serial) {
      return user
    }
  }
  return null
}

function safeNumber(input:unknown | null | undefined, dfvalue:number) { 
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

function safeBoolean(input:unknown | null | undefined, dfvalue:boolean = false) 
{
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
  if (cameraTokens.has(token)) {
    return cameraTokens.get(token)
  } else {
    return null
  }
}

async function clearTemp() {
  try {
    await fs.rm("./tmp", { recursive: true })
  } catch (err) {
  }
}