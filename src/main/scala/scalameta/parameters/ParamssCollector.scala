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

package scalameta.operations.parameters

import scalameta.util.context.CollectorContext
import uml.Parameter

import scala.meta.Term

case class ParamssCollector(parameterLists:List[List[Parameter]])

object ParamssCollector {
  def apply(paramss:List[List[Term.Param]])(implicit context:CollectorContext): ParamssCollector = {
    val pparamss = for (params <- paramss) yield {
      ParamsCollector(params).parameters
    }

    new ParamssCollector(pparamss)
  }
}
