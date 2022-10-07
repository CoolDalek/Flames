package flames.concurrent.execution.handles

import java.lang.invoke.{MethodHandles, VarHandle as JVarHandle}
object Example {

  private val nameHandle: VarGet[Test, String, "Name"] = VarHandle[Test, String](_.Name)

  def main(args: Array[String]): Unit =
    val instance = new Test
    inline given Magnet[Test, String, "Name"] = nameHandle.magnetize(instance)
    val result = nameHandle.getPlain
    println(result)
  end main

}
