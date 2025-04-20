package flames.actors.utils

trait SummonerK[Effect[F[_]]] {

  inline def apply[F[_]](using ev: Effect[F]): ev.type = ev

}
