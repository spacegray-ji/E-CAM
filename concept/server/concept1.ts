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


const tokens = new Map<string, UserInfo>()
/**
 * 토큰 관련 express 명령어들을 등록합니다.
 * @param exp Express 객체
 * @param db 데이터베이스
 */
async function registerToken(exp:Express, db: Db) {
  /**
   * Create token by serial
   */
  exp.post("/token/create", async (req, res) => {
    // paramter check
    const serial = req.body.serial as string | null
    const username = req.body.username as string | null
    if (serial == null || username == null) {
      responseError(res, Status.INVALID_PARAMETER, "No serial or username was provided.")
      return
    }
    const token = short.generate()
    tokens.set(token, {
      serial,
      username,
    })
    setTimeout(() => {
      tokens.delete(token)
    }, oneDay)

    responseJSON(res, Status.OK, "Token created.", { token })
  })
}

async function registerPhoto(exp:Express, db: Db) {
  const photoDB = db.collection("photos")

  app.use("/photo/", express.static("./photos"))
  exp.post("/users/:token/photo/upload", async (req, res) => {
    // parameter check
    const token = req.params.token as string
    const user = await verifyToken(token)
    if (user == null) {
      responseError(res, Status.INVALID_PARAMETER, "Invalid token.")
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
    const filenames:string[] = []
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
      const pInfo:PhotoInfo = {
        user,
        filename,
        testnumber: Math.floor(Math.random() * 1000),
      }
      await photoDB.insertOne(pInfo)
      filenames.push(filename)
    }
    const photoRes:PhotoRes = {
      uploader: user,
      filenames,
    }
    responseJSON(res, Status.OK, "Photo uploaded.", photoRes)
  })
}


async function init() {
  const db = await connectDB("localhost", "local", "concept1")
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
async function verifyToken(token:string):Promise<UserInfo | undefined | null> {
  if (tokens.has(token)) {
    return tokens.get(token)
  } else {
    return null
  }
}