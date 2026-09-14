/*
 * Copyright 2023 HM Revenue & Customs
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

package deniedrefs.model

import play.api.libs.json.{Json, OFormat}
import tps.model.Reference
import tps.model.repo.HasId

import java.time.LocalDateTime

/** This entity represents list of denied refs. We store it in mongo.
  */
final case class DeniedRefs(
  _id:      DeniedRefsId,
  refs:     List[Reference],
  inserted: LocalDateTime
) extends HasId[DeniedRefsId]
    derives CanEqual:

  private lazy val refsSet: Set[Reference] = refs.toSet
  def containsRef(reference: Reference): Boolean = refsSet.contains(reference)

object DeniedRefs:
  given OFormat[DeniedRefs] = Json.format[DeniedRefs]
