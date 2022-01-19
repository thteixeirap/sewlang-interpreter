package sewlang.interpreter

import org.scalatest.funsuite.AnyFunSuite
import org.scalamock.scalatest.MockFactory
import sewlang.util.ExpTestUtilities
import sewlang.exception.ExpUnboundVariableException
import sewlang.exception.ExpInvalidValueTypeException
import sewlang.exception.ExpIdentifierRequiredException
import org.scalatest.BeforeAndAfterAll

class ExpInterpreterTest extends AnyFunSuite with BeforeAndAfterAll with MockFactory with ExpTestUtilities {

  implicit val testEnvironment = Environment(Map("null" -> NilV, "PI" -> NumberV(math.Pi), "zero" -> NumberV(0)))

  test("Exp Interpreting Basic Value Expressions") {
    assert(ExpInterpreter(parseSExprToExp("0")) == NumberV(0))
    assert(ExpInterpreter(parseSExprToExp("5.2")) == NumberV(5.2))
    assert(ExpInterpreter(parseSExprToExp("\"test\"")) == StringV("test"))
    assert(ExpInterpreter(parseSExprToExp("\"\"")) == StringV(""))
    assert(ExpInterpreter(parseSExprToExp("true")) == BoolV(true))
    assert(ExpInterpreter(parseSExprToExp("false")) == BoolV(false))
    assert(ExpInterpreter(parseSExprToExp("nil")) == NilV)
  }

  test("Exp Interpreting Bound Id Expressions") {
    assert(ExpInterpreter(parseSExprToExp("null")) == NilV)
    assert(ExpInterpreter(parseSExprToExp("PI")) == NumberV(math.Pi))
  }

  test("Exp Interpreting Unbound Id Expressions") {
    val caught =
      intercept[ExpUnboundVariableException] {
        ExpInterpreter(parseSExprToExp("num"))
      }
    assert(caught.getMessage.contains("num"))
  }

  test("Exp Interpreting Sum Expressions") {
    assert(ExpInterpreter(parseSExprToExp("(+ 2 3)")) == NumberV(5))
    assert(ExpInterpreter(parseSExprToExp("(+ 1.2 zero)")) == NumberV(1.2))
  }

  test("Exp Interpreting Multiplication Expressions") {
    assert(ExpInterpreter(parseSExprToExp("(* 2 3.5)")) == NumberV(7))
    assert(ExpInterpreter(parseSExprToExp("(* zero (+ 2 3))")) == NumberV(0))
  }

  test("Exp Interpreting Invalid Arithmetic Expressions") {
    var caught =
      intercept[ExpInvalidValueTypeException] {
        ExpInterpreter(parseSExprToExp("(+ 1 true)"))
      }
    assert(caught.getMessage.contains("arithmetic"))
    assert(caught.getMessage.contains("1"))
    assert(caught.getMessage.contains("true"))

    caught =
      intercept[ExpInvalidValueTypeException] {
        ExpInterpreter(parseSExprToExp("(* \"x\" 3)"))
      }
    assert(caught.getMessage.contains("arithmetic"))
    assert(caught.getMessage.contains("x"))
    assert(caught.getMessage.contains("3"))

    caught =
      intercept[ExpInvalidValueTypeException] {
        ExpInterpreter(parseSExprToExp("(* null false)"))
      }
    assert(caught.getMessage.contains("arithmetic"))
    assert(caught.getMessage.contains("nil"))
    assert(caught.getMessage.contains("false"))
  }

  test("Exp Interpreting Equal Expressions") {
    assert(ExpInterpreter(parseSExprToExp("(= zero 0)")) == BoolV(true))
    assert(ExpInterpreter(parseSExprToExp("(= 3 (+ 2 1))")) == BoolV(true))
    assert(ExpInterpreter(parseSExprToExp("(= 2 3)")) == BoolV(false))
  }

  test("Exp Interpreting Less Than Expressions") {
    assert(ExpInterpreter(parseSExprToExp("(< zero 1)")) == BoolV(true))
    assert(ExpInterpreter(parseSExprToExp("(< 3 (* 4 7))")) == BoolV(true))
    assert(ExpInterpreter(parseSExprToExp("(< (+ 2 3) 5)")) == BoolV(false))
  }

