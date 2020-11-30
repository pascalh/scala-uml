package rewriting

import org.bitbucket.inkytonik.kiama.==>
import org.bitbucket.inkytonik.kiama.rewriting.Rewriter._
import org.bitbucket.inkytonik.kiama.rewriting.Strategy
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import scalameta.toplevel.SourcesCollector
import uml.{Parameter, Stereotype, UMLElement}

import scala.meta.{Source, dialects}

class RewritingSuite extends AnyFreeSpec with Matchers {
  case class TestData(){
    val program =
      """
        |package foo
        |
        |trait Foo(x:Int,y:Int)
        |
        |sealed case class Bar(d:Double) extends Bar(5,5) {
        |  def bar(ints:List[Int])(using c:Context):Foo = {
        |   c.getContextFoo(ints)
        |  }
        |}
        |
        |
        |""".stripMargin

    val tempProgram =
      """
        |package foo
        |
        |trait Foo()
        |""".stripMargin

    val parsedProgram = dialects.Dotty(program).parse[Source].get
    val parsedTempProgram = dialects.Dotty(tempProgram).parse[Source].get

    val collectedUml = SourcesCollector(List((parsedProgram,"fooAst.scala")),"foo-ast")
    val collectedTempUml = SourcesCollector(List((parsedTempProgram,"fooAst.scala")),"foo-ast")

  }

  "Rewriting the fooAst" - {
    "without intended changes doesn't change the fooAst" in new TestData() {
      val s : Unit => Strategy = _ => id
      val f: (UMLElement,()) => () = (ue:UMLElement,_) => ue match {
        case _ => ()
      }
      val res = collectedUml.umlUnit.rewrite(s)(())(f)

      res.value._2 must equal(collectedUml.umlUnit)
    }

    "by collecting the trait instances yields exactly one trait Foo " in new TestData() {
      val f : UMLElement ==> String = ue => ue match {
        case c:uml.Class if c.stereotype.contains(Stereotype("trait",Nil)) => c.identifier
      }
      val res = collectedUml.umlUnit.collect(f)

      res must have size 1
      res must contain("Foo")
    }
  }

  "by checking if the fooAst contains an implicit Parameter c of type Context returns true" in new TestData() {
    val elem = Parameter("c","Context",List(Stereotype("using",Nil)))

    val res = collectedUml.umlUnit.contains(elem)

    res must equal(true)
  }

  "by checking if the fooAst contains an implicit Parameter c of type NotContext returns false" in new TestData() {
    val elem = Parameter("c","NotContext",List(Stereotype("using",Nil)))

    val res = collectedUml.umlUnit.contains(elem)

    res must equal(false)
  }

  "by mapping the implicit parameter c to a new implicit parameter c' returns the mapped UMLELement" in new TestData() {
    val f : UMLElement ==> UMLElement = ue => ue match {
      case p@Parameter("c","Context",List(Stereotype("using",List()))) => p.copy(identifier = "c'")
      case elem@_ => elem
    }

    val res = collectedUml.umlUnit.map(f)
    res.contains(Parameter("c'","Context",List(Stereotype("using",Nil)))) must equal(true)
  }

  "counting the number of Int values as parameters in fooAst is 2" in new TestData(){
    val p:UMLElement => Boolean = u => u match {
      case Parameter(_,"Int",_) => true
      case _ => false
    }

    val res = collectedUml.umlUnit.count(p)
    res must equal(2)
  }

  "forall should evaluate that no type of the form Option[Foo] is used in Parameters" in new TestData {
    val p:UMLElement => Boolean = u => u match {
      case Parameter(_,"Option<Foo>",_) => false
      case _ => true
    }
    val res = collectedUml.umlUnit.forall(p)
    res must equal(true)
  }

  "exists yields true when searching for the existence of a class named Foo" in new TestData {
    val p:UMLElement => Boolean = u => u match {
      case c:uml.Class if c.identifier.equals("Foo") => true
      case _ => false
    }
    val res = collectedUml.umlUnit.exists(p)
    res must equal(true)
  }

  "toList yields a list of elements that exactly contains the elements of the tree" in new TestData {
    val res = collectedUml.umlUnit.toList
    //For each element of the list it is contained in the tree
    res.forall(u => collectedUml.umlUnit.contains(u)) must equal(true)
    //for each element of the tree, it is contained in the list
    collectedUml.umlUnit.forall(u => res.contains(u)) must equal(true)
  }

}
