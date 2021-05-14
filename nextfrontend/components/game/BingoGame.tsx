import React, { useCallback, useEffect } from 'react'
import useWebSocket, { ReadyState } from "react-use-websocket"
import { getWebSocketUrl, onApiMessage, updateWebSocket } from "../../api"
import { BoardShape, IBoard } from "../../interface/IBoard"
import { IGameMessage } from "../../interface/IGameMessage"
import { IPlayerBoard } from "../../interface/IPlayerBoard"

import { BoardContainer } from "./BoardContainer"
import { LoadingSpinner } from "./LoadingSpinner"
import { RevealButton } from "./RevealButton"
import { SecondaryBoardsContainer } from "./SecondaryBoardsContainer"
import { TapModeContext, TapModeSelector } from "./TapModeSelector"

type IProps = {
  gameCode: string
  playerName?: string
}

export type IBingoGameState = {
  board: IBoard
  playerBoards: IPlayerBoard[]
  messages: IGameMessage[]
  connecting: boolean
  tapToMark: boolean
}

export const BingoGame: React.FunctionComponent<IProps> = (props: IProps) => {
  const [state, setState] = React.useState<IBingoGameState>(getInitialState)

  const socketUrl = useCallback(() => getWebSocketUrl(props.gameCode, props.playerName),
    [props.gameCode, props.playerName])
  const {
    lastJsonMessage,
    getWebSocket,
    readyState,
  } = useWebSocket(socketUrl, {
    onOpen: () => console.log('Websocket opened: ' + getWebSocket()?.url),
    shouldReconnect: () => true,
  })

  /* Update `connecting` state entry and API's websocket */
  useEffect(() => {
    setState(s => ({...s, connecting: (readyState !== ReadyState.OPEN)}))
    updateWebSocket(getWebSocket())
  }, [getWebSocket, readyState])

  /* React to incoming messages */
  useEffect(() => {
    onApiMessage(setState, lastJsonMessage)
  }, [lastJsonMessage])

  const primaryPlayer = state.playerBoards.find(pb => pb.player_name === props.playerName)
  const secondaryPlayers = state.playerBoards.filter(pb => pb.player_name !== props.playerName)

  return (
    <TapModeContext.Provider
      value={{
        tapToMark: state.tapToMark,
        set: (v: boolean) => setState(s => ({...s, tapToMark: v}))
      }}
    >
      <div className={"bingo-app " + (state.board.obscured ? "obscured" : "revealed")}>
        {primaryPlayer &&
          <TapModeSelector/>
        }
        <BoardContainer isPrimary={true} board={state.board} playerBoard={primaryPlayer}/>
        <SecondaryBoardsContainer board={state.board} playerBoards={secondaryPlayers}/>
        {state.connecting && <LoadingSpinner/>}
        {state.board.obscured && <RevealButton/>}
        {/*<ChatBox messages={state.messages}/>*/}
      </div>
    </TapModeContext.Provider>
  )
}

const getInitialState: (() => IBingoGameState) = () => {
  const board: IBoard = {
    obscured: true,
    shape: BoardShape.SQUARE,
    spaces: [],
  }

  return {
    board: board,
    connecting: true,
    playerBoards: [],
    messages: [],
    tapToMark: false,
  }
}
