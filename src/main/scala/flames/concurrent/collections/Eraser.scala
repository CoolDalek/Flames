package flames.concurrent.collections

private[collections] type ErasedCallback = Iterator[Any] => Unit
private[collections] type Fix[T] = Any
private[collections] type ErasedCollection = Fix[Any]
private[collections] type ErasedSplitter = Splitter[Fix]
private[collections] type ErasedParallel = Parallel[Nothing, Any]
private[collections] type Splitted = Iterator[Iterator[Any]]
private[collections] type Continuation = Splitted => ErasedParallel

transparent trait Eraser {

  inline private[collections] def eraseCallback[T](callback: Iterator[T] => Unit): ErasedCallback =
    callback.asInstanceOf[ErasedCallback]
  inline private[collections] def eraseParallel[T, R](parallel: Parallel[T, R]): ErasedParallel =
    parallel.asInstanceOf[ErasedParallel]
  inline private[collections] def eraseCollection[C[_], T](collection: C[T]): ErasedCollection =
    collection.asInstanceOf[ErasedCollection]
  inline private[collections] def eraseSplitter[C[_]](splitter: Splitter[C]): ErasedSplitter =
    splitter.asInstanceOf[ErasedSplitter]
  inline private[collections] def eraseSplitted[T](splitted: Iterator[Iterator[T]]): Splitted =
    splitted.asInstanceOf[Splitted]

}