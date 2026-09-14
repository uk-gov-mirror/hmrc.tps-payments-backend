/*
 * Copyright 2026 HM Revenue & Customs
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

package tps.model.vatiossandoss

import enumeratum.{Enum, EnumEntry}
import play.api.libs.json.Format
import tps.utils.EnumFormat

import scala.collection.immutable

sealed trait IossAndOssPaymentType extends EnumEntry derives CanEqual

object IossAndOssPaymentTypes extends Enum[IossAndOssPaymentType] {
  case object Ioss            extends IossAndOssPaymentType
  case object Oss             extends IossAndOssPaymentType
  case object ChargeReference extends IossAndOssPaymentType

  override def values: immutable.IndexedSeq[IossAndOssPaymentType] = findValues
}

object IossAndOssPaymentType {
  given Format[IossAndOssPaymentType] = EnumFormat(IossAndOssPaymentTypes)
}
