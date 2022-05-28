'use strict';

let webRtcServer = null

async function fetchGet(url) {
  return (await fetch(url)).json()
}

window.addEventListener("load", async (ev) => {
  /**
   * @type {number}
   */
  const streamPort = (await fetchGet("/api/cameraPort")).port
  console.log(`Stream port: ${streamPort}`)
  /**
   * @type {string}
   */
  const cameraName = (await fetchGet("/api/cameraName")).name
  console.log(`Camera name: ${cameraName}`)

  const videoElement = document.getElementById("preview_content")
  videoElement.addEventListener("loadedmetadata", (ev) => {
    console.log("Loaded metadata!")
    // videoElement.play()
  }, false)
  videoElement.addEventListener("loadeddata", (ev) => {
    console.log("Video data loaded!")
  }, false)


  webRtcServer = new WebRtcStreamer("preview_content", `${location.protocol}//${location.hostname}:${streamPort}`)
  webRtcServer.connect(cameraName, null, `width=${1920},height=${1080}`)
})
window.addEventListener("beforeunload", (ev) => {
  webRtcServer.disconnect()
})