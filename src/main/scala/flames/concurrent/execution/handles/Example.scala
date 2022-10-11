package flames.concurrent.execution.handles

import java.lang.invoke.{MethodHandles, VarHandle as JVarHandle}
object Example {

  private val ageHandle = VarHandle[Test](_.age)

  def main(args: Array[String]): Unit =
    JVarHandle.AccessMode.values()
      .foreach { mode =>
        println(s"$mode -> ${ageHandle.asInstanceOf[JVarHandle].isAccessModeSupported(mode)}")
      }
  end main

}
