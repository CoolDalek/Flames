package flames.actors.collections

trait ChunksPool[C[_], T] extends Pool[C[T]] {
  
  def chunkSize: Int
  
}
