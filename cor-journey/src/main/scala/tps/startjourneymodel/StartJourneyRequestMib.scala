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
import tps.model.CustomerName

/** Start Journey Request (Sjr) For Merchandise in Baggage (Mib)
  * @param amendmentReference
  *   used to when processing notification from pcipal (this value is send back to Mib)
  * @param totalVatDue
  *   used in reconciliation when building finance report. They aren't strictly required to maka a payment.
  * @param totalDutyDue
  *   used in reconciliation when building finance report. They aren't strictly required to maka a payment.
  */
final case class StartJourneyRequestMib(
  mibReference:       String,
  customerName:       CustomerName,
  amount:             BigDecimal,
  amendmentReference: Option[Int],
  totalVatDue:        BigDecimal,
  totalDutyDue:       BigDecimal,
  backUrl:            String,
  resetUrl:           String,
  finishUrl:          String
) derives CanEqual

object StartJourneyRequestMib:
  given OFormat[StartJourneyRequestMib] = Json.format[StartJourneyRequestMib]
