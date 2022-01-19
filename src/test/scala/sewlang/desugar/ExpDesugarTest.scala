package sewlang.desugar

import sewlang.util.ExpTestUtilities
import org.scalatest.funsuite.AnyFunSuite
import sexpr.ast._

class ExpDesugarTest extends AnyFunSuite with ExpTestUtilities {

  test("Exp Desugaring Simple Expressions") {
    assert(ExpDesugar(SNum(5)) == SNum(5))
    assert(ExpDesugar(SString("test")) == SString("test"))
    assert(ExpDesugar(SSym("nil")) == SSym("nil"))
    assert(ExpDesugar(SSym("true")) == SSym("true"))
    assert(ExpDesugar(SSym("false")) == SSym("false"))
    assert(ExpDesugar(SSym("x")) == SSym("x"))
    assert(ExpDesugar(SList(List(SSym("read-num")))) == SList(List(SSym("read-num"))))
  }

  test("Exp Desugaring Simple Arithmetic Expressions") {
    val input1 = SList(List(SSym("+"), SNum(3), SSym("x"))) // expression (+ 3 x)
    val expected1 = SList(List(SSym("+"), SNum(3), SSym("x")))
    assert(ExpDesugar(input1) == expected1)

    val input2 = SList(List(SSym("*"), SSym("x"), SNum(3.2))) // expression (* x 3.2)
    val expected2 = SList(List(SSym("*"), SSym("x"), SNum(3.2)))
    assert(ExpDesugar(input2) == expected2)
  }

  test("Exp Desugaring Simple Relational Expressions") {
    val input1 = SList(List(SSym("="), SSym("x"), SNum(10))) // expression (= x 10)
    val expected1 = SList(List(SSym("="), SSym("x"), SNum(10)))
    assert(ExpDesugar(input1) == expected1)

    val input2 = SList(List(SSym("<"), SSym("x"), SList(List(SSym("+"), SSym("y"), SNum(1))))) // expression (< x (+ y 1))
    val expected2 = SList(List(SSym("<"), SSym("x"), SList(List(SSym("+"), SSym("y"), SNum(1)))))
    assert(ExpDesugar(input2) == expected2)
  }

  test("Exp Desugaring Simple Logical Expressions") {
    val input1 = SList(List(SSym("not"), SSym("false"))) // expression (not false)
    val expected1 = SList(List(SSym("not"), SSym("false")))
    assert(ExpDesugar(input1) == expected1)

    val input2 = SList(List(SSym("and"), SSym("true"), SSym("false"))) // expression (and true false)
    val expected2 = SList(List(SSym("and"), SSym("true"), SSym("false")))
    assert(ExpDesugar(input2) == expected2)
  }

  test("Exp Desugaring Simple Variable Declaration and Assignment Expressions") {
    val input1 = SList(List(SSym("var"), SSym("x"), SNum(0))) // expression (var x 0)
    val expected1 = SList(List(SSym("var"), SSym("x"), SNum(0)))
    assert(ExpDesugar(input1) == expected1)

    val input2 = SList(List(SSym("set"), SSym("x"), SList(List(SSym("+"), SSym("x"), SNum(1))))) // expression (set x (+ x 1))
    val expected2 = SList(List(SSym("set"), SSym("x"), SList(List(SSym("+"), SSym("x"), SNum(1)))))
    assert(ExpDesugar(input2) == expected2)
  }

  test("Exp Desugaring Simple If Expression") {
    val input1 = SList(List(SSym("if"), SList(List(SSym("="), SSym("x"), SNum(10))), SSym("x"), SNum(1))) // expression (if (= x 10) x 1)
    val expected1 = SList(List(SSym("if"), SList(List(SSym("="), SSym("x"), SNum(10))), SSym("x"), SNum(1)))
    assert(ExpDesugar(input1) == expected1)
  }

