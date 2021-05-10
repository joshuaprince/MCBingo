import { sendRevealBoard } from "api"
import React from "react"

export const RevealButton: React.FunctionComponent = () => {
  return (
    <div className="reveal-button" onClick={sendRevealBoard}>
      Reveal Board
    </div>
  )
}
