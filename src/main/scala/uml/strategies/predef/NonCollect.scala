package uml.strategies.predef

import org.bitbucket.inkytonik.kiama.rewriting.Strategy
import uml.UMLElement
import uml.strategies.collecting.CollectStrategy
import uml.strategies.rewriting.RewriteStrategy

case class NonCollect[T]() extends CollectStrategy[T] {
  override def apply(v1: UMLElement, v2: T): T = v2
}
