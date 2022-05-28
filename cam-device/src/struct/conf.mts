import { myOS, OSType } from "./ostype.mjs"

export const streamingPort = 7720
export const expressPort = 7721
export const nextjsPort = 7722
export const wsPort = 7723

export const redBtnPin = 22
export const blueBtnPin = 27
export const camLedPin = 23

const mainServerHostname = "192.168.3.95"
export const mainServerHost = `http://${mainServerHostname}:3200`
export const mainSIOHost = `ws://${mainServerHostname}:3201`

export const useWebRTC = false // Performance Issue

export const gpioProxyTag = `___###molloo:internal:gpio-proxy###___`

export const cameraName = myOS === OSType.LINUX ? "v4l2:///dev/video0" : "USB Camera" // Whatever VTubeStudioCam

export const githubURL = "https://github.com/craftingmod/molloo/blob/master/cam-device/README.md"