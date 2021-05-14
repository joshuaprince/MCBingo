import { FormLabel, Switch } from "@chakra-ui/react"
import React from "react"

import styles from "styles/Game.module.scss"

type ITapModeContext = {
  tapToMark: boolean
  set: (val: boolean) => void
}

export const TapModeContext = React.createContext<ITapModeContext>({
  tapToMark: false,
  set: () => {}
})

export const TapModeSelector: React.FC = () => {
  const {tapToMark, set} = React.useContext(TapModeContext)
  return (
    <div className={styles.tapModeSelector}>
      <FormLabel htmlFor="tap-mode-selector">
        Tap to Strategize
      </FormLabel>
      <Switch
        id="tap-mode-selector"
        checked={tapToMark}
        onChange={e => set(e.target.checked)}
      />
      <FormLabel htmlFor="tap-mode-selector">
        Tap to Complete
      </FormLabel>
    </div>
  )
}
