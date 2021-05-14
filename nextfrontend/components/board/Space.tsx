import Tippy from "@tippyjs/react"

import { sendMarkBoard } from "api"
import classNames from "classnames"

import { ColorPickerTooltip } from "components/board/ColorPickerTooltip"
import { SpaceInner } from "components/board/SpaceInner"
import { BoardShape } from "interface/IBoard"
import { Color, IPlayerBoardMarking } from "interface/IPlayerBoard"
import { IPosition } from "interface/IPosition"
import { ISpace } from "interface/ISpace"
import React from "react"
import styles from "styles/Board.module.scss"

import "tippy.js/animations/shift-away.css"
import { TapModeContext } from "../game/TapModeSelector"

type IProps = {
  obscured: boolean
  editable: boolean
  space: ISpace
  shape: BoardShape
  marking?: IPlayerBoardMarking
  winning: boolean
  isPrimary: boolean
  isVertical: boolean
}

export const Space: React.FunctionComponent<IProps> = (props: IProps) => {
  const {tapToMark} = React.useContext(TapModeContext)
  const onMouseDown = (e: React.MouseEvent) => {
    e.preventDefault()

    if (!props.editable) {
      return false
    }

    const isRightClick = e.button === 2;
    if (isRightClick || !tapToMark) {
      /* Covert mark */
      sendMarkBoard({
        space_id: props.space.space_id,
        covert_marked: !props.marking?.covert_marked
      })
    } else {
      sendMarkBoard({
        space_id: props.space.space_id,
        color: nextColor(props.marking?.color),
      })
    }

    return false
  }

  const onContextMenu = (e: React.MouseEvent) => {
    e.preventDefault()
  }

  const wholeSpaceTooltip = (!props.isPrimary && !props.obscured) && props.space.text

  const markColorStyle = styles["mark-" + props.marking?.color || Color.UNMARKED]
  const covertMarkedStyle = props.marking?.covert_marked && styles.covertMarked
  const winningStyle = props.winning && styles.winning
  const editable = props.editable && styles.editable

  const spaceDiv = (
    <div
      className={classNames(styles.space, markColorStyle, covertMarkedStyle, winningStyle, editable)}
      style={calculateGridPosition(props.space.position, props.shape, props.isVertical)}
      onMouseDown={onMouseDown}
      onContextMenu={onContextMenu}
    >
      <SpaceInner
        obscured={props.obscured}
        space={props.space}
        isPrimary={props.isPrimary}
      />
    </div>
  )

  if (wholeSpaceTooltip) {
    return (<Tippy delay={0} interactive={false} content={wholeSpaceTooltip}>{spaceDiv}</Tippy>)
  } else {
    const tooltipHtml = <ColorPickerTooltip spaceId={props.space.space_id}/>;
    return <Tippy disabled={!props.editable} interactive delay={[500, 300]} animation={'shift-away'}
                  content={tooltipHtml}>{spaceDiv}</Tippy>;
  }
}

const calculateGridPosition = (pos: IPosition, shape: BoardShape, vertical: boolean): React.CSSProperties => {
  switch (shape) {
    case BoardShape.SQUARE:
      return {gridColumn: pos.x + 1, gridRow: pos.y + 1}
    case BoardShape.HEXAGON:
      const colStart = (pos.x * 2) + (pos.y % 2) + (pos.y >> 1) * 2 + 1
      const rowStart = (pos.y * 3) + 1

      let err = false
      if (colStart <= 0 || rowStart <= 0) {
        console.error(`Hex position (${pos.x}, ${pos.y}) calculated invalid grid position (${colStart}, ${rowStart})`)
        err = true
      }

      if (!vertical) {
        return {
          background: err ? "red" : undefined,
          gridColumnStart: colStart,
          gridColumnEnd: "span 2",
          gridRowStart: rowStart,
          gridRowEnd: "span 4"
        }
      } else {
        return {
          background: err ? "red" : undefined,
          gridColumnStart: rowStart,
          gridColumnEnd: "span 4",
          gridRowStart: colStart,
          gridRowEnd: "span 2"
        }
      }
  }
}

const nextColor = (col?: Color) => {
  switch (col) {
    case Color.UNMARKED:
      return Color.COMPLETE
    case Color.COMPLETE:
      return Color.UNMARKED
    case Color.REVERTED:
      return Color.COMPLETE
    case Color.INVALIDATED:
      return Color.NOT_INVALIDATED
    case Color.NOT_INVALIDATED:
      return Color.INVALIDATED
  }

  return Color.UNMARKED
}
