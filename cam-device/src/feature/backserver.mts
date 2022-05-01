import express from "express"
import Debug from "debug"
import chalk, { Chalk } from "chalk"
import Path from "node:path"
import { myOS, OSType } from "../struct/ostype.mjs"
import { cameraName, githubURL, mainServerHost, streamingPort } from "../struct/conf.mjs"
import { ensureCameraToken, MainServerResp, TokenObj } from "../mainreq.mjs"
import multer from "multer"
import FormData from "form-data"
import got from "got"
import fsp from "node:fs/promises"

const debug = Debug("molloo:backServer")

export class BackServer {
  public httpPort: number
  protected expressApp = express()
  protected tokenObj: TokenObj
  public constructor(port: number, tokenObj: TokenObj) {
    this.httpPort = port
    this.tokenObj = tokenObj
  }
  /**
   * Express server 등록
   * @returns 
   */
  public async start() {
    // Use webrtc-streamer static files
    this.expressApp.use("/streamer/", express.static(
      Path.resolve(".", "webrtc-streamer", "html")
    ))
    // Register Route
    this.registerRoute()
    // Use custom static files
    this.expressApp.use("/", express.static("./static"))
    // Listen to port
    return new Promise<void>((res, rej) => {
      const timeout = setTimeout(() => rej("Server Timeout"), 10000)
      this.expressApp.listen(this.httpPort, () => {
        debug(`Express server listening on port ${chalk.green(this.httpPort.toString())}`)
        clearTimeout(timeout)
        res()
      })
    })
  }
  /**
   * API Router 등록
   */
  protected registerRoute() {
    const upload = multer({ dest: "./uploads" })
    // root router (readme.md)
    this.expressApp.get("/", (req, res) => {
      res.redirect(githubURL)
    })
    // camera name
    this.expressApp.get("/api/cameraName", (req, res) => {
      res.json({
        name: cameraName,
      })
    })
    this.expressApp.get("/api/cameraPort", (req, res) => {
      res.json({
        port: streamingPort,
      })
    })
    // upload photo
    this.expressApp.post("/api/photo", upload.single("photo"), async (req, res) => {
      debug(`New Photo Upload Request: ${req.socket.remoteAddress}:${req.socket.remotePort} - ${req.file?.originalname ?? "no file"}`)
      const file = req.file
      // check failcase
      if (file == null) {
        res.status(400).json({
          error: "No photo uploaded",
        })
        return
      }
      if (file.size >= 1024 * 1024 * 10) {
        res.status(400).json({
          error: "Photo too large",
        })
        return
      }
      if (file.mimetype !== "image/jpeg") {
        res.status(400).json({
          error: "Photo is not jpeg. Only support jpeg.",
        })
        return
      }
      // upload to main server
      const fileBuffer = await fsp.readFile(Path.resolve(file.path), { encoding: null })
      const result = await ensureCameraToken(this.tokenObj, async (token) => {
        const uploadBody = new FormData()
        uploadBody.append("token", token)
        uploadBody.append("photo", fileBuffer, { filename: file.originalname })
        const uploadRes = await got(`${mainServerHost}/photo`, {
          method: "PUT",
          throwHttpErrors: false,
          body: uploadBody,
        }).json() as MainServerResp<{ results: unknown[] }>
        return uploadRes
      })
      if (result.isError) {
        res.status(500).json({
          error: result.message,
        })
        return
      } else {
        res.status(200).json({
          uploaded: result.data.results[0],
        })
      }
      try {
        await fsp.rm(file.path)
      } catch (err) {
        console.error(err)
      }
    })
  }
}