package object Interpreter {

  import Syntax.Syntax._

  object Interpreter {

    sealed abstract class EvalExp
    case class EEInt(value: Int) extends EvalExp
    case class EEBool(value: Boolean) extends EvalExp
    case class EEChan(name: Name) extends EvalExp

    def runAct(act: Action): Action = act match {
      case Send(_, _, _) => throw new RuntimeException("Not yet implemented")
      case Receive(_, _, _) => throw new RuntimeException("Not yet implemented")
      case LetIn(name, exp, next) =>
        throw new RuntimeException("Not yet implemented")
      case IfThenElse(exp, tAct, fAct) =>
        throw new RuntimeException("Not yet implemented")
      case End => End
    }

    /** Substitute the Name 'to' for the Name 'from' within the Action act, and
     *  obtain the resulting Action.
     */
    def substituteAct(act: Action, from: Name, to: Name): Action = {
      val subA : Function[Action, Action] =
        a => substituteAct(a, from, to)
      val subE : Function[Expression, Expression] =
        e => substituteExp(e, from, to)
      act match {
        case Send(ch, msg, next) if ch == from =>
          Send(to, subE(msg), subA(next))

        case Send(ch, msg, next) if ch != from =>
          Send(ch, subE(msg), subA(next))

        case Receive(ch, bind, next) if ch == from && bind == from =>
          Receive(to, bind, next)

        case Receive(ch, bind, next) if ch == from && bind != from =>
          Receive(to, bind, subA(next))

        case Receive(ch, bind, next) if ch != from && bind == from =>
          act

        case Receive(ch, bind, next) if ch != from && bind != from =>
          Receive(ch, bind, subA(next))

        case LetIn(name, exp, next) if name == from =>
          LetIn(name, subE(exp), next)

        case LetIn(name, exp, next) if name != from =>
          LetIn(name, subE(exp), subA(next))

        case IfThenElse(exp, tAct, fAct) =>
          IfThenElse(subE(exp), subA(tAct), subA(fAct))

        case End =>
          End
      }
    }

    def substituteExp(exp: Expression, from: Name, to: Name): Expression =
      throw new RuntimeException("not implemented")

    def evalExp(exp: Expression): EvalExp = exp match {
      case Variable(n) =>
        throw new RuntimeException("Free variable error: '" + n + "'.")
      case IntLiteral(x) => EEInt(x)
      case BoolLiteral(x) => EEBool(x)
      case ChanLiteral(c) => EEChan(c)
      case Not(e) => evalExp(e) match {
        case EEBool(b) => EEBool(!b)
        case _ => throw new RuntimeException("Type error in negation")
      }
      case BinExp(ty, lhs, rhs) => (ty, evalExp(lhs), evalExp(rhs)) match {

        // Int -> Int -> Int
        case(Add, EEInt(l), EEInt(r)) => EEInt(l + r)
        case(Add, _, _) =>
          throw new RuntimeException("Type error in +")

        case(Sub, EEInt(l), EEInt(r)) => EEInt(l - r)
        case(Sub, _, _) =>
          throw new RuntimeException("Type error in -")

        case(Mul, EEInt(l), EEInt(r)) => EEInt(l * r)
        case(Mul, _, _) =>
          throw new RuntimeException("Type error in *")

        case(Div, EEInt(l), EEInt(r)) => EEInt(l / r)
        case(Div, _, _) =>
          throw new RuntimeException("Type error in /")

        case(Mod, EEInt(l), EEInt(r)) => EEInt(l % r)
        case(Mod, _, _) =>
          throw new RuntimeException("Type error in %")

        // A -> A -> Bool
        case(Equal, EEInt(l), EEInt(r)) => EEBool(l == r)
        case(Equal, EEBool(l), EEBool(r)) => EEBool(l == r)
        case(Equal, EEChan(l), EEChan(r)) => EEBool(l == r)
        case(Equal, _, _) =>
          throw new RuntimeException("Type error in ==")

        case(NotEqual, EEInt(l), EEInt(r)) => EEBool(l != r)
        case(NotEqual, EEBool(l), EEBool(r)) => EEBool(l != r)
        case(NotEqual, EEChan(l), EEChan(r)) => EEBool(l != r)
        case(NotEqual, _, _) =>
          throw new RuntimeException("Type error in !=")

        // Int -> Int -> Bool
        case(Less, EEInt(l), EEInt(r)) => EEBool(l < r)
        case(Less, _, _) =>
          throw new RuntimeException("Type error in <")

        case(LessEq, EEInt(l), EEInt(r)) => EEBool(l <= r)
        case(LessEq, _, _) =>
          throw new RuntimeException("Type error in <=")

        case(Greater, EEInt(l), EEInt(r)) => EEBool(l > r)
        case(Greater, _, _) =>
          throw new RuntimeException("Type error in >")

        case(GreaterEq, EEInt(l), EEInt(r)) => EEBool(l >= r)
        case(GreaterEq, _, _) =>
          throw new RuntimeException("Type error in >=")

        // Bool -> Bool -> Bool
        case(And, EEBool(l), EEBool(r)) => EEBool(l && r)
        case(And, _, _) =>
          throw new RuntimeException("Type error in >")

        case(Or, EEBool(l), EEBool(r)) => EEBool(l || r)
        case(Or, _, _) =>
          throw new RuntimeException("Type error in >=")
      }
    }
  }
}