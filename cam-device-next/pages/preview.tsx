import React, { useEffect, useRef, useState, useCallback } from 'react'
import styled from 'styled-components';

import { ToastContainer, toast } from "react-toastify"
import "react-toastify/dist/ReactToastify.css"

import Image from "next/image"
import { useRouter } from "next/router"
import useWebSocket, { ReadyState } from "react-use-websocket"
import { Camera, CameraType } from "../components/camera"
import { nextjsPort, streamingPort, wsPort } from '../libs/struct/conf'
import styles from "../styles/Preview.module.css"
import type { GetStaticProps } from "next"
import { myOS, OSType } from "../libs/struct/ostype"

const Wrapper = styled.div`
  position: fixed;
  width: 100%;
  height: 100%;
  z-index: 1;
`;

const Control = styled.div`
  position: fixed;
  display: flex;
  right: 0;
  width: 20%;
  min-width: 130px;
  min-height: 130px;
  height: 100%;
  background: rgba(0, 0, 0, 0.8);
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30px;
  box-sizing: border-box;
  flex-direction: column-reverse;

  @media (max-aspect-ratio: 1/1) {
    flex-direction: row;
    bottom: 0;
    width: 100%;
    height: 20%;
  }

  @media (max-width: 400px) {
    padding: 10px;
  }
`;

const Button = styled.button`
  outline: none;
  color: white;
  opacity: 1;
  background: transparent;
  background-color: transparent;
  background-position-x: 0%;
  background-position-y: 0%;
  background-repeat: repeat;
  background-image: none;
  padding: 0;
  text-shadow: 0px 0px 4px black;
  background-position: center center;
  background-repeat: no-repeat;
  pointer-events: auto;
  cursor: pointer;
  z-index: 2;
  filter: invert(100%);
  border: none;

  &:hover {
    opacity: 0.7;
  }
`;

const TakePhotoButton = styled(Button)`
  background: url('https://img.icons8.com/ios/50/000000/compact-camera.png');
  background-position: center;
  background-size: 50px;
  background-repeat: no-repeat;
  width: 80px;
  height: 80px;
  border: solid 4px black;
  border-radius: 50%;
  display: none;

  &:hover {
    background-color: rgba(0, 0, 0, 0.3);
  }
`;

const ChangeFacingCameraButton = styled(Button)`
  background: url(https://img.icons8.com/ios/50/000000/switch-camera.png);
  background-position: center;
  background-size: 40px;
  background-repeat: no-repeat;
  width: 40px;
  height: 40px;
  padding: 40px;
  &:disabled {
    opacity: 0;
    cursor: default;
    padding: 60px;
  }
  @media (max-width: 400px) {
    padding: 40px 5px;
    &:disabled {
      padding: 40px 25px;
    }
  }
`;

const ImagePreview = styled.div<{ image: string | null }>`
  width: 120px;
  height: 120px;
  ${({ image }) => (image ? `background-image:  url(${image});` : '')}
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;

  @media (max-width: 400px) {
    width: 50px;
    height: 120px;
  }
`;

const FullScreenImagePreview = styled.div<{ image: string | null }>`
  width: 100%;
  height: 100%;
  z-index: 1;
  position: absolute;
  background-color: black;
  ${({ image }) => (image ? `background-image:  url(${image});` : '')}
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
`;

type PropType = {
  streamServer: string,
  sCameraName: string,
  useLocalCam: boolean,
}

export const getStaticProps:GetStaticProps = async (context) => {
  const getJson = (url: string) => {
    return fetch(url).then(res => res.json())
  }
  const srvUrl = `http://127.0.0.1:${nextjsPort}`
  const cameraName = (await getJson(`${srvUrl}/local/api/cameraName`)).name as string | null ?? ""
  const cameraPort = (await getJson(`${srvUrl}/local/api/cameraPort`)).port as number | null ?? -1
  const useWebRTC = (await getJson(`${srvUrl}/local/api/usewebrtc`)).usewebrtc as boolean | null ?? false

  const prop:PropType = {
    streamServer: `http://0.0.0.0:${cameraPort}`,
    sCameraName: cameraName,
    useLocalCam: !useWebRTC,
  }
  return {
    props: prop
  }
}

