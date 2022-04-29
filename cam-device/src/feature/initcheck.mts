import { myOS, OSType } from "../struct/ostype.mjs"
import { spawn } from "child_process"
import Path from "path"
import fsp from "fs/promises"
import fs from "fs"
import Debug from "debug"
import chalk from "chalk"

const debug = Debug("molloo:initcheck")
const rootDIR = Path.resolve(".")

export async function checkDirectory() {
  // 1. Check WebRTC-Streamer
  const rtcServerDIR = Path.resolve(rootDIR, "webrtc-streamer")
  debug(`OS: ${process.platform} (${myOS})`)
  debug("Checking WebRTC-Streamer directory...")
  // check Execution
  try {
    if (myOS === OSType.WINDOWS) {
      await fsp.access(Path.resolve(rtcServerDIR, "webrtc-streamer.exe"), fs.constants.X_OK)
    } else if (myOS === OSType.LINUX) {
      await fsp.access(Path.resolve(rtcServerDIR, "webrtc-streamer"), fs.constants.X_OK)
    } else {
      console.error()
      throw new Error("Not supported OS")
    }
  } catch (err) {

  }
  if (myOS === OSType.WINDOWS) {

  } else if (myOS === OSType.LINUX) {

  }
}