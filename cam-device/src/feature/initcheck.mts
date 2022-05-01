import { myOS, is64Bit, OSType, isARM } from "../struct/ostype.mjs"
import { spawn } from "node:child_process"
import Path from "node:path"
import fsp from "node:fs/promises"
import fs from "node:fs"
import fse from "fs-extra"
import Debug from "debug"
import chalk from "chalk"
import got from "got"
import decompress from "decompress"
import { streamingPort } from "../struct/conf.mjs"
import { sleep, exec } from "../util.mjs"

const debug = Debug("molloo:initcheck")
const rootDIR = Path.resolve(".")

/**
 * 깔려 있어야 하는 프로그램들을 미리 확인한다.
 * @returns 깔린 여부
 */
export async function checkProgram() {
  if (myOS === OSType.UNKNOWN) {
    console.error(chalk.red(`OS is not supported.`))
    return false
  }
  if (myOS === OSType.WINDOWS) {
    console.log(chalk.yellow(`Windows is only support for development.`))
    return true
  }

  let exists = true
  const v4l2ctlPath = await exec("which v4l2-ctl")
  if (v4l2ctlPath.indexOf("not found") >= 0) {
    console.error(chalk.red(`v4l2-ctl not found. Please install v4l2-utils.`))
    console.log(chalk.yellow(`sudo apt install v4l2-utils`))
    exists = false
  }
  return exists
}

/**
 * WebRTC-Streamer가 있는지 확인하고 실행한다.
 * @returns 성공 여부
 */
export async function checkStreamer() {
  // 1. Check WebRTC-Streamer
  const rtcServerDIR = Path.resolve(rootDIR, "webrtc-streamer")
  debug(`OS: ${process.platform} (${myOS})`)
  console.log(chalk.yellow(`Checking WebRTC-Streamer exists...`))
  await fse.ensureDir(rtcServerDIR)
  // check exists
  try {
    if (myOS === OSType.WINDOWS) {
      await fsp.access(Path.resolve(rtcServerDIR, "webrtc-streamer.exe"), fs.constants.X_OK)
    } else if (myOS === OSType.LINUX) {
      await fsp.access(Path.resolve(rtcServerDIR, "webrtc-streamer"), fs.constants.X_OK)
    } else {
      throw new Error("Not supported OS")
    }
  } catch (err) {
    if (myOS !== OSType.WINDOWS && myOS !== OSType.LINUX) {
      return false
    }
    // Download webrtc-streamer
    console.log([
      chalk.red(`WebRTC-Streamer is not found.`),
      chalk.green(`Downloading v0.6.5 from github...`),
      chalk.yellow(`Source: https://github.com/mpromonet/webrtc-streamer`)
    ].join("\n"))
    let platformStr: string = ""
    if (myOS === OSType.WINDOWS) {
      platformStr = "Windows-AMD64"
    } else {
      // Linux
      if (isARM) {
        platformStr = is64Bit ? "Linux-arm64" : "Linux-armv7l"
      } else {
        platformStr = "Linux-x86_64"
      }
    }
    const url = `https://github.com/mpromonet/webrtc-streamer/releases/download/v0.6.5/webrtc-streamer-v0.6.5-${platformStr}-Release.tar.gz`
    const execTar = await got(url, { responseType: "buffer" })
    const tarPath = Path.resolve(rtcServerDIR, "webrtc-streamer.tar.gz")
    await fsp.writeFile(tarPath, execTar.body)
    // extract tar.gz
    await decompress(tarPath, rtcServerDIR)
    // get directory name
    const dirName = (await fsp.readdir(rtcServerDIR)).filter((v) => v.endsWith("Release"))[0]
    // move to parent directory
    for (const file of await fsp.readdir(Path.resolve(rtcServerDIR, dirName))) {
      await fsp.rename(Path.resolve(rtcServerDIR, dirName, file), Path.resolve(rtcServerDIR, file))
    }
    // remove tar.gz
    await fsp.unlink(tarPath)
    // remove extracted directory
    await fsp.rmdir(Path.resolve(rtcServerDIR, dirName))
    console.log(chalk.green(`Downloaded and extracted WebRTC-Streamer v0.6.5.`))
  }
  debug("WebRTC-Streamer is prepared.")
  // 2. Check Streaming Server is aliving
  console.log(chalk.yellow("Checking Streaming Server is aliving"))
  debug(`Port: ${streamingPort}`)
  try {
    await got(`http://127.0.0.1:${streamingPort}/`, {
      timeout: {
        request: 1000,
      },
    })
    console.log(chalk.green(`Streaming Server is already running.`))
  } catch (err) {
    // set resolution
    if (myOS === OSType.LINUX) {
      await exec(`v4l2-ctl -d /dev/video0 --set-fmt-video=width=${1920},height=${1080}`)
      await exec(`v4l2-ctl -d /dev/video0 --set-parm=${24}`)
    }

    // start server
    let execName = myOS === OSType.WINDOWS ? "webrtc-streamer.exe" : "webrtc-streamer"
    console.log(chalk.red(`Streaming Server is not found.`))
    console.log(chalk.green(`Starting Streaming Server...`))
    const server = new StreamerDaemon(rtcServerDIR, execName)
    server.start()
    console.log(chalk.green(`Streaming Server is started. Waiting while server is booting...`))
    await sleep(3000)
    try {
      await got(`http://127.0.0.1:${streamingPort}/`)
    } catch (err) {
      console.log(chalk.red(`Streaming Server failed to start. Aborting.`))
      return false
    }
    console.log(chalk.green(`Streaming Server is started.`))
  }
  return true
}

export class StreamerDaemon {
  protected dirPath: string
  protected execName: string
  protected execPath: string
  constructor(dirPath: string, execName: string) {
    this.dirPath = dirPath
    this.execName = execName
    this.execPath = Path.resolve(this.dirPath, this.execName)
  }
  public start() {
    let args: string[] = [`-H`, streamingPort.toString()]
    if (myOS === OSType.LINUX) {
      args.push("-o")
    }
    debug(`Exec ${this.execPath} ${args.join(" ")}`)

    const exec = spawn(this.execPath, args, {
      cwd: Path.resolve(this.dirPath),
      detached: true,
      stdio: "ignore",
    })
    exec.unref()
  }
}