package flames.concurrent.execution

trait TimeStealer {
  
  def steal(): Boolean

  def idleThreshold: Int

}
