/*
 * Copyright 2015 Tilman Zuckmantel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package scalameta.stats.defn

import scalameta.mods.ObjectModsCollector
import scalameta.stats.StatsCollector
import scalameta.stats.init.InitsCollector
import scalameta.stats.util.defs.obtainFurtherNamespace
import scalameta.util.BaseCollector
import scalameta.util.context.CollectorContext
import scalameta.util.namespaces.DefaultNamespace
import uml.{Attribute, Class, ClassRef, Compartment, ConcreteClass, Inner, Operation, Relationship, RelationshipInfo, Stereotype, ToFrom, UMLElement, Without}

import scala.meta.Defn

case class DefnObjectCollector(override val definedElements: List[UMLElement],
                               override val resultingContext: CollectorContext) extends BaseCollector

object DefnObjectCollector {
  def apply(defnObject:Defn.Object)(implicit context:CollectorContext): DefnObjectCollector = {
    val mods = ObjectModsCollector(defnObject.mods)
    val objectName = defnObject.name.value

    val tempThisPointer = ClassRef(objectName,namespace = context.localCon.currentNamespace)
    val previousThisPointer = context.localCon.thisPointer
    val previousToplevel = context.localCon.isTopLevel
    val previousNamespace = context.localCon.currentNamespace
    val newNamespace = obtainFurtherNamespace(previousNamespace,defnObject.name.value)

    val inheritedElements = InitsCollector(defnObject.templ.inits)(context
      .withThisPointer(tempThisPointer)
    .withNamespace(context.localCon.currentNamespace))

    val innerElements = StatsCollector(defnObject.templ.stats)(
      inheritedElements
        .resultingContext
        .notToplevel
        .withNamespace(newNamespace))

    val operations = innerElements.definedElements.flatMap{
      case o:Operation => Some(o)
      case _ => None
    }
    val innerWithoutOperations = innerElements.definedElements.flatMap{
      case _:Operation => None
      case other => Some(other)
    }

    val isCaseobject = mods.objectStereotypes.contains(Stereotype("caseobject",Nil))

    val cls = Class(
      false,
      objectName,
      innerWithoutOperations.flatMap{case a:Attribute => Some(a) case _ => None},
      operations,
      mods.modifiers,
      None,
      mods.objectStereotypes ++ (if(isCaseobject){Nil}else{List(Stereotype("object",Nil))}),
      previousNamespace
    )

    val innerRelationship = if(previousThisPointer.isDefined){
      Some(Relationship(Inner,ToFrom,RelationshipInfo(None,None,previousThisPointer.get,ConcreteClass(cls),None,Without),Nil))
    } else {None}

    new DefnObjectCollector(
      cls :: innerWithoutOperations ++ inheritedElements.definedElements ++ innerRelationship.map( List(_)).getOrElse(Nil),
      innerElements
        .resultingContext
        .withOptionalThisPointer(previousThisPointer)
        .withToplevel(previousToplevel)
        .withNamespace(previousNamespace)
    )
  }
}