  test("Exp Desugaring Simple While Expression") {
    val input1 = SList(List(SSym("while"), SList(List(SSym("<"), SSym("x"), SNum(10))), SList(List(SSym("set"), SSym("x"), SList(List(SSym("+"), SSym("x"), SNum(1))))))) // expression (while (< x 10) (set x (+ x 1)))
    val expected1 = SList(List(SSym("while"), SList(List(SSym("<"), SSym("x"), SNum(10))), SList(List(SSym("set"), SSym("x"), SList(List(SSym("+"), SSym("x"), SNum(1)))))))
    assert(ExpDesugar(input1) == expected1)
  }

  test("Exp Desugaring Simple Block Expression") {
    val input1 = SList(List(SSym("begin"), SList(List(SSym("var"), SSym("continue"), SSym("true"))), SList(List(SSym("set"), SSym("continue"), SSym("false"))))) // expression (begin (var continue true) (set continue false))
    val expected1 = SList(List(SSym("begin"), SList(List(SSym("var"), SSym("continue"), SSym("true"))), SList(List(SSym("set"), SSym("continue"), SSym("false")))))
    assert(ExpDesugar(input1) == expected1)
  }

  test("Exp Desugaring Print Expression") {
    val input1 = SList(List(SSym("print"), SString("x = "), SSym("x"))) // expression (print "x = " x)
    val expected1 = SList(List(SSym("print"), SString("x = "), SSym("x")))
    assert(ExpDesugar(input1) == expected1)
  }

  // New Expressions

  test("Exp Desugaring Increment Expression") { // (++ x) --> (set x (+ x 1))
    val input1 = SList(List(SSym("++"), SSym("x"))) // (++ x)
    val expected1 = SList(List(SSym("set"), SSym("x"), SList(List(SSym("+"), SSym("x"), SNum(1))))) // (set x (+ x 1))
    assert(ExpDesugar(input1) == expected1)

    assert(ExpDesugar(parseSExpr("(++ a)")) == ExpDesugar(parseSExpr("(set a (+ a 1.0))")))
  }

  test("Exp Desugaring Sum Assignment Expression") { // (+= x y) --> (set x (+ x y))
    val input1 = SList(List(SSym("+="), SSym("x"), SSym("y"))) // (+= x y)
    val expected1 = SList(List(SSym("set"), SSym("x"), SList(List(SSym("+"), SSym("x"), SSym("y"))))) // (set x (+ x y))
    assert(ExpDesugar(input1) == expected1)

    assert(ExpDesugar(parseSExpr("(+= a 5)")) == ExpDesugar(parseSExpr("(set a (+ a 5.0))")))
  }

  test("Exp Desugaring For Expression") { // (for (var x 0) (< x 10) (++ x) (print x)) --> (begin (var x 0) (while (< x 10) (begin (print x) (++ x))))
    val input1 = SList(List(SSym("for"), SList(List(SSym("var"), SSym("x"), SNum(0))), SList(List(SSym("<"), SSym("x"), SNum(10))), SList(List(SSym("++"), SSym("x"))), SList(List(SSym("print"), SSym("x")))))
    val expected1 = SList(List(
      SSym("begin"),
      SList(List(SSym("var"), SSym("x"), SNum(0))),
      SList(List(SSym("while"), SList(List(SSym("<"), SSym("x"), SNum(10))),
        SList(List(
          SSym("begin"),
          SList(List(SSym("print"), SSym("x"))),
          SList(List(SSym("set"), SSym("x"), SList(List(SSym("+"), SSym("x"), SNum(1))))) // (++ x) desugared
        ))))))

    assert(ExpDesugar(input1) == expected1)

    val input2 = "(for (set a 50) (not (= a 0)) (set a (- a 1)) (print a))"
    val expected2 =
      """
        (begin
          (set a 50)
          (while (not (= a 0))
            (begin
              (print a)
              (set a (- a 1))
            )
          )
        )
      """
    assert(ExpDesugar(parseSExpr(input2)) == ExpDesugar(parseSExpr(expected2)))
  }

}