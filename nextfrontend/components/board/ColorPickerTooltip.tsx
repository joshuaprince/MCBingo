import React from "react"

import styles from "styles/Board.module.scss"
import { $enum } from "ts-enum-util"
import { sendMarkBoard } from "../../api"
import { Color } from "../../interface/IPlayerBoard"

type IProps = {
  spaceId: number
}

export const ColorPickerTooltip: React.FunctionComponent<IProps> = (props: IProps) => {
  const onMouseDown = (e: React.MouseEvent, newMarking: number) => {
    e.preventDefault()
    sendMarkBoard({
      space_id: props.spaceId,
      color: newMarking,
    })
    return false
  }

  return (
    <div className={styles.colorPickerTooltip}>
      {$enum(Color).map(color => (
        <div
          key={color}
          className={styles["mark-" + color]}
          onMouseDown={(e) => onMouseDown(e, color)}
        />
      ))}
    </div>
  )
}
