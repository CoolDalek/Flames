package flames.ui

trait SyncScreen extends Screen {

  final override def render(delta:  Float): Unit = {
    view(delta)
    update(delta)
  }

  def update(delta: Float): Unit

  def view(delta: Float): Unit

}