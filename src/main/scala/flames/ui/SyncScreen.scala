package flames.ui

trait SyncScreen extends Screen {

  override final def render(delta:  Float): Unit = {
    view(delta)
    update(delta)
  }

  def update(delta: Float): Unit

  def view(delta: Float): Unit

}