package flames.actors.utils

trait SummonerK[Effect[F[_]]] {

  inline def apply[F[_]: Effect]: Effect[F] = summon[Effect[F]]

}
