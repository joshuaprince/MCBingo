import React, { useCallback, useEffect } from 'react'
import { useMediaQuery } from "react-responsive"
import useWebSocket, { ReadyState } from "react-use-websocket"
import { getWebSocketUrl, onApiMessage, updateWebSocket } from "../../api"
import { BoardShape, IBoard } from "../../interface/IBoard"
import { IGameMessage } from "../../interface/IGameMessage"
import { IPlayerBoard } from "../../interface/IPlayerBoard"

import { BoardContainer } from "./BoardContainer"
import { LoadingSpinner } from "./LoadingSpinner"
import { ResponsiveContext } from './ResponsiveContext'
import { RevealButton } from "./RevealButton"
import { SecondaryBoardsContainer } from "./SecondaryBoardsContainer"
import { TapModeSelector } from "./TapModeSelector"

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
  const isTapOnly = useMediaQuery({
    query: "(hover: none)"
  })
  const [state, setState] = React.useState<IBingoGameState>(() => getInitialState(isTapOnly))

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
    <ResponsiveContext.Provider
      value={{
        isTapOnly: isTapOnly,
        tapToMark: state.tapToMark,
        setTapToMark: (v: boolean) => setState(s => ({...s, tapToMark: v}))
      }}
    >
      <div className={"bingo-app " + (state.board.obscured ? "obscured" : "revealed")}>
        {isTapOnly && primaryPlayer &&
          <TapModeSelector/>
        }
        <BoardContainer isPrimary={true} board={state.board} playerBoard={primaryPlayer}/>
        <SecondaryBoardsContainer board={state.board} playerBoards={secondaryPlayers}/>
        {state.connecting && <LoadingSpinner/>}
        {state.board.obscured && <RevealButton/>}
        {/*<ChatBox messages={state.messages}/>*/}
      </div>
    </ResponsiveContext.Provider>
  )
}

const getInitialState = (isTapOnly: boolean): IBingoGameState => {
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
    tapToMark: !isTapOnly,
  }
}
