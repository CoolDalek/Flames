package flames.concurrent.execution

trait AcquireRelease {

  def acquire(): Unit

  def release(): Unit
  
  final def scoped[T](action: => T): T = {
    acquire()
    val result = action
    release()
    result
  }

}
object AcquireRelease {

  trait Noop extends AcquireRelease{

    override def acquire(): Unit = ()

    override def release(): Unit = ()

  }
  
  val noop: AcquireRelease = new Noop {}

  trait Volatile extends AcquireRelease {
    //Let's just hope that scalac and JIT will not optimize this

    @volatile
    private var dummy = true

    private def consume[T](dummy: T): Unit = ()

    // Volatile read to sync memory after release
    override def acquire(): Unit =
      consume(dummy)

    //Volatile write to sync memory before acquire
    override def release(): Unit =
      dummy = false

  }
  
  def volatile: AcquireRelease = new Volatile {}
  
  def lockFree(model: ExecutionModel): AcquireRelease =
    model match {
      case ExecutionModel.Pinned =>
        noop
      case _ =>
        volatile
    }
  
  type Factory = ExecutionModel => AcquireRelease

}