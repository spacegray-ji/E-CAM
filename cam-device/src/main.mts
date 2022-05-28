import express from "express"
import http from "node:http"
import { checkStreamer, checkProgram } from "./feature/initcheck.mjs"
import got from "got"
import { spawn } from "node:child_process"
import chalk from "chalk"
import { BackServer } from "./feature/backserver.mjs"
import { camLedPin, expressPort, redBtnPin, useWebRTC, wsPort } from "./struct/conf.mjs"
import { WSServer } from "./feature/wsserver.mjs"
import { GPIORecv } from "./feature/gpiorecv.mjs"
import Debug from "debug"
import { myOS, OSType } from "./struct/ostype.mjs"
import { getCameraToken, TokenObj } from "./mainreq.mjs"
import fsp from "fs/promises"
import { SIOClient } from "./feature/sioclient.mjs"
import puppeteer from "puppeteer"

const debug = Debug("molloo:main")

async function main() {
  // token
  const tokenObj: TokenObj = { token: "", serial: "" }
  // get token & check serial & main-server
  try {
    tokenObj.serial = JSON.parse(await fsp.readFile("./serial.json", "utf8")).serial
    debug(`Device Serial: ${chalk.green(tokenObj.serial)}`)
    tokenObj.token = await getCameraToken(tokenObj.serial)
    debug(`Device Token: ${chalk.green(tokenObj.token)}`)
  } catch (err) {
    console.error(chalk.red("Generating token failed. Check main server or serial.json file."))
    await fsp.writeFile("./serial.json", JSON.stringify({ serial: "" }, null, 4))
    console.error(err)
    return
  }

  // Program installation
  const isProgramReady = await checkProgram()
  if (!isProgramReady) {
    console.error(chalk.red(`Please check your environment.`))
    process.exit(1)
  }

  // WebRTC-Streamer
  if (useWebRTC) {
    const isStreamReady = await checkStreamer()
    if (!isStreamReady) {
      console.error(chalk.red(`WebRTC-Streamer cannot work. Please report to developer with logs.`))
      process.exit(1)
    }
  }

  // Express Server
  const backServer = new BackServer(expressPort, tokenObj)
  await backServer.start()

  // Websocket Server
  const wsServer = new WSServer(wsPort)
  await wsServer.start()

  // Socket.io client
  const sioClient = new SIOClient(tokenObj)
  await sioClient.connect()

}

async function mainP() {
  await puppeteer.launch({
    headless: false,
    args: [
      "--enable-features=VaapiVideoDecoder",
      "--use-gl=desktop",
      "--disable-features=UseOzonePlatform",
      "--ignore-gpu-blocklist",
      "--enable-gpu-rasterization",
      "--enable-zero-copy",
      "--enable-drdc",
      "--canvas-oop-rasterization",
      "--start-fullscreen"
    ],
    ignoreDefaultArgs: ["--enable-automation"],
    defaultViewport: null,
  })
}

// 

main()

// init
/*
const app = express()
app.set("port", 8080)

app.use("/streamer/", express.static("./webrtc-streamer/html/"))

app.use("/", express.static("./static"))

const server = http.createServer(app)
server.listen(app.get("port"), () => {
  console.log(`Express server listening on port ${app.get("port")}`)
})
*/