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

package tps.pcipalmodel

import play.api.libs.functional.syntax._
import play.api.libs.json._
import tps.model.{HeadOfDutyIndicators, PaymentItemId}
import tps.pcipalmodel
import tps.pcipalmodel.PcipalInitialValues._

import scala.annotation.tailrec

final case class PcipalSessionLaunchRequest(
  FlowId:              Int,
  InitialValues:       List[PcipalInitialValues],
  UTRBlacklistFlag:    String,
  postcodeFlag:        String,
  taxRegime:           String,
  TotalTaxAmountToPay: String,
  callbackUrl:         String,
  backUrl:             String,
  resetUrl:            String,
  finishUrl:           String,
  LanguageFlag:        String
) derives CanEqual

object PcipalSessionLaunchRequest:

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  given reads: Reads[PcipalSessionLaunchRequest] = (
    (JsPath \ "FlowId").read[Int] and
      (JsPath \ "InitialValues").read[Map[String, String]].map(m => unwrap(m)) and
      (JsPath \ "InitialValues")
        .read[Map[String, String]]
        .map(m => getSingleValue(m, PcipalInitialValues.UTRBlacklistFlag)) and
      (JsPath \ "InitialValues")
        .read[Map[String, String]]
        .map(m => getSingleValue(m, PcipalInitialValues.postcodeFlag)) and
      (JsPath \ "InitialValues")
        .read[Map[String, String]]
        .map(m => getSingleValue(m, PcipalInitialValues.taxRegime)) and
      (JsPath \ "InitialValues")
        .read[Map[String, String]]
        .map(m => getSingleValue(m, PcipalInitialValues.TotalTaxAmountToPay)) and
      (JsPath \ "InitialValues")
        .read[Map[String, String]]
        .map(m => getSingleValue(m, PcipalInitialValues.callbackUrl)) and
      (JsPath \ "InitialValues").read[Map[String, String]].map(m => getSingleValue(m, PcipalInitialValues.backURL)) and
      (JsPath \ "InitialValues").read[Map[String, String]].map(m => getSingleValue(m, PcipalInitialValues.resetURL)) and
      (JsPath \ "InitialValues")
        .read[Map[String, String]]
        .map(m => getSingleValue(m, PcipalInitialValues.finishURL)) and
      (JsPath \ "InitialValues").read[Map[String, String]].map(m => getLanguageFlag(m))
  )(PcipalSessionLaunchRequest.apply _)

  private def getSingleValue(values: Map[String, String], keyToFind: String): String =
    values.getOrElse(keyToFind, throw new RuntimeException(s"Could not find key: $keyToFind"))

  private def getLanguageFlag(values: Map[String, String]): String =
    values.getOrElse(PcipalInitialValues.LanguageFlag, "E")

  @tailrec
  @SuppressWarnings(Array("org.wartremover.warts.ListAppend"))
  private def unwrap(
    values: Map[String, String],
    a:      Int = 1,
    acc:    List[PcipalInitialValues] = List.empty[PcipalInitialValues]
  ): List[PcipalInitialValues] =
    if !values.contains(s"${PcipalInitialValues.ClientID}${a.toString}") then acc
    else
      val newPciPal = pcipalmodel.PcipalInitialValues(
        clientId = values(s"${PcipalInitialValues.ClientID}${a.toString}"),
        pid = values(s"${PcipalInitialValues.PID}${a.toString}"),
        accountOfficeId = values(s"${PcipalInitialValues.AccountOfficeID}${a.toString}"),
        HODIdentifier = HeadOfDutyIndicators.forCode(values(s"${PcipalInitialValues.HODIdentifier}${a.toString}")),
        UTRReference = values(s"${PcipalInitialValues.UTRReference}${a.toString}"),
        name1 = values(s"${PcipalInitialValues.Name1}${a.toString}"),
        amount = values(s"${PcipalInitialValues.Amount}${a.toString}"),
        taxAmount = values.get(s"${PcipalInitialValues.TaxAmount}${a.toString}"),
        nicAmount = values.get(s"${PcipalInitialValues.NICAmount}${a.toString}"),
        lnpClass2 = values.get(s"${PcipalInitialValues.LNPClass2}${a.toString}"),
        nirRate = values.get(s"${PcipalInitialValues.NIRSRate}${a.toString}"),
        startDate = values.get(s"${PcipalInitialValues.StartDate}${a.toString}"),
        endDate = values.get(s"${PcipalInitialValues.EndDate}${a.toString}"),
        vatPeriodReference = values.get(s"${PcipalInitialValues.VATPeriodReference}${a.toString}"),
        vatRemittanceType = values.get(s"${PcipalInitialValues.VATRemittanceType}${a.toString}"),
        paymentItemId = PaymentItemId(values(s"${PcipalInitialValues.PaymentItemID}${a.toString}")),
        chargeReference = values(s"${PcipalInitialValues.ChargeReference}${a.toString}"),
        taxRegimeDisplay = values(s"${PcipalInitialValues.TaxRegimeDisplay}${a.toString}"),
        reference = values(s"${PcipalInitialValues.ReferenceDisplay}${a.toString}"),
        increment = a.toString
      )

      unwrap(values, a + 1, acc :+ newPciPal)

  // WRITES=======================================================================================>
  given pcipalInitialValuesWrites: OWrites[PcipalInitialValues] = new OWrites[PcipalInitialValues]:
    def writes(pcipalInitialValues: PcipalInitialValues): JsObject =
      val taxAmountJs          =
        if pcipalInitialValues.taxAmount.isDefined then Json.obj(s"$TaxAmount${pcipalInitialValues.increment}" -> pcipalInitialValues.taxAmount)
        else JsObject.empty
      val nicAmountJs          =
        if pcipalInitialValues.nicAmount.isDefined then Json.obj(s"$NICAmount${pcipalInitialValues.increment}" -> pcipalInitialValues.nicAmount)
        else JsObject.empty
      val lnpClass2Js          =
        if pcipalInitialValues.lnpClass2.isDefined then Json.obj(s"$LNPClass2${pcipalInitialValues.increment}" -> pcipalInitialValues.lnpClass2)
        else JsObject.empty
      val nirRateJs            =
        if pcipalInitialValues.nirRate.isDefined then Json.obj(s"$NIRSRate${pcipalInitialValues.increment}" -> pcipalInitialValues.nirRate)
        else JsObject.empty
      val startDateJs          =
        if pcipalInitialValues.startDate.isDefined then Json.obj(s"$StartDate${pcipalInitialValues.increment}" -> pcipalInitialValues.startDate)
        else JsObject.empty
      val endDateJs            =
        if pcipalInitialValues.endDate.isDefined then Json.obj(s"$EndDate${pcipalInitialValues.increment}" -> pcipalInitialValues.endDate)
        else JsObject.empty
      val vatPeriodReferenceJs =
        if pcipalInitialValues.vatPeriodReference.isDefined then
          Json.obj(s"$VATPeriodReference${pcipalInitialValues.increment}" -> pcipalInitialValues.vatPeriodReference)
        else JsObject.empty
      val vatRemittanceTypeJs  =
        if pcipalInitialValues.vatRemittanceType.isDefined then
          Json.obj(s"$VATRemittanceType${pcipalInitialValues.increment}" -> pcipalInitialValues.vatRemittanceType)
        else JsObject.empty

      Json.obj(s"$ClientID${pcipalInitialValues.increment}" -> pcipalInitialValues.clientId) ++
        Json.obj(s"$PID${pcipalInitialValues.increment}" -> pcipalInitialValues.pid) ++
        Json.obj(s"$AccountOfficeID${pcipalInitialValues.increment}" -> pcipalInitialValues.accountOfficeId) ++
        Json.obj(s"$HODIdentifier${pcipalInitialValues.increment}" -> pcipalInitialValues.HODIdentifier) ++
        Json.obj(s"$UTRReference${pcipalInitialValues.increment}" -> pcipalInitialValues.UTRReference) ++
        Json.obj(s"$Name1${pcipalInitialValues.increment}" -> pcipalInitialValues.name1) ++
        Json.obj(s"$Amount${pcipalInitialValues.increment}" -> pcipalInitialValues.amount) ++
        taxAmountJs ++
        nicAmountJs ++
        lnpClass2Js ++
        nirRateJs ++
        startDateJs ++
        endDateJs ++
        vatPeriodReferenceJs ++
        vatRemittanceTypeJs ++
        Json.obj(s"$PaymentItemID${pcipalInitialValues.increment}" -> pcipalInitialValues.paymentItemId) ++
        Json.obj(s"$ChargeReference${pcipalInitialValues.increment}" -> pcipalInitialValues.chargeReference) ++
        Json.obj(s"$TaxRegimeDisplay${pcipalInitialValues.increment}" -> pcipalInitialValues.taxRegimeDisplay) ++
        Json.obj(s"$ReferenceDisplay${pcipalInitialValues.increment}" -> pcipalInitialValues.reference)

  given pcipalSessionLaunchRequest: OWrites[PcipalSessionLaunchRequest] =
    new OWrites[PcipalSessionLaunchRequest]:
      def writes(pcipalSessionLaunchRequest: PcipalSessionLaunchRequest): JsObject = Json.obj(
        "FlowId"        -> pcipalSessionLaunchRequest.FlowId,
        "InitialValues" -> createInitialValues(pcipalSessionLaunchRequest)
      )

  private def createInitialValues(pcipalSessionLaunchRequest: PcipalSessionLaunchRequest) =

    val endValues = Json.obj(
      PcipalInitialValues.UTRBlacklistFlag    -> pcipalSessionLaunchRequest.UTRBlacklistFlag,
      PcipalInitialValues.postcodeFlag        -> pcipalSessionLaunchRequest.postcodeFlag,
      PcipalInitialValues.taxRegime           -> pcipalSessionLaunchRequest.taxRegime,
      PcipalInitialValues.TotalTaxAmountToPay -> pcipalSessionLaunchRequest.TotalTaxAmountToPay,
      PcipalInitialValues.callbackUrl         -> pcipalSessionLaunchRequest.callbackUrl,
      PcipalInitialValues.backURL             -> pcipalSessionLaunchRequest.backUrl,
      PcipalInitialValues.resetURL            -> pcipalSessionLaunchRequest.resetUrl,
      PcipalInitialValues.finishURL           -> pcipalSessionLaunchRequest.finishUrl,
      PcipalInitialValues.LanguageFlag        -> pcipalSessionLaunchRequest.LanguageFlag
    )

    // Merge all the pairs of values in the list together plus the values from the object above to create one big list of pairs
    val fullSet =
      pcipalSessionLaunchRequest.InitialValues.foldLeft(Json.obj())(_ deepMerge Json.toJsObject(_) deepMerge endValues)

    JsObject(fullSet.fields.sortBy(x => sortDecoder(x._1)))

  // Sort by the right most character which will be 1 to 5 then attach another character so that the sort order is 11 to 516, or 950- if it is one of the ending values
  private def sortDecoder(key: String): String =
    key match
      case x if x.contains(s"${PcipalInitialValues.ClientID}")            => s"${x.takeRight(1)}1"
      case x if x.contains(s"${PcipalInitialValues.PID}")                 => s"${x.takeRight(1)}2"
      case x if x.contains(s"${PcipalInitialValues.AccountOfficeID}")     => s"${x.takeRight(1)}3"
      case x if x.contains(s"${PcipalInitialValues.HODIdentifier}")       => s"${x.takeRight(1)}4"
      case x if x.contains(s"${PcipalInitialValues.UTRReference}")        => s"${x.takeRight(1)}5"
      case x if x.contains(s"${PcipalInitialValues.Name1}")               => s"${x.takeRight(1)}6"
      case x if x.contains(s"${PcipalInitialValues.Amount}")              => s"${x.takeRight(1)}7"
      case x if x.contains(s"${PcipalInitialValues.TaxAmount}")           => s"${x.takeRight(1)}8"
      case x if x.contains(s"${PcipalInitialValues.NICAmount}")           => s"${x.takeRight(1)}9"
      case x if x.contains(s"${PcipalInitialValues.LNPClass2}")           => s"${x.takeRight(1)}10"
      case x if x.contains(s"${PcipalInitialValues.NIRSRate}")            => s"${x.takeRight(1)}11"
      case x if x.contains(s"${PcipalInitialValues.StartDate}")           => s"${x.takeRight(1)}12"
      case x if x.contains(s"${PcipalInitialValues.EndDate}")             => s"${x.takeRight(1)}13"
      case x if x.contains(s"${PcipalInitialValues.VATPeriodReference}")  => s"${x.takeRight(1)}14"
      case x if x.contains(s"${PcipalInitialValues.VATRemittanceType}")   => s"${x.takeRight(1)}15"
      case x if x.contains(s"${PcipalInitialValues.PaymentItemID}")       => s"${x.takeRight(1)}16"
      case x if x.contains(s"${PcipalInitialValues.ChargeReference}")     => s"${x.takeRight(1)}17"
      case x if x.contains(s"${PcipalInitialValues.TaxRegimeDisplay}")    => s"${x.takeRight(1)}18"
      case x if x.contains(s"${PcipalInitialValues.ReferenceDisplay}")    => s"${x.takeRight(1)}19"
      case x if x.contains(s"${PcipalInitialValues.UTRBlacklistFlag}")    => "950"
      case x if x.contains(s"${PcipalInitialValues.postcodeFlag}")        => "951"
      case x if x.contains(s"${PcipalInitialValues.taxRegime}")           => "952"
      case x if x.contains(s"${PcipalInitialValues.TotalTaxAmountToPay}") => "953"
      case x if x.contains(s"${PcipalInitialValues.callbackUrl}")         => "954"
      case _                                                              => "957"
