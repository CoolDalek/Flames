package flames.concurrent.exception

abstract class NoStackTrace(message: String | Null = null) extends RuntimeException(message) {
  
  override def fillInStackTrace(): Throwable =
    setStackTrace(Array.empty); this
  
}
