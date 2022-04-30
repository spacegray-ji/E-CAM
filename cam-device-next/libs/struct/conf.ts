/**
 * Scope: Server-Only
 */

import { myOS, OSType } from "./ostype"

export const streamingPort = 7720
export const expressPort = 7721

export const cameraName = myOS === OSType.LINUX ? "v4l2:///dev/video0" : "USB Camera" // Whatever VTubeStudioCam

export const githubURL = "https://github.com/craftingmod/molloo/blob/master/cam-device/README.md"