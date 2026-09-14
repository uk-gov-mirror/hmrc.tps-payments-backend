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

package tps.testdata

import tps.journey.model.Journey
import tps.testdata.tdjourney.externaltaxtypes.{TdJourneyInStatesExternalTaxTypes, TdJourneyInStatesExternalTaxTypesOld, TdJourneyMib, TdJourneyMibOld, TdJourneyPngr, TdJourneyPngrOld}
import tps.testdata.tdjourney.{TdJourneyChildBenefit, TdJourneyCotax, TdJourneyInStates, TdJourneyNps, TdJourneyNtc, TdJourneyPaye, TdJourneyPpt, TdJourneySa, TdJourneySafe, TdJourneySdlt, TdJourneyVat, TdJourneyVatIossAndOss, TdMultiPaymentJourney}
import tps.testdata.util.JourneyJson

object TdAll extends TdAll

trait TdAll
    extends AnyRef
    with TdBase
    with TdRequest
    with TdJourneyMibOld
    with TdJourneyMib
    with TdJourneyPngrOld
    with TdJourneyPngr
    with TdJourneyChildBenefit
    with TdJourneyCotax
    with TdJourneySa
    with TdJourneySdlt
    with TdJourneySafe
    with TdJourneyNtc
    with TdJourneyPaye
    with TdJourneyNps
    with TdJourneyVat
    with TdJourneyVatIossAndOss
    with TdJourneyPpt
    with TdMultiPaymentJourney:

  /** only journeys with TpsInternalTaxType
    */
  @SuppressWarnings(
    Array(
      "org.wartremover.warts.JavaSerializable",
      "org.wartremover.warts.Serializable",
      "org.wartremover.warts.Product"
    )
  )
  lazy val allTdJourneyInStatesInternalTaxTypes: List[TdJourneyInStates] = List(
    TdJourneyChildBenefit,
    TdJourneyCotax,
    TdJourneyNps,
    TdJourneyNtc,
    TdJourneyPaye,
    TdJourneyPpt,
    TdJourneySa,
    TdJourneySafe,
    TdJourneySdlt,
    TdJourneyVat,
    TdJourneyVatIoss,
    TdJourneyVatOss,
    TdJourneyIossOssPenalty
  )

  /** only journeys with ExternalTaxType
    */
  lazy val allTdJourneyInStatesExternalTaxTypesOld: List[TdJourneyInStatesExternalTaxTypesOld] = List(
    TdJourneyPngrOld,
    TdJourneyMibOld
  )

  /** only journeys with ExternalTaxType
    */
  lazy val allTdJourneyInStatesExternalTaxTypes: List[TdJourneyInStatesExternalTaxTypes] = List(
    TdJourneyPngr,
    TdJourneyMib
  )

  lazy val allJourneysWithJson: List[(Journey, JourneyJson)] =
    (allTdJourneyInStatesInternalTaxTypes.map(_.allJourneys) ++ allTdJourneyInStatesExternalTaxTypesOld.map(
      _.allJourneys
    ))
      .foldLeft[List[(Journey, JourneyJson)]](Nil)(_ ++ _)
      .++(TdJourneyMultiPayment.allJourneys)
