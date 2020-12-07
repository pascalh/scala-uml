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

package scalameta.stateless

import scalameta.util.context.CollectorContext

import scala.meta.Type

case class BoundsNameCollector(bounds:Option[String])

object BoundsNameCollector {
  def apply(bounds:Type.Bounds): BoundsNameCollector = bounds match {
    case Type.Bounds(None, Some(Type.ApplyInfix(Type.Name(lower),Type.Name(":>"),Type.Name(higher)))) =>
      new BoundsNameCollector(Some(s"Bounds<$lower,$higher>"))
    case Type.Bounds(None,Type.Name(lo)) =>
      new BoundsNameCollector(Some(s"LowerBound<$lo>"))
      //@todo for T :> A the behaviour of scala meta is not logical
    case _ => BoundsNameCollector(None)
  }
}