const App = (props:PropType) => {

  const [numberOfCameras, setNumberOfCameras] = useState(0);
  const [image, setImage] = useState<string | null>(null);
  const [showImage, _setShowImage] = useState<boolean>(false);
  const camera = useRef<CameraType>(null);
  const [streamServer, setStreamServer] = useState<string>(props.
    streamServer)
  const [socketUrl, setSocketUrl] = useState(`ws://0.0.0.0:${wsPort}`)

  const [isInited, setIsInited] = useState(false)
  const [guideLevel, setGuideLevel] = useState(0)
  const [sentCount, setSentCount] = useState(0)

  // External
  const router = useRouter()
  const { sendMessage, lastMessage, readyState } = useWebSocket(socketUrl)
  const useClose = router.query.close != null
  const useOverlay = router.query.overlay != null




  const setShowImage = (show:boolean, useHistory = false) => {
    _setShowImage(show)
  }

  const onPositiveBtnClick = async (ev:Event | null) => {
    if (showImage) {
      const sendImage = async () => {
        /*
          const sleep = (ms:number) => new Promise(resolve => setTimeout(resolve, ms))
          await sleep(1000)
          sendMessage("World!!!")
        */
        if (image == null) {
          console.log("Image is null! Aborting!")
          throw new Error("No Image Found.")
        }

        const formdata = new FormData()
        formdata.append("photo", await (await fetch(image)).blob())
        const postPhoto = await fetch("/local/api/photo", {
          method: "POST",
          body: formdata,
        })
        const postData:unknown = await postPhoto.json()
        console.log("Uploaded Photo: " + JSON.stringify(postData))
        setSentCount(sentCount + 1)
        return postData
      }
      await toast.promise(sendImage, {
        pending: "???? ????????? ????????? ????????????.",
        success: `???? ???????????? ${sentCount + 1}?????? ????????? ???????????????.`,
        error: "????????? ????????? ?????? ????????? ??????????????????.",
      })
      if (guideLevel <= 2) {
        toast.info("????????? ?????? ????????? ????????? ?????? ??? ????????? ????????? ??? ????????????. ????????? ???????????????. ????")
        setGuideLevel(3)
      }
      setShowImage(false)
    } else {
      if (camera.current) {
        const photo = camera.current.takePhoto()
        // console.log(photo)
        setImage(photo)
        if (guideLevel <= 1) {
          setGuideLevel(2)
          toast.info(`?????? ??????! ?????? ${useClose ? "??????????" : "?????????"}  ????????? ?????? ????????? ?????? ??? ????????????.`)
          toast.error(`????????? ?????? ???????????? ${useClose ? "?????????? ????????? ??????" : "????????? ????????? ?????????"} ???????????????? ?????? ?????? ??? ?????????.`)
        } else {
          toast.info("?????? ??????!", {autoClose: 2000})
        }
        setShowImage(true, true)
      }
    }
  }

  const onNegativeBtnClick = async (ev:Event | null) => {
    if (showImage) {
      setShowImage(false)
    } else {
      try {
        let wany = window as any
        // puppeteer close
        if (wany.closePuppeteer != null) {
          wany.closePuppeteer()
        } else {
          window.close()
        }
        setTimeout(() => {
          toast.warn("?????? ?????? ??? ?????????. ???????????? ?????? ???????????????.")
        }, 500)
      } catch (err) {
        console.error(err)
      }
    }
  }

  // Client side only init
  if (typeof window !== "undefined") {
    if (streamServer.indexOf("0.0.0.0") >= 0) {
      setStreamServer(streamServer.replace("0.0.0.0", location.hostname))
    }
    if (socketUrl.indexOf("0.0.0.0") >= 0) {
      setSocketUrl(socketUrl.replace("0.0.0.0", location.hostname))
    }
  }

  useEffect(() => {
    if (!router.isReady) {
      return
    }
    const firstInit = (window as any).firstinit === true
    if (firstInit) {
      return
    }
    (window as any).firstinit = false
    setIsInited(true)
    console.log("Query: " + JSON.stringify(router.query))
    // Singleton
    toast.info(`${useClose ? "??????????" : "??????"} ????????? ?????? ???????????????????.`)
    if (useClose) {
      toast.error("?????????? ????????? ?????? ??????????????????.")
    }
    setGuideLevel(guideLevel + 1)
  }, [router.isReady])

  useEffect(() => {
    if (lastMessage !== null) {
      const decodeMessage = async () => {
        let data:Blob | string = lastMessage.data
        let dataStr:string = ""
        if (typeof data === "string") {
          dataStr = data
        } else {
          dataStr = await data.text()
        }
        // use JSON if starts with { and ends with }
        if (dataStr.startsWith("{") && dataStr.endsWith("}")) {
          // Object-scope
          const obj:{[key in string]:unknown} = JSON.parse(dataStr)
          switch (obj.command) {
            case "pressPositiveBtn":
              onPositiveBtnClick(null)
              break
            case "pressNegativeBtn":
              onNegativeBtnClick(null)
              break
            default:
              console.log("Unknown command: " + obj.command)
          }
        } else {
          // String-scope
          console.log("Unknown message: " + dataStr)
        }
      }
      decodeMessage()
    }
  }, [lastMessage])

  return (
    <Wrapper>
      {showImage ? (
        <FullScreenImagePreview
          image={image}
          onClick={() => {
            setShowImage(!showImage, true);
          }}
        />
      ) : (
        <Camera
          ref={camera}
          streamServerUrl={props.useLocalCam ? "" : streamServer}
          cameraDeviceName={props.sCameraName}
          aspectRatio="cover"
          numberOfCamerasCallback={setNumberOfCameras}
          errorMessages={{
            noCameraAccessible: 'No camera device accessible. Please connect your camera or try a different browser.',
            permissionDenied: 'Permission denied. Please refresh and give camera permission.',
            switchCamera:
              'It is not possible to switch camera to different one because there is only one video device accessible.',
            canvas: 'Canvas is not supported.',
          }}
        />
      )}
      {(useOverlay && !showImage) ? (
        <div className={styles.overlay_container}>
          <Image
            src="/images/camera_overlay.svg"
            className={styles.overlay_image}
            layout="fill"
          />
        </div>
      ) : null}
      <Control>
        <ImagePreview
          image={image}
          onClick={() => {
            setShowImage(!showImage, true);
          }}
        />
        {useClose ? (
          <img
            src={showImage ? "/images/cancel_btn.svg" : "/images/close_btn.svg"}
            className={styles.camera_btn}
            onClick={(ev) => onNegativeBtnClick(null)}
          />
        ) : null}
        <img
          src={showImage ? "/images/send_btn.svg" : "/images/camera_btn.svg"}
          className={styles.camera_btn}
          onClick={(ev) => onPositiveBtnClick(null)}
          />
        <TakePhotoButton
          onClick={() => {
            // ?????? ?????? ??????!!
            if (camera.current) {
              const photo = camera.current.takePhoto()
              console.log(photo)
              setImage(photo)
            }
          }}
        />
        <ChangeFacingCameraButton
          disabled={numberOfCameras <= 1 || showImage}
          onClick={() => {
            if (camera.current) {
              const result = camera.current.switchCamera();
              console.log(result);
            }
          }}
        />
      </Control>
      <ToastContainer
        position="bottom-center"
        autoClose={5000}
        newestOnTop={false}
      />
    </Wrapper>
  );
};

export default App;