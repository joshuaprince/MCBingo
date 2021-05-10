import Tippy from "@tippyjs/react"
import classNames from "classnames"

import { ISpace } from "interface/ISpace"
import React from "react"
import styles from "styles/Board.module.scss"

import "tippy.js/animations/shift-away.css"

type IProps = {
  obscured: boolean
  space: ISpace
  isPrimary: boolean
}

export const SpaceInner: React.FunctionComponent<IProps> = (props) => {
  const autoAStyle = props.space.auto && styles.goalAutoA
  const textSizeStyle = (props.space.text.length > 32) && styles.small

  const goalTooltipText = (props.isPrimary && !props.obscured) && props.space.tooltip

  return (
    <div className={styles.spaceInner}>
      {/* Goal tooltip */}
      {goalTooltipText &&
        <Tippy content={goalTooltipText}>
          <div className={styles.goalTooltip}>
            ?
          </div>
        </Tippy>
      }

      {/* Goal text */}
      <span className={classNames(styles.goalText, textSizeStyle)}>
        {props.space.text}
      </span>

      {/* Auto-activation indicator "A" */}
      {autoAStyle &&
        <Tippy content={"This space will be activated automatically."}>
          <div className={autoAStyle}>
            A
          </div>
        </Tippy>}
    </div>
  )
}
