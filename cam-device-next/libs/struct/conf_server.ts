/**
 * Scope: Server Only
 */

import { myOS, OSType } from "./ostype"

export const cameraName = myOS === OSType.LINUX ? "v4l2:///dev/video0" : "USB Camera" // Whatever VTubeStudioCam