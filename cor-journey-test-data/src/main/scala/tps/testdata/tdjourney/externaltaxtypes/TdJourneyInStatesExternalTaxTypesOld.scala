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

package tps.testdata.tdjourney.externaltaxtypes

import tps.journey.model.{Journey, JourneyId, JourneyState}
import tps.model.{ExternalTaxType, Navigation, PaymentItem, PaymentSpecificData}
import tps.pcipalmodel.{ChargeRefNotificationPcipalRequest, PcipalSessionLaunchRequest, PcipalSessionLaunchResponse}
import tps.startjourneymodel.StartJourneyRequestMibOrPngr
import tps.testdata.util.JourneyJson

import java.time.Instant

// TODO: remove according to OPS-11079
trait TdJourneyInStatesExternalTaxTypesOld {
  def taxType: ExternalTaxType

  def journeyId: JourneyId

  def pid: String

  def created: Instant

  def navigation: Navigation

  def amountString: String

  final def amount: BigDecimal = BigDecimal(amountString)

  def taxReference: String

  def pcipalSessionLaunchRequest: PcipalSessionLaunchRequest

  def pcipalSessionLaunchResponse: PcipalSessionLaunchResponse

  def pcipalData: ChargeRefNotificationPcipalRequest

  def paymentItemBeforePcipal: PaymentItem // i.e. pcipal data is None

  def paymentItem: PaymentItem

  def paymentSpecificData: PaymentSpecificData

  def startJourneyRequest: StartJourneyRequestMibOrPngr

  def journeyCreated: Journey

  def journeyCreatedJson: JourneyJson

  lazy val journeyAtPciPal: Journey =
    journeyCreated.copy(
      journeyState = JourneyState.AtPciPal,
      pcipalSessionLaunchRequest = Some(pcipalSessionLaunchRequest),
      pcipalSessionLaunchResponse = Some(pcipalSessionLaunchResponse)
    )

  def journeyAtPciPalJson: JourneyJson

  lazy val journeyResetByPciPal: Journey = journeyAtPciPal.copy(
    journeyState = JourneyState.ResetByPciPal
  )

  def journeyResetByPciPalJson: JourneyJson

  lazy val journeyFinishedByPciPal: Journey = journeyAtPciPal.copy(
    journeyState = JourneyState.FinishedByPciPal
  )

  def journeyFinishedByPciPalJson: JourneyJson

  lazy val journeyBackByPciPal: Journey = journeyAtPciPal.copy(
    journeyState = JourneyState.BackByPciPal
  )

  def journeyBackByPciPalJson: JourneyJson

  lazy val journeyReceivedNotification: Journey = journeyFinishedByPciPal.copy(
    journeyState = JourneyState.ReceivedNotification,
    payments = List(
      paymentItem.copy(
        pcipalData = Some(pcipalData)
      )
    )
  )

  def journeyReceivedNotificationJson: JourneyJson

  lazy val allJourneys: List[(Journey, JourneyJson)] = List(
    (journeyCreated, journeyCreatedJson),
    (journeyAtPciPal, journeyAtPciPalJson),
    (journeyResetByPciPal, journeyResetByPciPalJson),
    (journeyFinishedByPciPal, journeyFinishedByPciPalJson),
    (journeyBackByPciPal, journeyBackByPciPalJson),
    (journeyReceivedNotification, journeyReceivedNotificationJson)
  )
}
