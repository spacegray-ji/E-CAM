import React, { useState, useCallback, useEffect } from 'react';
import useWebSocket, { ReadyState } from 'react-use-websocket';

export default function WSTest() {
  //Public API that will echo messages sent to it back to the client
  const [socketUrl, setSocketUrl] = useState('wss://echo.websocket.org');
  const [messageHistory, setMessageHistory] = useState<unknown[]>([]);

  const { sendMessage, lastMessage, readyState } = useWebSocket(socketUrl);

  useEffect(() => {
    if (lastMessage !== null) {
      setMessageHistory((prev) => prev.concat(lastMessage))
      const decodeMessage = async () => {
        const data:Blob | string = lastMessage.data
        if (typeof data === "string") {
          console.log("Message: " + lastMessage.data)
        } else {
          console.log("Message: " + (await lastMessage.data.text()))
        }
      }
      decodeMessage()
    }
  }, [lastMessage, setMessageHistory]);

  const handleClickChangeSocketUrl = useCallback(
    () => setSocketUrl('ws://127.0.0.1:7723/'),
    []
  );

  const handleClickSendMessage = useCallback(() => sendMessage('Hello'), []);

  const connectionStatus = {
    [ReadyState.CONNECTING]: 'Connecting',
    [ReadyState.OPEN]: 'Open',
    [ReadyState.CLOSING]: 'Closing',
    [ReadyState.CLOSED]: 'Closed',
    [ReadyState.UNINSTANTIATED]: 'Uninstantiated',
  }[readyState];

  return (
    <div>
      <button onClick={handleClickChangeSocketUrl}>
        Click Me to change Socket Url
      </button>
      <button
        onClick={() => {
          console.log(sendMessage)
          sendMessage("Hello" + Math.floor(Math.random() * 20000).toString())
        }}
        disabled={readyState !== ReadyState.OPEN}
      >
        Click Me to send 'Hello'
      </button>
      <span>The WebSocket is currently {connectionStatus}</span>
      {null}
      <ul>
        {null}
      </ul>
    </div>
  );
};