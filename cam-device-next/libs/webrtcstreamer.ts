const handleHTTPErrors = (response: Response) => {
  if (!response.ok) {
    throw Error(response.statusText)
  }
  return response
}

type IceURLs = {urls: string[]}
type IceResponse = {iceServers:IceURLs[]}

/**
 * WebRTCStreamer helper class
 * 
 * Original: https://github.com/mpromonet/webrtc-streamer-html/blob/99813c6159de9c4e26918b3df8ea14f8b5fe06e1/webrtcstreamer.js
 */
export default class WebRTCStreamer {
  protected videoElement:HTMLVideoElement
  protected serverUrl:string
  protected iceServers:IceURLs[] = []
  protected peerId:string = ""
  protected earlyCandidates:RTCIceCandidate[] = []
  protected mediaConstraints:RTCOfferOptions = {offerToReceiveVideo: true}
  protected peerConnection:RTCPeerConnection | null = null
  protected currentStream:MediaStream | null = null

  public constructor(videoElement:HTMLVideoElement, srvurl:string) {
    this.videoElement = videoElement
    this.serverUrl = srvurl
  }
  public async getMediaList() {
    const resp:Array<{video:string}> = await (await fetch(`${this.serverUrl}/api/getMediaList`)).json()
    return resp.map((v) => v.video)
  }
  public async connect(cameraURL:string, options:Partial<{width:number, height:number, audioUrl:string}> = {}) {
    // Check ice server endpoint
    if (this.iceServers.length <= 0) {
      this.iceServers.push(...(await this.getIceServers()).iceServers)
    }
    // Create peer connection
    const createPeerConnection = async () => {
      const peerConnection = new RTCPeerConnection({
        iceServers: this.iceServers,
      })
      this.peerId = Math.random().toString()
  
      peerConnection.addEventListener("icecandidate", (event) => {
        if (event.candidate != null) {
          if (peerConnection.currentRemoteDescription != null) {
            this.addIceCandidate(event.candidate)
          } else {
            this.earlyCandidates.push(event.candidate)
          }
        }
      }, false)
  
      peerConnection.addEventListener("track", (event) => {
        this.currentStream = event.streams[0]
        this.videoElement.srcObject = event.streams[0]
        // this.videoElement.play()
      }, false)
  
      peerConnection.addEventListener("iceconnectionstatechange", (event) => {
        switch (peerConnection.iceConnectionState) {
          case "connected":
            break
          case "disconnected":
            break
          case "failed":
          case "closed":
            break
          case "new":
            this.getIceCandidate(peerConnection)
            break
          default:
            break
        }
      }, false)
  
      peerConnection.addEventListener("datachannel", (event) => {
        console.log("remote datachannel created:" + JSON.stringify(event))
        const channel = event.channel
        channel.addEventListener("open", (evt) => {
          console.log("remote datachannel open")
          channel.send("remote channel openned")
        })
        channel.addEventListener("message", (evt) => {
          console.log("remote datachannel recv:" + JSON.stringify(evt.data))
        })
      })
  
      peerConnection.addEventListener("icegatheringstatechange", (event) => {
        if (peerConnection.iceGatheringState === "complete") {
          const recvs = peerConnection.getReceivers()
  
          for (const recv of recvs) {
            const track = recv.track
            if (track != null) {
              console.log("codecs:" + JSON.stringify(recv.getParameters().codecs))
            }
          }
        }
      })
  
      // create local data channel
      try {
        const channel = peerConnection.createDataChannel("ClientDataChannel", {
          ordered: true,
        })
        channel.addEventListener("open", (evt) => {
          console.log("data channel open")
          channel.send("local channel openned")
        })
        channel.addEventListener("message", (evt) => {
          console.log("data channel recv:" + JSON.stringify(evt.data))
        })
      } catch (err) {
        console.log(`createDataChannel ERROR ${JSON.stringify(err)}`)
      }
  
      console.log("Created RTCPeerConnnection.")
      return peerConnection
    }

    const peerConnection = await createPeerConnection()
    
    let apiUrl = `${this.serverUrl}/api/call?peerid=${this.peerId}&url=${encodeURIComponent(cameraURL)}`

    if (options.audioUrl != null) {
      apiUrl += `&audioUrl=${encodeURIComponent(options.audioUrl)}`
    }
    let optionQuery = ""
    if (options.width != null) {
      optionQuery += `&width=${options.width}`
    }
    if (options.height != null) {
      optionQuery += `&height=${options.height}`
    }
    if (optionQuery.length >= 1) {
      apiUrl += `&options=${encodeURIComponent(optionQuery.substring(1))}`
    }
    
    // clear early candidates
    this.earlyCandidates = []

    // Create offer
    const sessionDescription = await peerConnection.createOffer(this.mediaConstraints)

    console.log("Create offer:" + JSON.stringify(sessionDescription))

    // Set local description
    await peerConnection.setLocalDescription(sessionDescription)

    const apiResp:RTCSessionDescriptionInit = await (await fetch(apiUrl, {
      method: "POST",
      body: JSON.stringify(sessionDescription),
    })).json()

    console.log("offer: " + JSON.stringify(apiResp))

    const remoteDescription = new RTCSessionDescription(apiResp)

    await peerConnection.setRemoteDescription(remoteDescription)

    while (this.earlyCandidates.length > 0) {
      const candidate = this.earlyCandidates.shift()
      await peerConnection.addIceCandidate(candidate)
    }

    await this.getIceCandidate(peerConnection)

    this.peerConnection = peerConnection

    return peerConnection
  }

  public async disconnect(peerConnection?:RTCPeerConnection) {
    this.videoElement.src = ""
    const pc = peerConnection ?? this.peerConnection
    if (pc != null) {
      await fetch(`${this.serverUrl}/api/hangup?peerid=${this.peerId}`)
      try {
        pc.close()
      } catch (err) {
        console.log("Failure close peer connection:" + err);
      }
      this.peerConnection = null
    }
  }

  public updateMediaComponent(element:HTMLVideoElement) {
    this.videoElement = element
    if (!(this.currentStream == null)) {
      element.srcObject = this.currentStream
    } 
  }

  protected async addIceCandidate(candidate:RTCIceCandidate) {
    const resp = await fetch(`${this.serverUrl}/api/addIceCandidate?peerid=${this.peerId}`, {
      method: "POST",
      body: JSON.stringify(candidate),
    })
    return resp.ok
  }

  protected async getIceServers() {
    const response = await fetch(`${this.serverUrl}/api/getIceServers`)
    return handleHTTPErrors(response).json() as Promise<IceResponse>
  }

  protected async getIceCandidate(peerConnection:RTCPeerConnection) {
    try {
      const resp:Array<Partial<RTCIceCandidate>> | null = await (await fetch(`${this.serverUrl}/api/getIceCandidate?peerid=${this.peerId}`)).json()
      if (resp == null) {
        throw new Error("No ice candidate")
      }
      for (const candidate of resp) {
        const iceCandidate = new RTCIceCandidate(candidate)
        try {
          await peerConnection.addIceCandidate(iceCandidate)
          console.log("addIceCandidate OK")
        } catch (err) {
          console.log(`addIceCandidate ERROR ${JSON.stringify(err)}`)
        }
      }
      await peerConnection.addIceCandidate()
    } catch (err) {
      throw err
    }
  }
}