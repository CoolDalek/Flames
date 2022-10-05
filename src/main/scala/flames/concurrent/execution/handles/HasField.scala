package flames.concurrent.execution.handles

trait HasField[Var, Self <: HasField[Var, Self]](using Field[Var, Self]) {
  this: Self =>
  
  inline protected given Field.Scoped[Var] = Field.inScope[Var, Self](this)

}
