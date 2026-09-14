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

package tps.testdata.tdjourney

import tps.journey.model.JourneyId
import tps.model.vatiossandoss.CalendarQuarter.AprilToJune
import tps.model.vatiossandoss.{CalendarPeriod, CalendarQuarterlyPeriod, IossAndOssPaymentTypes, IossOssChargeReference, IossRegistrationNumber, OssVrn}
import tps.model.{HeadOfDutyIndicator, HeadOfDutyIndicators, Navigation, PaymentItem, SearchTag, TaxTypes, TpsNativeTaxType, VatIossAndOssSpecificData}
import tps.pcipalmodel.{ChargeRefNotificationPcipalRequest, PcipalInitialValues, PcipalSessionLaunchRequest, PcipalSessionLaunchResponse, StatusTypes}
import tps.testdata.TdBase
import tps.testdata.util.JourneyJson

import java.time.Instant

trait TdJourneyVatIossAndOss { dependencies: TdBase =>

  object TdJourneyVatIoss extends TdJourneyInStates {

    override def tdNameForTest: String = "VatIoss"

    val headOfDutyIndicator: HeadOfDutyIndicator          = HeadOfDutyIndicators.D
    val vatIossRegistrationNumber: IossRegistrationNumber = IossRegistrationNumber("IM1234567891")
    val vatIossPeriod: CalendarPeriod                     = CalendarPeriod(1, 2026)

    override lazy val journeyId: JourneyId               = dependencies.journeyId
    override lazy val pid: String                        = dependencies.pid
    override lazy val created: Instant                   = dependencies.instant
    override lazy val navigation: Navigation             = dependencies.navigation
    override lazy val amountEditedString: String         = "108.08"
    override lazy val taxReference: String               = "IM1234567891M0126"
    override final val selectedTaxType: TpsNativeTaxType = TaxTypes.VatIossAndOss
    override lazy val searchTag: SearchTag               = SearchTag("IM1234567891")

    override lazy val pcipalSessionLaunchRequest: PcipalSessionLaunchRequest = PcipalSessionLaunchRequest(
      FlowId = dependencies.flowId,
      InitialValues = List(
        PcipalInitialValues(
          clientId = "OSSM",
          pid = dependencies.pid,
          accountOfficeId = "G1",
          HODIdentifier = headOfDutyIndicator,
          UTRReference = taxReference,
          name1 = dependencies.customerName.value,
          amount = amountEditedString,
          taxAmount = None,
          nicAmount = None,
          lnpClass2 = None,
          nirRate = None,
          startDate = None,
          endDate = None,
          vatPeriodReference = None,
          vatRemittanceType = None,
          paymentItemId = dependencies.paymentItemId,
          chargeReference = taxReference,
          taxRegimeDisplay = "VAT IOSS and OSS",
          reference = dependencies.pciPalReferenceNumber,
          increment = "1"
        )
      ),
      UTRBlacklistFlag = "N",
      postcodeFlag = "Y",
      taxRegime = "gen",
      TotalTaxAmountToPay = amountEditedString,
      callbackUrl = navigation.callback,
      backUrl = navigation.back,
      resetUrl = navigation.reset,
      finishUrl = navigation.finish,
      LanguageFlag = "E"
    )

    override lazy val pcipalSessionLaunchResponse: PcipalSessionLaunchResponse = PcipalSessionLaunchResponse(
      Id = dependencies.pciPalSessionId,
      LinkId = dependencies.linkId
    )

    override lazy val pcipalData: ChargeRefNotificationPcipalRequest = ChargeRefNotificationPcipalRequest(
      HoD = headOfDutyIndicator,
      TaxReference = taxReference,
      Amount = amountEdited,
      Commission = 0,
      CardType = dependencies.cardType,
      Status = StatusTypes.validated,
      PCIPalSessionId = dependencies.pciPalSessionId,
      TransactionReference = taxReference,
      paymentItemId = dependencies.paymentItemId,
      ChargeReference = taxReference,
      ReferenceNumber = dependencies.pciPalReferenceNumber,
      CardLast4 = dependencies.cardLast4Digits
    )

