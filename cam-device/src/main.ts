import got from "got-cjs"
import { spawn } from "child_process"
import os from "os"
import fs from "fs-extra"
import FormData from "form-data"
import { io, Socket } from "socket.io-client"
import { UserInfo } from "./user-info"

const mySerial = "12345"
const myHost = "http://192.168.3.95:3200"

async function takePhoto() {
  if (await fs.pathExists("./data/camera.jpg")) {
    await fs.remove("./data/camera.jpg")
  }
  if (os.type().indexOf("Linux") >= 0) {
    await new Promise((res, rej) => {
      const child = spawn("ffmpeg", ["-f", "v4l2", "-video_size", "1920x1080", "-input_format", "mjpeg", "-i", "/dev/video0", "-frames:v", "1", "data/camera.jpg", "-y"], {
        cwd: "./",
        stdio: "inherit",
      })
      child.on("error", rej)
      child.on("exit", res)
    })
  }
  if (!await fs.pathExists("./data/camera.jpg")) {
    throw new Error("Camera not found")
  }
  const image = await fs.readFile("./data/camera.jpg")
  return image
}

async function uploadPhoto(params: { host: string, token: string, image: Buffer }) {
  const { host, token, image } = params
  const uploadBody = new FormData()
  uploadBody.append("token", token)
  uploadBody.append("photo", image, { filename: "camera.jpg" })
  const uploadRes = await got(`${host}/photo`, {
    method: "PUT",
    throwHttpErrors: false,
    body: uploadBody,
  }).json()
  return uploadRes
}

async function connectToken(host: string, serial: string) {
  const tokenBody = new FormData()
  tokenBody.append("serial", serial)
  tokenBody.append("isCamera", "true")
  const tokenRes: { data: { token: string }, isError: boolean } = await got(`${host}/token`, {
    method: "PUT",
    throwHttpErrors: false,
    body: tokenBody
  }).json()
  if (tokenRes.isError) {
    console.error(JSON.stringify(tokenRes, null, 2))
    throw new Error("Server error")
  }
  const token = tokenRes.data.token
  return token
}

type SocketHandshakeRes = { token: string, serial: string }
async function connectSocket(host: string, token: string) {
  const socket = io(host, {
    query: {
      token,
    },
    transports: ["websocket"],
  })
  const response = await new Promise<SocketHandshakeRes>((res, rej) => {
    const timeout = setTimeout(() => rej("Timeout"), 10000)
    socket.once("error", (err) => {
      console.error("Error received: " + err)
      clearTimeout(timeout)
      throw new Error(err)
    })
    socket.once("connected", (r: SocketHandshakeRes) => {
      clearTimeout(timeout)
      res(r)
    })
  })
  socket.on("takePhoto", async (user: UserInfo) => {
    console.log("TakePhoto request with " + user)
    if (user.serial === mySerial) {
      const image = await takePhoto()
      const uploadRes = await uploadPhoto({ host, token, image })
      console.log(uploadRes)
    }
  })
  return {
    socket,
    response,
  }
}

async function main() {
  const token = await connectToken(myHost, mySerial)
  const { socket, response } = await connectSocket(myHost, token)
  console.log("Socket Response: " + response)
}

main()

// ffmpeg -f v4l2 -video_size 1920x1080 -input_format mjpeg -i /dev/video0 -frames:v 1 test3.jpg -y