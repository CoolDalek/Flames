package flames.actors.collections

object HashUtil {
  
  final def index[T, R](value: T, table: Array[R]): Int =
    val hash = value.##
    val improved = hash ^ (hash >>> 16)
    improved & (table.length - 1)
  end index

}
