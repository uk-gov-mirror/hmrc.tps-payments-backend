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

package tps.testdata.tdjourney

import tps.journey.model.{Journey, JourneyId, JourneyState}
import tps.model._
import tps.pcipalmodel.{ChargeRefNotificationPcipalRequest, PcipalSessionLaunchRequest, PcipalSessionLaunchResponse}
import tps.testdata.util.JourneyJson

import java.time.Instant

/** Test data representing list of journeys in various states and associated data. Each journey has only one payment Only Journeys with TpsNativeTaxType types
  * are defined here.
  *
  * See `allJourneys` to see what journey moments are supported
  */
trait TdJourneyInStates:

  lazy val allJourneys: List[(Journey, JourneyJson)] = List(
    (journeyStarted, journeyStartedJson),
    (journeyInEnterPayment, journeyInEnterPaymentJson),
    (journeyWithEnteredPayment, journeyWithEnteredPaymentJson),
    (journeyInEditPayment, journeyInEditPaymentJson),
    (journeyWithEditedPayment, journeyWithEditedPaymentJson),
    (journeyAtPciPal, journeyAtPciPalJson),
    (journeyResetByPciPal, journeyResetByPciPalJson),
    (journeyFinishedByPciPal, journeyFinishedByPciPalJson),
    (journeyBackByPciPal, journeyBackByPciPalJson),
    (journeyReceivedNotification, journeyReceivedNotificationJson)
  )

  def journeyId: JourneyId
  def pid: String
  def created: Instant
  def navigation: Navigation

  def selectedTaxType: TpsNativeTaxType
  def tdNameForTest: String = selectedTaxType.entryName

  /** This amount entered during `EnterPayment` state. Later it's changed.
    */
  final def amountEntered: BigDecimal = BigDecimal("606.61")

  def amountEditedString: String

  /** This amount is set in `EditPayment` state. Change it using `amountEditedString`
    */
  final def amountEdited: BigDecimal = BigDecimal(amountEditedString)

  // TODO: provide a strong type for that, use it in Journey, etc. Make sure you don't break existing json formats
  def taxReference: String

  def searchTag: SearchTag

  def pcipalSessionLaunchRequest: PcipalSessionLaunchRequest
  def pcipalSessionLaunchResponse: PcipalSessionLaunchResponse
  def pcipalData: ChargeRefNotificationPcipalRequest

  /** Initial PaymentItem created during EnterPayment state.
    */
  def paymentItemEntered: PaymentItem

  /** PaymentItem updated during EditPayment state.
    */
  def paymentItemEdited: PaymentItem = paymentItemEntered.copy(amount = amountEdited)

  /** PaymentItem when received notification from PciPal (via payments-processor)
    */
  lazy val paymentItemAfterReceivedNotification: PaymentItem = paymentItemEdited.copy(
    pcipalData = Some(pcipalData)
  )

  lazy val journeyStarted: Journey = Journey(
    _id = journeyId,
    journeyState = JourneyState.Started,
    pid = pid,
    created = created,
    payments = Nil,
    navigation = navigation,
    pcipalSessionLaunchRequest = None,
    pcipalSessionLaunchResponse = None
  )

  def journeyStartedJson: JourneyJson

  /** A tax type was selected, journey become in EnterPayment state
    */
  lazy val journeyInEnterPayment: Journey =
    journeyStarted.copy(
      journeyState = JourneyState.EnterPayment(taxType = selectedTaxType)
    )

  def journeyInEnterPaymentJson: JourneyJson

  lazy val journeyWithEnteredPayment: Journey =
    journeyInEnterPayment.copy(
      journeyState = JourneyState.Started,
      payments = List(paymentItemEntered)
    )

  def journeyWithEnteredPaymentJson: JourneyJson

  lazy val journeyInEditPayment: Journey =
    journeyWithEnteredPayment.copy(
      journeyState = JourneyState.EditPayment(paymentItemEntered.paymentItemId)
    )

  def journeyInEditPaymentJson: JourneyJson

  lazy val journeyWithEditedPayment: Journey =
    journeyInEditPayment.copy(
      journeyState = JourneyState.Started,
      payments = List(paymentItemEdited)
    )

  def journeyWithEditedPaymentJson: JourneyJson

  lazy val journeyAtPciPal: Journey = journeyWithEditedPayment.copy(
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
    payments = List(paymentItemAfterReceivedNotification)
  )

  def journeyReceivedNotificationJson: JourneyJson
