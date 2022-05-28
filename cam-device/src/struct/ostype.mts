export enum OSType {
  UNKNOWN,
  WINDOWS,
  MAC,
  LINUX,
  ANDROID,
}

export const myOS = getOSType()
export const is64Bit = process.arch.indexOf("64") >= 0
export const isARM = process.arch.indexOf("arm") >= 0

function getOSType() {
  const osStr = process.platform
  switch (osStr) {
    case "win32":
      return OSType.WINDOWS
    case "linux":
      return OSType.LINUX
    case "darwin":
      return OSType.MAC
    case "android":
      return OSType.ANDROID
    case "cygwin":
      return OSType.LINUX
    default:
      return OSType.UNKNOWN
  }
}