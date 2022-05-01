import { io, Socket } from "socket.io-client"
import { getCameraToken, SocketHandshakeRes, TokenObj } from "../mainreq.mjs"
import { mainSIOHost, nextjsPort } from "../struct/conf.mjs"
import Debug from "debug"
import puppeteer from "puppeteer"
import { exec, sleep } from "../util.mjs"
import { myOS, OSType } from "../struct/ostype.mjs"

const debug = Debug("molloo:sio-client")

export class SIOClient {
  protected tokenObj: TokenObj
  protected socket: Socket | null = null
  protected timeoutInterval = 1000
  protected timeoutQueue: NodeJS.Timeout | null = null
  protected runningBrowser = false
  public constructor(tokenobj: TokenObj) {
    this.tokenObj = tokenobj
  }
  public async connect(timeout: number = 5000) {
    try {
      debug(`[Socket.io] Trying conecting to ${mainSIOHost}`)
      const sock = await new Promise<Socket>((res, rej) => {
        const waiter = setTimeout(() => rej("Timeout"), timeout)
        const socket = io(mainSIOHost, {
          query: {
            token: this.tokenObj.token,
          },
          transports: ["websocket"],
        })
        socket.once("connected", (r: SocketHandshakeRes) => {
          debug(`Socket connected: ${JSON.stringify(r)}`)
          this.timeoutInterval = 1000
          clearTimeout(waiter)
          res(socket)
        })
        socket.on("error", async (text: string) => {
          if (text.startsWith("No device found with token")) {
            if (!socket.disconnected) {
              socket.disconnect()
            }
          }
        })
      })
      sock.once("disconnect", async (reason) => {
        debug(`Socket disconnected: ${reason}`)
        this.queueReconnect()
      })
      await this.registerEvents(sock)
      return sock
    } catch (err) {
      debug("Socket connection failed! error: " + err)
      this.queueReconnect()
      return null
    }
  }
  public async queueReconnect() {
    this.timeoutInterval = Math.min(this.timeoutInterval * 2, 1000 * 60 * 60 * 2)
    debug(`[Socket.io] Reconnecting in ${Math.floor(this.timeoutInterval / 1000)}s`)
    if (this.timeoutQueue == null) {
      this.timeoutQueue = setTimeout(async () => {
        this.timeoutQueue = null
        try {
          this.tokenObj.token = await getCameraToken(this.tokenObj.serial)
          await this.connect(this.timeoutInterval)
        } catch (err) {
          debug("[Socket.io] Reconnect failed!")
          this.queueReconnect()
        }
      }, this.timeoutInterval + 100)
    }
  }
  public async disconnect() {
    if (this.socket != null) {
      this.socket?.disconnect()
      this.socket = null
    }
  }
  protected async registerEvents(socket: Socket) {
    socket.on("takePhotoAction", async () => {
      debug("takePhotoAction event acquired")
      this.launchBrowser()
    })
  }
  protected async launchBrowser() {
    if (!this.runningBrowser) {
      this.runningBrowser = true
      // awake screen if linux
      if (myOS === OSType.LINUX) {
        try {
          await exec("xset -display :0 dpms force on")
        } catch (err) {
          debug("Exec Error: " + err)
        }
      }
      const browser = await puppeteer.launch({
        headless: false,
        args: [
          "--enable-features=VaapiVideoDecoder",
          "--use-gl=desktop",
          "--disable-features=UseOzonePlatform",
          "--ignore-gpu-blocklist",
          "--enable-gpu-rasterization",
          "--enable-zero-copy",
          "--start-fullscreen"
        ],
        ignoreDefaultArgs: ["--enable-automation"],
        defaultViewport: null,
      })
      browser.on("disconnected", () => {
        debug("Browser Disconnected")
        this.runningBrowser = false

        // sleep screen if linux
        if (myOS === OSType.LINUX) {
          try {
            exec("xset -display :0 dpms force suspend")
          } catch (err) {
            debug("Exec Error: " + err)
          }
        }
      })

      const page = await browser.newPage()
      await page.goto(`http://127.0.0.1:${nextjsPort}/preview?close&overlay`)
      await page.exposeFunction("closePuppeteer", async () => {
        debug("Close Puppeteer called")
        await page.close()
        await browser.close()
      })
    }
  }
}