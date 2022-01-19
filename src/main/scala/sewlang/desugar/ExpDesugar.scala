package sewlang.desugar

import sexpr.ast._

object ExpDesugar {

  def apply(sexp: SExpr) = desugar(sexp)

  def desugar(sexp: SExpr): SExpr = sexp match {
    case SList(List(SSym("+"), exp1, exp2)) => SList(List(SSym("+"), desugar(exp1), desugar(exp2)))
    case SList(List(SSym("*"), exp1, exp2)) => SList(List(SSym("*"), desugar(exp1), desugar(exp2)))
    // #13 Implemente o desugar para as expressões (- exp1 exp2), (/ exp1 exp2) e (- exp)

    case SList(List(SSym("="), exp1, exp2)) => SList(List(SSym("="), desugar(exp1), desugar(exp2)))
    case SList(List(SSym("<"), exp1, exp2)) => SList(List(SSym("<"), desugar(exp1), desugar(exp2)))
    // #14 Implemente o desugar para as expressões (<= exp1 exp2), (> exp1 exp2) e (>= exp1 exp2)

    case SList(List(SSym("not"), exp)) => SList(List(SSym("not"), desugar(exp)))
    case SList(List(SSym("and"), exp1, exp2)) => SList(List(SSym("and"), desugar(exp1), desugar(exp2)))
    // #15 Implemente o desugar para a expressão (or exp1 exp2)

    case SList(List(SSym("var"), id, exp)) => SList(List(SSym("var"), desugar(id), desugar(exp)))
    case SList(List(SSym("set"), id, exp)) => SList(List(SSym("set"), desugar(id), desugar(exp)))
    case SList(List(SSym("if"), cond, thenExp, elseExp)) => SList(List(SSym("if"), desugar(cond), desugar(thenExp), desugar(elseExp)))
    case SList(List(SSym("while"), cond, doExp)) => SList(List(SSym("while"), desugar(cond), desugar(doExp)))
    case SList(SSym("begin") :: exps) => SList(SSym("begin") :: exps.map(desugar(_)))
    case SList(SSym("print") :: exps) => SList(SSym("print") :: exps.map(desugar(_)))

    case SList(List(SSym("++"), id)) => SList(List(SSym("set"), desugar(id), SList(List(SSym("+"), desugar(id), SNum(1))))) // (++ id) --> (set id (+ id 1))
    // #16 Implemente o desugar para a expressão (-- id)

    case SList(List(SSym("+="), id, exp)) => SList(List(SSym("set"), desugar(id), SList(List(SSym("+"), desugar(id), desugar(exp))))) // (+= id exp) --> (set id (+ id exp))
    // #17 Implemente o desugar para as expressões (*= id exp), (-= id exp) e (/= id exp)

    case SList(List(SSym("for"), init, cond, mod, body)) => desugarFor(init, cond, mod, body) // (for init cond mod body) --> (begin init (while cond (begin body mod)))

    case _ => sexp
  }

  private def desugarFor(init: SExpr, cond: SExpr, mod: SExpr, body: SExpr): SExpr = {
    SList(List(
      SSym("begin"),
      desugar(init),
      SList(List(SSym("while"), desugar(cond),
        SList(List(
          SSym("begin"),
          desugar(body),
          desugar(mod)))))))
  }

}