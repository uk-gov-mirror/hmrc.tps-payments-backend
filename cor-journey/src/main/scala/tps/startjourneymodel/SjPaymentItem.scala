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

package tps.startjourneymodel

import play.api.libs.json.{Json, OFormat}
import tps.model.{CustomerName, Email, PaymentSpecificData, TaxType}

/** Start Journey (Sj) Payment Item
  */
final case class SjPaymentItem(
  chargeReference:     String,
  customerName:        CustomerName,
  amount:              BigDecimal,
  taxRegimeDisplay:    String,
  taxType:             TaxType,
  paymentSpecificData: PaymentSpecificData,
  email:               Option[Email]
)

object SjPaymentItem:
  given OFormat[SjPaymentItem] = Json.format[SjPaymentItem]
