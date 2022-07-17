package flames.concurrent.execution

trait Shutdown extends AutoCloseable {
  
  final override def close(): Unit = shutdown()
  
  def shutdown(): Unit

}