import { WSServer } from "./wsserver.mjs"
import { Gpio } from "pigpio"
import { blueBtnPin, redBtnPin } from "../struct/conf.mjs"
import Debug from "debug"

const debug = Debug("molloo:gpiorecv")

export class GPIORecv {
  protected lastTimeMap: Map<number, number> = new Map()
  protected gpios: Map<number, Gpio> = new Map()
  protected wsServer: WSServer
  public constructor(wsServer: WSServer) {
    this.wsServer = wsServer
  }
  public register() {
    this.addOnPress(redBtnPin, () => {
      debug(`Red Button Pressed`)
      this.wsServer.sendCommandToAll("pressNegativeBtn")
    })
    this.addOnPress(blueBtnPin, () => {
      debug(`Blue Button Pressed`)
      this.wsServer.sendCommandToAll("pressPositiveBtn")
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