import React from "react"

import { Navbar } from "./Navbar"

export const Layout: React.FC<React.PropsWithChildren> = (props) => {
  return (
    <>
      <Navbar/>
      {props.children}
    </>
  )
}
