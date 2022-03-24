package flames.util

trait SummonerK[Effect[_[_]]] {
  
  inline final def apply[F[_]: Effect]: Effect[F] = summon[Effect[F]]

}