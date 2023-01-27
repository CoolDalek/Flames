package flames.concurrent.utils

import scala.util.NotGiven

infix type =!=[T, R] = NotGiven[T =:= R]
