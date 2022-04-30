import React, { useEffect, useRef, useState } from 'react';
import styled from 'styled-components';

import { ToastContainer, toast } from "react-toastify"
import "react-toastify/dist/ReactToastify.css"

import Image from "next/image"
import { useRouter } from "next/router"
import { Camera, CameraType } from "../components/camera"
import { cameraName, streamingPort } from '../libs/struct/conf'
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
}

export const getStaticProps:GetStaticProps = async (context) => {
  const prop:PropType = {
    streamServer: `http://127.0.0.1:${streamingPort}`,
  }
  return {
    props: prop
  }
}

const App = (props:PropType) => {
  const router = useRouter()
  const useClose = router.query.close != null
  const useOverlay = router.query.overlay != null

  const [numberOfCameras, setNumberOfCameras] = useState(0);
  const [image, setImage] = useState<string | null>(null);
  const [showImage, _setShowImage] = useState<boolean>(false);
  const camera = useRef<CameraType>(null);
  const [streamServer, setStreamServer] = useState<string>(props.
    streamServer)

  const [isInited, setIsInited] = useState(false)
  const [guideLevel, setGuideLevel] = useState(0)

  const setShowImage = (show:boolean, useHistory = false) => {
    _setShowImage(show)
  }

  const onPositiveBtnClick = async (ev:Event | null) => {
    if (showImage) {
      const sendImage = async () => {
        const sleep = (ms:number) => new Promise(resolve => setTimeout(resolve, ms))
        await sleep(1000)
        const test = await fetch("/api/hello")
        return test
      }
      await toast.promise(sendImage, {
        pending: "사진을 보내는 중입니다.",
        success: "사진을 보냈습니다.",
        error: "사진을 보내는 중에 오류가 발생했습니다.",
      })
      if (guideLevel <= 2) {
        toast.info("사진을 보낸 후에는 원하는 만큼 더 찍거나 종료할 수 있습니다. 마음껏 찍어보세요. 😊")
        setGuideLevel(3)
      }
      setShowImage(false)
    } else {
      if (camera.current) {
        const photo = camera.current.takePhoto()
        console.log(photo)
        setImage(photo)
        if (guideLevel <= 1) {
          setGuideLevel(2)
          toast.info("촬영 성공! 이제 파란색 버튼을 눌러 사진을 보낼 수 있습니다.")
          toast.error("마음에 들지 않는다면 빨간 버튼을 눌러 취소하고 다시 찍을 수 있어요.")
        }
        setShowImage(true, true)
      }
    }
  }

  const onNegativeBtnClick = async (ev:Event | null) => {
    if (showImage) {
      setShowImage(false)
    } else {
      window.close()
    }
  }

  useEffect(() => {
    const firstInit = (window as any).firstinit === true
    if (firstInit) {
      return
    }
    (window as any).firstinit = true
    setIsInited(true)
    // Singleton
    toast.info("하늘색 버튼을 눌러 촬영하세요.")
    toast.error("빨간색 버튼을 눌러 종료하세요.")
    setGuideLevel(guideLevel + 1)
  }, [])

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
          streamServerUrl={streamServer}
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
            // 여기 코드 아님!!
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