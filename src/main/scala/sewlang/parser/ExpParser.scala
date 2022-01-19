package sewlang.parser

import sewlang.ast._
import sewlang.exception.ExpParserException
import sexpr.ast._
import sewlang.desugar.ExpDesugar

object ExpParser {

  def apply(sexp: SExpr) = parse(sexp)

  def parse(sexp: SExpr): Exp = sexp match {
    case SNum(n) => NumberExp(n) // n
    case SString(s) => StringExp(s) // "string"
    case SSym("nil") => NilExp // nil
    case SSym("true") => TrueExp // true
    case SSym("false") => FalseExp // false
    case SSym(s) if isIdentifier(s) => IdExp(s) // id

    case SList(List(SSym("+"), exp1, exp2)) => SumExp(parse(exp1), parse(exp2)) // (+ exp1 exp2)
    case SList(List(SSym("*"), exp1, exp2)) => MultExp(parse(exp1), parse(exp2)) // (* exp1 exp2)
    // #6 Implemente o parser para as expressões (- exp1 exp2), (/ exp1 exp2) e (- exp)

    case SList(List(SSym("="), exp1, exp2)) => EqualExp(parse(exp1), parse(exp2)) // (= exp1 exp2)
    case SList(List(SSym("<"), exp1, exp2)) => LessThanExp(parse(exp1), parse(exp2)) // (< exp1 exp2)
    // #7 Implemente o parser para as expressões (<= exp1 exp2), (> exp1 exp2) e (>= exp1 exp2)

    case SList(List(SSym("not"), exp)) => NotExp(parse(exp)) // (not exp)
    case SList(List(SSym("and"), exp1, exp2)) => AndExp(parse(exp1), parse(exp2)) // (and exp1 exp2)
    // #8 Implemente o parser para a expressão (or exp1 exp2)

    case SList(List(SSym("var"), id, exp2)) => VarDeclExp(parse(id), parse(exp2)) // (var id exp)
    case SList(List(SSym("set"), id, exp2)) => VarAssignExp(parse(id), parse(exp2)) // (set id exp)

    case SList(List(SSym("if"), cond, thenExp, elseExp)) => IfExp(parse(cond), parse(thenExp), parse(elseExp)) // (if cond then-exp else-exp)

    case SList(List(SSym("while"), cond, doExp)) => WhileExp(parse(cond), parse(doExp)) // (while cond do-exp)

    case SList(SSym("begin") :: exps) => BlockExp(exps.map(parse(_))) // (begin exp*)

    case SList(SSym("print") :: exps) => PrintExp(exps.map(parse(_))) // (print exp*)

    case SList(List(SSym("read-num"))) => ReadNumExp // (read-num)
    // #9 Implemente o parser para as expressões (read-bool) e (read-str)

    case _ => throw ExpParserException(s"error in the expression '$sexp'")
  }

  /*
 * #5 Implemente a verificação de palavras reservadas de
 * modo que não seja possível utilizar uma palavra-chave
 * como 'while', por exemplo, como um identificador.
 */

  private def isIdentifier(str: String) = {
    val regex = """^[a-zA-Z][a-zA-Z0-9_]*$""".r
    regex.pattern.matcher(str).matches()
  }

}