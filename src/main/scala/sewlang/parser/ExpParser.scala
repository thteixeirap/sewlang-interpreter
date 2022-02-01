package sewlang.parser

import sewlang.ast._
import sewlang.exception.ExpParserException
import sexpr.ast._

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
    
    // #5 Implemente o parser para as expressões (- exp1 exp2), (/ exp1 exp2) e (- exp)
    case SList(List(SSym("-"), exp1, exp2)) => SubExp(parse(exp1), parse(exp2)) // NOVO (- exp1 exp2)
    case SList(List(SSym("/"), exp1, exp2)) => DiviExp(parse(exp1), parse(exp2)) // NOVO  (/ exp1 exp2)
    case SList(List(SSym("-"), exp)) => NegExp(parse(exp)) // NOVO (- exp)
    //-----------------------------------------------------------------------------------------------------

    case SList(List(SSym("="), exp1, exp2)) => EqualExp(parse(exp1), parse(exp2)) // (= exp1 exp2)
    case SList(List(SSym("<"), exp1, exp2)) => LessThanExp(parse(exp1), parse(exp2)) // (< exp1 exp2)
    
    // #6 Implemente o parser para as expressões (<= exp1 exp2), (> exp1 exp2) e (>= exp1 exp2)
    case SList(List(SSym("<="), exp1, exp2)) => LessEqualThanExp(parse(exp1), parse(exp2)) // NOVO (<= exp1 exp2)
    case SList(List(SSym(">"), exp1, exp2)) => BiggerThanExp(parse(exp1), parse(exp2)) // NOVO (> exp1 exp2)
    case SList(List(SSym(">="), exp1, exp2)) => BiggerEqualThanExp(parse(exp1), parse(exp2)) // NOVO (>= exp1 exp2)
    //------------------------------------------------------------------------------------------------------

    case SList(List(SSym("not"), exp)) => NotExp(parse(exp)) // (not exp)
    case SList(List(SSym("and"), exp1, exp2)) => AndExp(parse(exp1), parse(exp2)) // (and exp1 exp2)
    
    // #7 Implemente o parser para a expressão (or exp1 exp2)
    case SList(List(SSym("or"), exp1, exp2)) => OrExp(parse(exp1), parse(exp2)) // NOVO (or exp1 exp2)
    //-------------------------------------------------------------------------------------------------------

    case SList(List(SSym("var"), id, exp2)) => VarDeclExp(parse(id), parse(exp2)) // (var id exp)
    case SList(List(SSym("set"), id, exp2)) => VarAssignExp(parse(id), parse(exp2)) // (set id exp)

    case SList(List(SSym("if"), cond, thenExp, elseExp)) => IfExp(parse(cond), parse(thenExp), parse(elseExp)) // (if cond then-exp else-exp)

    case SList(List(SSym("while"), cond, doExp)) => WhileExp(parse(cond), parse(doExp)) // (while cond do-exp)

    case SList(SSym("begin") :: exps) => BlockExp(exps.map(parse(_))) // (begin exp*)

    case SList(SSym("print") :: exps) => PrintExp(exps.map(parse(_))) // (print exp*)

    case SList(List(SSym("read-num"))) => ReadNumExp // (read-num)
    
    // #8 Implemente o parser para as expressões (read-bool) e (read-str)
    case SList(List(SSym("read-bool"))) => ReadBoolExp // NOVO (read-bool)
    case SList(List(SSym("read-str"))) => ReadStrExp // NOVO (read-str)
    //--------------------------------------------------------------------------------------------------------


    // As tarefas devem ser implementadas seguindo a ordem de numeração, logo, as três tarefas abaixo só devem ser implementadas após a #13.
    case SList(List(SSym("++"), id)) => parse(ExpDesugar.desugar(sexp))
    
    // #14 Implemente o desugar para a expressão (-- id)
    case SList(List(SSym("--"), id)) => parse(ExpDesugar.desugar(sexp)) //  NOVO (-- id)
    //-----------------------------------------------------------------------------------

    case SList(List(SSym("+="), id, exp)) => parse(ExpDesugar.desugar(sexp))
    
    // #15 Implemente o desugar para as expressões (*= id exp), (-= id exp) e (/= id exp)
    case SList(List(SSym("*="), id, exp)) => parse(ExpDesugar.desugar(sexp)) //  NOVO (*= id exp)
    case SList(List(SSym("-="), id, exp)) => parse(ExpDesugar.desugar(sexp)) //  NOVO (-= id exp)
    case SList(List(SSym("/="), id, exp)) => parse(ExpDesugar.desugar(sexp)) //  NOVO (/= id exp)
    //------------------------------------------------------------------------------------

    case SList(List(SSym("for"), init, cond, mod, body)) => parse(ExpDesugar.desugar(sexp))
    
    // #16 Implemente o desugar para a expressão (repeat body until-cond)
    case SList(List(SSym("repeat"),,body,untilCondicion)) => parse(ExpDesugar.desugar(sexp)) // NOVO (repeat body until-cond)
    //-------------------------------------------------------------------------------------

    case _ => throw ExpParserException(s"error in the expression '$sexp'")
  }

  /*
 * #9 Implemente a verificação de palavras reservadas de
 * modo que não seja possível utilizar uma palavra-chave
 * como 'while', por exemplo, como um identificador.
 */

  private def isIdentifier(str: String): Boolean = {
    val regex = """^[a-zA-Z][a-zA-Z0-9_]*$""".r
   if ( regex.pattern.matcher(str).matches()) {
    str match{
      case "while" => return true
      case "if" => return true
      case "print" => return true
      case "var" => return true
      case "begin" => return true
      case "print" => return true
      case "true" => return true
      case "false" => return true
      case "print" => return true
      case "set" => return true
      case "init" => return true
      case _ => return false
    }
   }
    return false
  }
}
