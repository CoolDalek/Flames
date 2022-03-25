package flames.ui

trait UIListener extends AutoCloseable {
  
  def resize(width: Int, height: Int): Unit
  
  def render(delta: Float): Unit
  
  def pause(): Unit
  
  def resume(): Unit

}