  test("Exp Interpreting Invalid Relational Expressions") {
    var caught =
      intercept[ExpInvalidValueTypeException] {
        ExpInterpreter(parseSExprToExp("(= 1 true)"))
      }
    assert(caught.getMessage.contains("relational"))
    assert(caught.getMessage.contains("1"))
    assert(caught.getMessage.contains("true"))

    caught =
      intercept[ExpInvalidValueTypeException] {
        ExpInterpreter(parseSExprToExp("(< false 3)"))
      }
    assert(caught.getMessage.contains("relational"))
    assert(caught.getMessage.contains("false"))
    assert(caught.getMessage.contains("3"))

    caught =
      intercept[ExpInvalidValueTypeException] {
        ExpInterpreter(parseSExprToExp("(= \"false\" false)"))
      }
    assert(caught.getMessage.contains("relational"))
    assert(caught.getMessage.contains("false"))
  }

  test("Exp Interpreting Not Expressions") {
    assert(ExpInterpreter(parseSExprToExp("(not true)")) == BoolV(false))
    assert(ExpInterpreter(parseSExprToExp("(not false)")) == BoolV(true))
    assert(ExpInterpreter(parseSExprToExp("(not (< zero 1))")) == BoolV(false))
    assert(ExpInterpreter(parseSExprToExp("(not (= 2 3))")) == BoolV(true))
  }

  test("Exp Interpreting And Expressions") {
    assert(ExpInterpreter(parseSExprToExp("(and true (not false))")) == BoolV(true))
    assert(ExpInterpreter(parseSExprToExp("(and true false)")) == BoolV(false))
    assert(ExpInterpreter(parseSExprToExp("(and false true)")) == BoolV(false))
    assert(ExpInterpreter(parseSExprToExp("(and false false)")) == BoolV(false))
    assert(ExpInterpreter(parseSExprToExp("(and (= (+ 2 3) 5) (< 0 1))")) == BoolV(true))
  }

  test("Exp Interpreting Invalid Logical Expressions") {
    var caught =
      intercept[ExpInvalidValueTypeException] {
        ExpInterpreter(parseSExprToExp("(not 1)"))
      }
    assert(caught.getMessage.contains("logical"))
    assert(caught.getMessage.contains("1"))

    caught =
      intercept[ExpInvalidValueTypeException] {
        ExpInterpreter(parseSExprToExp("(and false 3)"))
      }
    assert(caught.getMessage.contains("logical"))
    assert(caught.getMessage.contains("false"))
    assert(caught.getMessage.contains("3"))

    caught =
      intercept[ExpInvalidValueTypeException] {
        ExpInterpreter(parseSExprToExp("(and nil true)"))
      }
    assert(caught.getMessage.contains("logical"))
    assert(caught.getMessage.contains("nil"))
    assert(caught.getMessage.contains("true"))

    caught =
      intercept[ExpInvalidValueTypeException] {
        ExpInterpreter(parseSExprToExp("(and 1 1)"))
      }
    assert(caught.getMessage.contains("logical"))
    assert(caught.getMessage.contains("1"))
    assert(caught.getMessage.contains("1"))
  }

  test("Exp Interpreting Variable Declaration") {
    val localTestEnv = Environment(testEnvironment)
    assert(ExpInterpreter(parseSExprToExp("(var x 10)"))(localTestEnv) == NumberV(10))
    assert(ExpInterpreter(parseSExprToExp("(var continue true)"))(localTestEnv) == BoolV(true))
    assert(localTestEnv.lookup("x") == NumberV(10))
    assert(localTestEnv.lookup("continue") == BoolV(true))
  }

  test("Exp Interpreting Variable Assignment") {
    val localTestEnv = Environment(Map("x" -> NumberV(0), "continue" -> BoolV(true)), testEnvironment)
    assert(ExpInterpreter(parseSExprToExp("(set x (+ x 1))"))(localTestEnv) == NumberV(1))
    assert(ExpInterpreter(parseSExprToExp("(set continue false)"))(localTestEnv) == BoolV(false))
    assert(localTestEnv.lookup("x") == NumberV(1))
    assert(localTestEnv.lookup("continue") == BoolV(false))
  }

  test("Exp Interpreting Variable Declaration and Assignment Without Identifier") {
    val localTestEnv = Environment(testEnvironment)
    assertThrows[ExpIdentifierRequiredException] {
      ExpInterpreter(parseSExprToExp("(var 10 x)"))(localTestEnv)
    }

    assertThrows[ExpIdentifierRequiredException] {
      ExpInterpreter(parseSExprToExp("(set 10 10)"))(localTestEnv)
    }
  }

