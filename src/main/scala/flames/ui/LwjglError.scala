package flames.ui

case class LwjglError(
                       error: String,
                       description: String,
                     ) extends RuntimeException(s"$error:$description")