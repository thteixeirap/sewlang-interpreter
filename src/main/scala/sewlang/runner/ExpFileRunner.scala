package sewlang.runner

import sewlang.util.FileReader
import scala.util.{ Failure, Success }
import sexpr.exception.SExprParserException
import sewlang.exception._
import sexpr.parser.SExprParser
import sewlang.desugar.ExpDesugar
import sewlang.parser.ExpParser
import sewlang.interpreter.ExpInterpreter

object ExpFileRunner {

  def run(filePath: String): Unit = {
    FileReader.sourceCode(filePath) match {
      case Success(sourceCode) => runSourceCode(sourceCode)
      case Failure(s)          => Console.err.println(s"sewlang error: file '$filePath' not found")
    }
  }

  private def runSourceCode(sourceCode: String): Unit = {
    try {
      ExpInterpreter.eval(
        ExpParser.parse(
          ExpDesugar.desugar(
            SExprParser.parse(s"(begin $sourceCode)"))))(ExpInterpreter.globalEnvironment)
    } catch {
      case e: SExprParserException => Console.err.println(s"s-expression parsing error: ${e.getMessage}")
      case e: ExpParserException   => Console.err.println(s"sewlang parsing error: ${e.getMessage}")
      case e: Exception            => Console.err.println(s"sewlang interpreting error: ${e.getMessage}")
    }
  }

}