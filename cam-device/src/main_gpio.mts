/**
 * Run as root (pigpio only)
 */

import { myOS, OSType } from "./struct/ostype.mjs"
import Debug from "debug"
import { GPIORecv } from "./feature/gpiorecv.mjs"
import { wsPort } from "./struct/conf.mjs"

const debug = Debug("molloo:main_gpio")

async function runGpio() {
  // GPIO
  if (myOS === OSType.LINUX) {
    debug(`Starting GPIO Receiver`)
    const gpioRecv = new GPIORecv(`ws://127.0.0.1:${wsPort}`)
    await gpioRecv.connect()
    gpioRecv.register()
  } else {
    debug(`GPIO - not supported OS! Aborting!`)
  }
}

runGpio()