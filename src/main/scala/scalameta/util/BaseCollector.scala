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

package scalameta.util

import uml.externalReferences.ClassDefRef
import uml.{Attribute, Class, NamedElement, Operation, RelateableElement, Relationship, UMLElement}

trait BaseCollector extends StateChangingCollector {
  val definedElements:List[UMLElement]
  
  def operations: List[Operation] = definedElements.flatMap{
    case o : Operation => Some(o)
    case _ => None 
  }

  def templates : List[NamedElement with RelateableElement] = definedElements.flatMap{
    case c:Class => Some(c)
    case _ => None
  }

  def innerElements : List[UMLElement] = definedElements.flatMap{
    case c:Class => Some(c)
    case r:Relationship => Some(r)
    case _ => None
  }

  def classDefRefs : List[UMLElement] = definedElements.flatMap{
    case c:ClassDefRef => Some(c)
    case _ => None
  }

  def attributes : List[Attribute] = definedElements.flatMap{
    case a:Attribute => Some(a) case _ => None
  }
}
