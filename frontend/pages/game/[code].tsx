import Head from "next/head"
import { useRouter } from "next/router"
import { BingoGame } from "../../components/game/BingoGame"

// noinspection JSUnusedGlobalSymbols
export default function Game() {
  const router = useRouter()
  const gameCode = router.query.code?.toString() || ""
  const playerName = router.query.name?.toString() || undefined

  const title = `MultiBingo : ${gameCode}`

  return (
    <>
      <Head>
        <title>{title}</title>
      </Head>
      <BingoGame gameCode={gameCode} playerName={playerName}/>
    </>
  )
}
