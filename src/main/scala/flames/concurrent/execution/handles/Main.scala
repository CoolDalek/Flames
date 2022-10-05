package flames.concurrent.execution.handles

import java.lang.invoke.{MethodHandles, VarHandle as JVarHandle}
object Main {

  private val nameField = VarHandle[Test, String](_.Name)
  private val nameHandle: VarGet[String] = nameField.handle

  private val ageField = VarHandle[Test, Int](_.age)
  private val ageHandle: VarHandle[Int] = ageField.handle

  private val friendsField = VarHandle[Test, java.util.List[String]](_.friends)
  private val friendsHandle: VarHandle[java.util.List[String]] = friendsField.handle

  private val timestampField = VarHandle[Test, Long](_.timestamp)
  private val timestampHandle: VarHandle[Long] = timestampField.handle

  def main(args: Array[String]): Unit =
    println("Name")
    listAccess(nameHandle)
    println("Age")
    listAccess(ageHandle)
    println("Friends")
    listAccess(friendsHandle)
    println("Timestamp")
    listAccess(timestampHandle)
  end main

  def listAccess[T, Handle[x] <: VarHandle.Tag[x]](handle: Handle[T]): Unit =
    val jvh = handle.asInstanceOf[JVarHandle]
    JVarHandle.AccessMode.values().foreach { x =>
      val support = jvh.isAccessModeSupported(x)
      println(s"$x, $support")
    }
  end listAccess

}
