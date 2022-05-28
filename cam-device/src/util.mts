import { spawn } from "node:child_process"

export function sleep(timems = 1000) {
  return new Promise<void>((resolve) => {
    setTimeout(() => {
      resolve()
    }, timems)
  })
}

export async function exec(command: string) {
  return new Promise<string>((resolve, reject) => {
    const exec = spawn(command, {
      shell: true,
      stdio: "pipe",
    })
    let stdout = ""
    let stderr = ""
    exec.stdout.on("data", (data) => {
      stdout += data
    })
    exec.stderr.on("data", (data) => {
      stderr += data
    })
    exec.on("close", (code) => {
      if (code === 0) {
        resolve(stdout)
      } else {
        reject(stderr)
      }
    })
  })
}