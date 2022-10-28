package flames.concurrent.execution.handles

import scala.compiletime.*
import scala.quoted.*
import scala.annotation.tailrec

object Macro:
  
  transparent inline def vhMacro[C, V](
                                        inline getter: C => V,
                                        inline ofClass: Class[C],
                                        inline selfClass: Class[V],
                                      ): Any =
    ${vhMacroImpl[C, V]('getter, 'ofClass, 'selfClass)}

  def vhMacroImpl[C: Type, V: Type](
                                     getter: Expr[C => V],
                                     ofClass: Expr[Class[C]],
                                     selfClass: Expr[Class[V]]
                                   )(using
                                     Quotes,
                                     Type[VarHandle],
                                     Type[VarGet],
                                   ): Expr[Any] =
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
    val fieldOfClass = TypeRepr.of[C].classSymbol
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
      val vhType = Applied(
        if (isVariable) TypeTree.of[VarHandle]
        else TypeTree.of[VarGet],
        List(
          TypeTree.of[C],
          TypeTree.of[V],
          Singleton(Literal(StringConstant(fieldName))),
        ),
      )
      vhType.tpe.asType match {
        case '[t] => '{${make}.asInstanceOf[t]}
      }
    else throw IllegalArgumentException(s"$fieldName is not a field of ${TypeRepr.of[C].show}")
  end vhMacroImpl
/*
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
*/
end Macro
