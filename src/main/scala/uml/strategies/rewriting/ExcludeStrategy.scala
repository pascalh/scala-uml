package uml.strategies.rewriting

import org.bitbucket.inkytonik.kiama.rewriting.PositionedRewriter.rulef
import org.bitbucket.inkytonik.kiama.rewriting.Strategy
import uml.externalReferences.ClassDefRef
import uml.{ClassRef, ConcreteClass, PackageRef, Relationship, UMLUnit}

import scala.util.matching.Regex

object ExcludeStrategy extends RewriteStrategy[Regex]{
  override def apply(v1: Regex): Strategy = {
    val f: Any => Any = u => u match {
      case u:UMLUnit =>
        val excludedToplevelElements = u.toplevelElements
          .filterNot{
            case c:uml.Class =>
              matchesNameOrNamespace(v1,c.name,c.namespace.toString)
            case r:Relationship =>
              val fromB = r.relationshipInfo.from match {
                case ConcreteClass(cls) => matchesNameOrNamespace(v1,cls.name,cls.namespace.toString)
                case ClassRef(name, namespace) => matchesNameOrNamespace(v1,name,namespace.toString)
                case PackageRef(namespace) => matchesNameOrNamespace(v1,namespace.toString.dropRight(1),"")
              }
              val toB = r.relationshipInfo.to match {
                case ConcreteClass(cls) => matchesNameOrNamespace(v1,cls.name,cls.namespace.toString)
                case ClassRef(name, namespace) => matchesNameOrNamespace(v1,name,namespace.toString)
                case PackageRef(namespace) => matchesNameOrNamespace(v1,namespace.toString.dropRight(1),"")
              }
              fromB || toB
            case p:uml.Package => matchesNameOrNamespace(v1,p.namespace.toString.dropRight(1),"")
            case c:ClassDefRef => matchesNameOrNamespace(v1,c.name,c.namespace.toString + c.name)
            case _ => false
          }
        u.copy(toplevelElements = excludedToplevelElements)
      case p:uml.Package =>
        val excludedPackageBodyElements = p.packageBodyElements
          .filterNot{
            case c:uml.Class =>
              matchesNameOrNamespace(v1,c.name,c.namespace.toString)
            case r:Relationship =>
              val fromB = r.relationshipInfo.from match {
                case ConcreteClass(cls) => matchesNameOrNamespace(v1,cls.name,cls.namespace.toString)
                case ClassRef(name, namespace) => matchesNameOrNamespace(v1,name,namespace.toString)
                case PackageRef(namespace) => matchesNameOrNamespace(v1,namespace.toString.dropRight(1),"")
              }
              val toB = r.relationshipInfo.to match {
                case ConcreteClass(cls) => matchesNameOrNamespace(v1,cls.name,cls.namespace.toString)
                case ClassRef(name, namespace) => matchesNameOrNamespace(v1,name,namespace.toString)
                case PackageRef(namespace) => matchesNameOrNamespace(v1,namespace.toString.dropRight(1),"")
              }
              fromB || toB
            case p:uml.Package => matchesNameOrNamespace(v1,p.namespace.toString.dropRight(1),"")
            case _ => false
          }
        p.copy(packageBodyElements = excludedPackageBodyElements)
      case u@_ => u
    }
    rulef(f)
  }

  //Check if name without namespace matches regex
  //Check if name together with namespace matches regex
  private def matchesNameOrNamespace(v1:Regex,name:String,namespace:String):Boolean = {
      v1.matches(name) || v1.matches(namespace+name)
  }
}