  test("Exp Interpreting If Expressions") {
    val localTestEnv = Environment(Map("continue" -> BoolV(true), "a" -> NumberV(1), "b" -> NumberV(2)))
    assert(ExpInterpreter(parseSExprToExp("(if (< a b) a b)"))(localTestEnv) == NumberV(1))
    assert(ExpInterpreter(parseSExprToExp("(if (= a b) b a)"))(localTestEnv) == NumberV(1))
    assert(ExpInterpreter(parseSExprToExp("(if (not (and (< a 2) (= b 2))) (+ a 1) (+ b 1))"))(localTestEnv) == NumberV(3))
  }

  test("Exp Interpreting If Expressions Without Boolean Condition") {
    assertThrows[ExpInvalidValueTypeException] {
      ExpInterpreter(parseSExprToExp("(if nil 1 2)"))
    }
  }

  test("Exp Interpreting While Expressions") {
    val localTestEnv = Environment(Map("continue" -> BoolV(true), "x" -> NumberV(0)))
    assert(ExpInterpreter(parseSExprToExp("(while false 1)"))(localTestEnv) == NilV)
    assert(ExpInterpreter(parseSExprToExp("(while continue (set continue false))"))(localTestEnv) == BoolV(false))
    assert(ExpInterpreter(parseSExprToExp("(while (< x 10) (set x (+ x 1)))"))(localTestEnv) == NumberV(10))
    assert(localTestEnv.lookup("x") == NumberV(10))
  }

  test("Exp Interpreting While Expressions Without Boolean Condition") {
    assertThrows[ExpInvalidValueTypeException] {
      ExpInterpreter(parseSExprToExp("(while 1 (+ 1 2))"))
    }
  }

  test("Exp Interpreting Block Expressions ") {
    val localTestEnv = Environment(Map("x" -> NumberV(10)))
    val input1 =
      """
        (begin
          (var x 0)
          (set x (+ x 1))
          x  
        )
      """
    assert(ExpInterpreter(parseSExprToExp(input1))(localTestEnv) == NumberV(1))
    assert(localTestEnv.lookup("x") == NumberV(10))

    val input2 =
      """
        (begin 
          (var y 1)
          (begin
            (var y true)
          )
          (set y (+ y 1))
        )
      """
    assert(ExpInterpreter(parseSExprToExp(input2))(localTestEnv) == NumberV(2))
    assertThrows[ExpUnboundVariableException](localTestEnv.lookup("y"))

    assert(ExpInterpreter(parseSExprToExp("(begin)"))(localTestEnv) == NilV)
  }

  test("Exp Interpreting Print Expressioon") {
    val printTest1 = mockFunction[Any, Unit]
    printTest1 expects ("x = 1")
    ExpInterpreter.print = printTest1
    assert(ExpInterpreter(parseSExprToExp("(print \"x = \" 1)")) == NilV)

    val printTest2 = mockFunction[Any, Unit]
    printTest2 expects ("truefalsenil")
    ExpInterpreter.print = printTest2
    assert(ExpInterpreter(parseSExprToExp("(print true false nil)")) == NilV)

    val printTest3 = mockFunction[Any, Unit]
    printTest3 expects ""
    ExpInterpreter.print = printTest3
    assert(ExpInterpreter(parseSExprToExp("(print)")) == NilV)

    val printTest4 = mockFunction[Any, Unit]
    printTest4 expects ("x = 2.5")
    ExpInterpreter.print = printTest4
    assert(ExpInterpreter(parseSExprToExp("(print \"x = \" 2.5)")) == NilV)
  }

  test("Exp Interpreting Read Number Expressioon") {
    val readTest1 = mockFunction[Double]
    readTest1 expects () returning 5.0
    ExpInterpreter.readDouble = readTest1
    assert(ExpInterpreter(parseSExprToExp("(read-num)")) == NumberV(5.0))

    val readTest2 = mockFunction[Double]
    readTest2 expects () returning 1
    readTest2 expects () returning 2
    ExpInterpreter.readDouble = readTest2
    assert(ExpInterpreter(parseSExprToExp("(+ (read-num) (read-num))")) == NumberV(3.0))
  }

  override def afterAll() = {
    ExpInterpreter.print = println
    ExpInterpreter.readDouble = io.StdIn.readDouble
  }

}