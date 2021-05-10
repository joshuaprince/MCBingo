import { Container, Heading } from "@chakra-ui/react"

import styles from "../styles/about.module.scss"

export default function About() {
  return (
    <Container className={styles.container}>
      <Heading size="lg">MultiBingo</Heading>
    </Container>
  )
}
