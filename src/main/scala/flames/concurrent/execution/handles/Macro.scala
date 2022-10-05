package flames.concurrent.execution.handles

import scala.compiletime.*
import scala.quoted.*
import scala.annotation.tailrec

object Macro:
  
  transparent inline def varHandleMacro[R, T](
                                               inline getter: R => T,
                                               ofClass: Class[R],
                                               selfClass: Class[T],
                                             ): VarHandle.Apply[T, R] =
    ${varHandleMacroImpl[R, T]('getter, 'ofClass, 'selfClass)}

  def varHandleMacroImpl[R: Type, T: Type](
                                            getter: Expr[R => T],
                                            ofClass: Expr[Class[R]],
                                            selfClass: Expr[Class[T]]
                                          )(using
                                           Quotes,
                                           Type[VarHandle],
                                           Type[VarGet],
                                          ): Expr[VarHandle.Apply[T, R]] =
    import quotes.reflect.*
    import java.lang.invoke.{MethodHandles, VarHandle as JVarHandle}

    @tailrec
    def findField(tree: Tree): Symbol =
      tree match
        case term: Ident => term.symbol
        case term: Select => term.symbol
        case Block(List(stmt), _) => findField(stmt)
        case DefDef("$anonfun", _, _, Some(term)) => findField(term)
        case Block(_, term) => findField(term)
        case Apply(term, _) if term.symbol.fullName != "<special-ops>.throw" => findField(term)
        case TypeApply(term, _) => findField(term)
        case Inlined(_, _, term) => findField(term)
        case Typed(term, _) => findField(term)
        case _ => throw new IllegalArgumentException(s"Unsupported expression: ${tree.show}")
    end findField

    val field = findField(getter.asTerm)
    val fieldName = field.name
    val fieldOfClass = TypeRepr.of[R].classSymbol
      .exists(
        _.fieldMember(
          fieldName
        ).exists
      )
    if(fieldOfClass)
      val isVariable = field.flags is Flags.Mutable
      val isConstant = field.flags is Flags.JavaStatic
      val make = if(isConstant) '{
        MethodHandles.lookup()
          .findStaticVarHandle(
            $ofClass,
            ${Expr(fieldName)},
            $selfClass,
          )
      } else '{
        MethodHandles.lookup()
          .findVarHandle(
            $ofClass,
            ${Expr(fieldName)},
            $selfClass,
          )
      }
      if(isVariable) '{
        new Field[T, R] {

          override type Handle[x] = VarHandle[x]

          override val handle: VarHandle[T] =
            VarHandle.unsafeCoerce[T, VarHandle]($make)

          override val of: Class[R] = $ofClass

          override val self: Class[T] = $selfClass

          override val name: String = ${Expr(fieldName)}

          override val isStatic: Boolean = ${Expr(isConstant)}

          override val isMutable: Boolean = ${Expr(isVariable)}

        }
      } else '{
        new Field[T, R] {

          override type Handle[x] = VarGet[x]

          override val handle: VarGet[T] =
            VarHandle.unsafeCoerce[T, VarGet]($make)

          override val of: Class[R] = $ofClass

          override val self: Class[T] = $selfClass

          override val name: String = ${Expr(fieldName)}

          override val isStatic: Boolean = ${Expr(isConstant)}

          override val isMutable: Boolean = ${Expr(isVariable)}

        }
      }
    else throw IllegalArgumentException(s"${field.name} is not a field of ${TypeRepr.of[R].show}")
  end varHandleMacroImpl

  inline def findField[T, R](name: String): Option[Boolean] =
    ${findFieldImpl[T, R]('name)}

  def findFieldImpl[T: Type, R: Type](namePattern: Expr[String])
                                     (using Quotes): Expr[Option[Boolean]] =
    import quotes.reflect.*
    TypeRepr.of[R].classSymbol.map { of =>
      val fields = of.fieldMembers.collect {
        case field if field.asInstanceOf[ValDef].tpt.tpe =:= TypeRepr.of[T] =>
          field.name -> false
        case field if field.asInstanceOf[ValDef].tpt.tpe <:< TypeRepr.of[T] =>
          field.name -> true
      }
      '{
        ${Expr(fields)}.find { (name, _) =>
          name == $namePattern
        }.map { (_, subtype) =>
          subtype
        }
      }
    }.getOrElse(Expr(None))
  end findFieldImpl

end Macro
