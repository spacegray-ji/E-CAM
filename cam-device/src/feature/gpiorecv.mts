import { WSServer } from "./wsserver.mjs"
import { Gpio } from "pigpio"
import { blueBtnPin, camLedPin, gpioProxyTag, redBtnPin } from "../struct/conf.mjs"
import Debug from "debug"
import WebSocket from "ws"

const debug = Debug("molloo:gpiorecv")

export class GPIORecv {
  protected lastTimeMap: Map<number, number> = new Map()
  protected gpios: Map<number, Gpio> = new Map()
  protected sockAddr: string
  protected isConnected = false
  protected webSocket: WebSocket | null = null
  public constructor(wsaddr: string) {
    this.sockAddr = wsaddr
  }
  public async connect() {
    const ws = await new Promise<WebSocket>((res, rej) => {
      const timeout = setTimeout(() => rej("Timeout"), 5000)
      const ws = new WebSocket(this.sockAddr)
      const rejFn = (err: unknown) => {
        rej(err)
      }
      ws.once("open", () => {
        debug("Opened socket!")
        ws.off("error", rejFn)
        clearTimeout(timeout)
        ws.send(gpioProxyTag)
        res(ws)
      })
      ws.once("error", rejFn)
    })
    ws.on("message", (data) => {
      let dataStr: string = ""
      if (typeof data === "string") {
        dataStr = data
      } else {
        // Buffer
        dataStr = data.toString("utf8")
      }
      if (dataStr.startsWith("{") && dataStr.endsWith("}")) {
        const obj = JSON.parse(dataStr)
        // gpio Command
        switch (obj.command) {
          case "turnOnLed":
            this.setBoolState(camLedPin, true)
            break
          case "turnOffLed":
            this.setBoolState(camLedPin, false)
            break
          default:
            debug(`Unknown command ${obj.command}`)
        }
      }
    })
    this.webSocket = ws
  }
  public register() {
    this.addOnPress(redBtnPin, () => {
      debug(`Red Button Pressed`)
      this.webSocket?.send(JSON.stringify({ command: "pressNegativeBtn" }))
    })
    this.addOnPress(blueBtnPin, () => {
      debug(`Blue Button Pressed`)
      this.webSocket?.send(JSON.stringify({ command: "pressPositiveBtn" }))
    })
  }
  /**
   * 버튼 형식 추가
   * @param pin 핀 번호 
   * @param callback 콜백
   * @param minInterval 최소 클릭 간격 (ms)
   */
  public addOnPress(pin: number, callback: () => unknown, minInterval = 500) {
    const gpio = new Gpio(pin, {
      mode: Gpio.INPUT,
      pullUpDown: Gpio.PUD_DOWN,
      alert: true,
      edge: Gpio.FALLING_EDGE,
    })
    gpio.on("interrupt", async (level) => {
      console.log(`Button ${pin} Pressed with ${level}`)
      const lastTime = this.lastTimeMap.get(pin) ?? 0
      if (Date.now() - lastTime >= minInterval) {
        this.lastTimeMap.set(pin, Date.now())
        callback()
      }
    })
    this.gpios.set(pin, gpio)
  }
  public addOutToggle(pin: number) {
    const gpio = new Gpio(pin, {
      mode: Gpio.OUTPUT
    })
    this.gpios.set(pin, gpio)
  }
  public setBoolState(pin: number, state: boolean) {
    const gpio = this.gpios.get(pin)
    if (gpio != null) {
      gpio.digitalWrite(state ? 1 : 0)
    }
  }
}