    override lazy val paymentItemEntered: PaymentItem = PaymentItem(
      paymentItemId = dependencies.paymentItemId,
      amount = amountEntered,
      headOfDutyIndicator = headOfDutyIndicator,
      updated = dependencies.instant,
      customerName = dependencies.customerName,
      chargeReference = taxReference,
      pcipalData = None,
      paymentSpecificData = VatIossAndOssSpecificData(
        iossAndOssPaymentType = IossAndOssPaymentTypes.Ioss,
        vatIossRegistrationNumber = Some(vatIossRegistrationNumber),
        vatIossPeriod = Some(vatIossPeriod),
        vatOssVrn = None,
        vatOssPeriod = None,
        vatIossOssPenaltyReference = None
      ),
      taxType = TaxTypes.VatIossAndOss,
      email = Some(dependencies.email),
      searchTag = Some(searchTag)
    )

    override lazy val paymentItemAfterReceivedNotification: PaymentItem =
      paymentItemEdited.copy(pcipalData = Some(pcipalData))

    override lazy val journeyStartedJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/ioss/journey-1-Started.json"
    )

    override lazy val journeyInEnterPaymentJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/ioss/journey-2-InEnterPaymentJson.json"
    )

    override lazy val journeyWithEnteredPaymentJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/ioss/journey-3-WithOnePaymentInTheBasket.json"
    )

    override def journeyInEditPaymentJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/ioss/journey-4-InEditPayment.json"
    )

    override def journeyWithEditedPaymentJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/ioss/journey-5-WithEditedPayment.json"
    )

    override lazy val journeyAtPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/ioss/journey-6-AtPciPal.json"
    )

    override lazy val journeyResetByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/ioss/journey-7.a-ResetByPciPal.json"
    )

    override lazy val journeyFinishedByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/ioss/journey-7.b-FinishedByPciPal.json"
    )

    override lazy val journeyBackByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/ioss/journey-7.c-BackByPciPal.json"
    )

    override lazy val journeyReceivedNotificationJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/ioss/journey-8-ReceivedNotification.json"
    )
  }

  object TdJourneyVatOss extends TdJourneyInStates {

    override def tdNameForTest: String = "VatOss"

    val headOfDutyIndicator: HeadOfDutyIndicator = HeadOfDutyIndicators.E
    val vatOssVrn: OssVrn                        = OssVrn("968501144")
    val vatOssPeriod: CalendarQuarterlyPeriod    = CalendarQuarterlyPeriod(AprilToJune, 2059)

    override lazy val journeyId: JourneyId               = dependencies.journeyId
    override lazy val pid: String                        = dependencies.pid
    override lazy val created: Instant                   = dependencies.instant
    override lazy val navigation: Navigation             = dependencies.navigation
    override lazy val amountEditedString: String         = "108.08"
    override lazy val taxReference: String               = "NI968501144Q259"
    override final val selectedTaxType: TpsNativeTaxType = TaxTypes.VatIossAndOss
    override lazy val searchTag: SearchTag               = SearchTag("968501144")

    override lazy val pcipalSessionLaunchRequest: PcipalSessionLaunchRequest = PcipalSessionLaunchRequest(
      FlowId = dependencies.flowId,
      InitialValues = List(
        PcipalInitialValues(
          clientId = "OSSM",
          pid = dependencies.pid,
          accountOfficeId = "G1",
          HODIdentifier = headOfDutyIndicator,
          UTRReference = taxReference,
          name1 = dependencies.customerName.value,
          amount = amountEditedString,
          taxAmount = None,
          nicAmount = None,
          lnpClass2 = None,
          nirRate = None,
          startDate = None,
          endDate = None,
          vatPeriodReference = None,
          vatRemittanceType = None,
          paymentItemId = dependencies.paymentItemId,
          chargeReference = taxReference,
          taxRegimeDisplay = "VAT IOSS and OSS",
          reference = dependencies.pciPalReferenceNumber,
          increment = "1"
        )
      ),
      UTRBlacklistFlag = "N",
      postcodeFlag = "Y",
      taxRegime = "gen",
      TotalTaxAmountToPay = amountEditedString,
      callbackUrl = navigation.callback,
      backUrl = navigation.back,
      resetUrl = navigation.reset,
      finishUrl = navigation.finish,
      LanguageFlag = "E"
    )

    override lazy val pcipalSessionLaunchResponse: PcipalSessionLaunchResponse = PcipalSessionLaunchResponse(
      Id = dependencies.pciPalSessionId,
      LinkId = dependencies.linkId
    )

    override lazy val pcipalData: ChargeRefNotificationPcipalRequest = ChargeRefNotificationPcipalRequest(
      HoD = headOfDutyIndicator,
      TaxReference = taxReference,
      Amount = amountEdited,
      Commission = 0,
      CardType = dependencies.cardType,
      Status = StatusTypes.validated,
      PCIPalSessionId = dependencies.pciPalSessionId,
      TransactionReference = taxReference,
      paymentItemId = dependencies.paymentItemId,
      ChargeReference = taxReference,
      ReferenceNumber = dependencies.pciPalReferenceNumber,
      CardLast4 = dependencies.cardLast4Digits
    )

    override lazy val paymentItemEntered: PaymentItem = PaymentItem(
      paymentItemId = dependencies.paymentItemId,
      amount = amountEntered,
      headOfDutyIndicator = headOfDutyIndicator,
      updated = dependencies.instant,
      customerName = dependencies.customerName,
      chargeReference = taxReference,
      pcipalData = None,
      paymentSpecificData = VatIossAndOssSpecificData(
        iossAndOssPaymentType = IossAndOssPaymentTypes.Oss,
        vatIossRegistrationNumber = None,
        vatIossPeriod = None,
        vatOssVrn = Some(vatOssVrn),
        vatOssPeriod = Some(vatOssPeriod),
        vatIossOssPenaltyReference = None
      ),
      taxType = TaxTypes.VatIossAndOss,
      email = Some(dependencies.email),
      searchTag = Some(searchTag)
    )

    override lazy val paymentItemAfterReceivedNotification: PaymentItem =
      paymentItemEdited.copy(pcipalData = Some(pcipalData))

    override lazy val journeyStartedJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/oss/journey-1-Started.json"
    )

    override lazy val journeyInEnterPaymentJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/oss/journey-2-InEnterPaymentJson.json"
    )

    override lazy val journeyWithEnteredPaymentJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/oss/journey-3-WithOnePaymentInTheBasket.json"
    )

    override def journeyInEditPaymentJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/oss/journey-4-InEditPayment.json"
    )

    override def journeyWithEditedPaymentJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/oss/journey-5-WithEditedPayment.json"
    )

    override lazy val journeyAtPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/oss/journey-6-AtPciPal.json"
    )

    override lazy val journeyResetByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/oss/journey-7.a-ResetByPciPal.json"
    )

    override lazy val journeyFinishedByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/oss/journey-7.b-FinishedByPciPal.json"
    )

    override lazy val journeyBackByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/oss/journey-7.c-BackByPciPal.json"
    )

    override lazy val journeyReceivedNotificationJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/oss/journey-8-ReceivedNotification.json"
    )
  }

  object TdJourneyIossOssPenalty extends TdJourneyInStates {

    override def tdNameForTest: String = "IossOssPenalty"

    val headOfDutyIndicator: HeadOfDutyIndicator           = HeadOfDutyIndicators.F
    val vatIossOssPenaltyReference: IossOssChargeReference = IossOssChargeReference("XE123456789012")

    override lazy val journeyId: JourneyId               = dependencies.journeyId
    override lazy val pid: String                        = dependencies.pid
    override lazy val created: Instant                   = dependencies.instant
    override lazy val navigation: Navigation             = dependencies.navigation
    override lazy val amountEditedString: String         = "108.08"
    override lazy val taxReference: String               = "XE123456789012"
    override final val selectedTaxType: TpsNativeTaxType = TaxTypes.VatIossAndOss
    override lazy val searchTag: SearchTag               = SearchTag("XE123456789012")

    override lazy val pcipalSessionLaunchRequest: PcipalSessionLaunchRequest = PcipalSessionLaunchRequest(
      FlowId = dependencies.flowId,
      InitialValues = List(
        PcipalInitialValues(
          clientId = "OSSM",
          pid = dependencies.pid,
          accountOfficeId = "G1",
          HODIdentifier = headOfDutyIndicator,
          UTRReference = taxReference,
          name1 = dependencies.customerName.value,
          amount = amountEditedString,
          taxAmount = None,
          nicAmount = None,
          lnpClass2 = None,
          nirRate = None,
          startDate = None,
          endDate = None,
          vatPeriodReference = None,
          vatRemittanceType = None,
          paymentItemId = dependencies.paymentItemId,
          chargeReference = taxReference,
          taxRegimeDisplay = "VAT IOSS and OSS",
          reference = dependencies.pciPalReferenceNumber,
          increment = "1"
        )
      ),
      UTRBlacklistFlag = "N",
      postcodeFlag = "Y",
      taxRegime = "gen",
      TotalTaxAmountToPay = amountEditedString,
      callbackUrl = navigation.callback,
      backUrl = navigation.back,
      resetUrl = navigation.reset,
      finishUrl = navigation.finish,
      LanguageFlag = "E"
    )

    override lazy val pcipalSessionLaunchResponse: PcipalSessionLaunchResponse = PcipalSessionLaunchResponse(
      Id = dependencies.pciPalSessionId,
      LinkId = dependencies.linkId
    )

    override lazy val pcipalData: ChargeRefNotificationPcipalRequest = ChargeRefNotificationPcipalRequest(
      HoD = headOfDutyIndicator,
      TaxReference = taxReference,
      Amount = amountEdited,
      Commission = 0,
      CardType = dependencies.cardType,
      Status = StatusTypes.validated,
      PCIPalSessionId = dependencies.pciPalSessionId,
      TransactionReference = taxReference,
      paymentItemId = dependencies.paymentItemId,
      ChargeReference = taxReference,
      ReferenceNumber = dependencies.pciPalReferenceNumber,
      CardLast4 = dependencies.cardLast4Digits
    )

    override lazy val paymentItemEntered: PaymentItem = PaymentItem(
      paymentItemId = dependencies.paymentItemId,
      amount = amountEntered,
      headOfDutyIndicator = headOfDutyIndicator,
      updated = dependencies.instant,
      customerName = dependencies.customerName,
      chargeReference = taxReference,
      pcipalData = None,
      paymentSpecificData = VatIossAndOssSpecificData(
        iossAndOssPaymentType = IossAndOssPaymentTypes.ChargeReference,
        vatIossRegistrationNumber = None,
        vatIossPeriod = None,
        vatOssVrn = None,
        vatOssPeriod = None,
        vatIossOssPenaltyReference = Some(vatIossOssPenaltyReference)
      ),
      taxType = TaxTypes.VatIossAndOss,
      email = Some(dependencies.email),
      searchTag = Some(searchTag)
    )

    override lazy val paymentItemAfterReceivedNotification: PaymentItem =
      paymentItemEdited.copy(pcipalData = Some(pcipalData))

    override lazy val journeyStartedJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/penalty/journey-1-Started.json"
    )

    override lazy val journeyInEnterPaymentJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/penalty/journey-2-InEnterPaymentJson.json"
    )

    override lazy val journeyWithEnteredPaymentJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/penalty/journey-3-WithOnePaymentInTheBasket.json"
    )

    override def journeyInEditPaymentJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/penalty/journey-4-InEditPayment.json"
    )

    override def journeyWithEditedPaymentJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/penalty/journey-5-WithEditedPayment.json"
    )

    override lazy val journeyAtPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/penalty/journey-6-AtPciPal.json"
    )

    override lazy val journeyResetByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/penalty/journey-7.a-ResetByPciPal.json"
    )

    override lazy val journeyFinishedByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/penalty/journey-7.b-FinishedByPciPal.json"
    )

    override lazy val journeyBackByPciPalJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/penalty/journey-7.c-BackByPciPal.json"
    )

    override lazy val journeyReceivedNotificationJson: JourneyJson = JourneyJson(
      "/tps/testdata/vatiossandoss/penalty/journey-8-ReceivedNotification.json"
    )
  }
}
