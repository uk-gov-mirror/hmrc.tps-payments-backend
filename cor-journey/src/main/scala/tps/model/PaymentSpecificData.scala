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

package tps.model

import play.api.libs.json.{Json, OFormat}
import play.api.libs.json.*
import tps.model.vatiossandoss.{CalendarPeriod, CalendarQuarterlyPeriod, IossAndOssPaymentType, IossAndOssPaymentTypes, IossOssChargeReference, IossRegistrationNumber, OssVrn}

sealed trait PaymentSpecificData {
  def getReference: String
  def getRawReference: String
  def searchTag: String
}

final case class PngrSpecificData(
  chargeReference: String
) extends PaymentSpecificData {
  override def getReference: String    = chargeReference
  override def getRawReference: String = chargeReference
  override def searchTag: String       = chargeReference
}

object PngrSpecificData {
  given format: OFormat[PngrSpecificData] = Json.format[PngrSpecificData]
}

final case class MibSpecificData(
  chargeReference:    String,
  vat:                BigDecimal,
  customs:            BigDecimal,
  amendmentReference: Option[Int] = None
) extends PaymentSpecificData {
  override def getReference: String      = chargeReference
  def getAmendmentReference: Option[Int] = amendmentReference
  override def getRawReference: String   = chargeReference
  override def searchTag: String         = chargeReference
}

object MibSpecificData {
  given format: OFormat[MibSpecificData] = Json.format[MibSpecificData]
}

final case class ChildBenefitSpecificData(
  childBenefitYReference: String
) extends PaymentSpecificData {
  override def getReference: String    = childBenefitYReference
  override def getRawReference: String = childBenefitYReference
  override def searchTag: String       = childBenefitYReference
}

object ChildBenefitSpecificData {
  given format: OFormat[ChildBenefitSpecificData] = Json.format[ChildBenefitSpecificData]
}

final case class SaSpecificData(
  saReference: String // TODO make strong type for that UTR
) extends PaymentSpecificData {
  override def getReference: String    = saReference
  override def getRawReference: String = saReference.dropRight(1)
  override def searchTag: String       = saReference.toUpperCase.replaceAll("K", "")
}

object SaSpecificData {
  given format: OFormat[SaSpecificData] = Json.format[SaSpecificData]
}

final case class SdltSpecificData(
  sdltReference: String
) extends PaymentSpecificData {
  override def getReference: String    = sdltReference
  override def getRawReference: String = sdltReference
  override def searchTag: String       = sdltReference
}

object SdltSpecificData {
  given format: OFormat[SdltSpecificData] = Json.format[SdltSpecificData]
}

final case class SafeSpecificData(
  safeReference: String
) extends PaymentSpecificData {
  override def getReference: String    = safeReference
  override def getRawReference: String = safeReference
  override def searchTag: String       = safeReference
}

object SafeSpecificData {
  given format: OFormat[SafeSpecificData] = Json.format[SafeSpecificData]
}

final case class CotaxSpecificData(
  cotaxReference: String
) extends PaymentSpecificData {
  override def getReference: String    = cotaxReference
  override def getRawReference: String = cotaxReference.dropRight(7)
  override def searchTag: String       = cotaxReference.dropRight(7)
}

object CotaxSpecificData {
  given format: OFormat[CotaxSpecificData] = Json.format[CotaxSpecificData]
}

final case class NtcSpecificData(
  ntcReference: String
) extends PaymentSpecificData {
  override def getReference: String    = ntcReference
  override def getRawReference: String = ntcReference.dropRight(8)
  override def searchTag: String       = ntcReference.dropRight(8)
}

object NtcSpecificData {
  given format: OFormat[NtcSpecificData] = Json.format[NtcSpecificData]
}

final case class PayeSpecificData(
  payeReference: String,
  taxAmount:     BigDecimal,
  nicAmount:     BigDecimal
) extends PaymentSpecificData {
  override def getReference: String    = payeReference
  override def getRawReference: String = payeReference.dropRight(4)
  override def searchTag: String       = payeReference.dropRight(4)
}

object PayeSpecificData {
  given format: OFormat[PayeSpecificData] = Json.format[PayeSpecificData]
}

final case class NpsSpecificData(
  npsReference:    String,
  periodStartDate: String,
  periodEndDate:   String,
  npsType:         String,
  rate:            BigDecimal
) extends PaymentSpecificData {
  override def getReference: String    = npsReference
  override def getRawReference: String = npsReference.dropRight(2)
  override def searchTag: String       = npsReference
}
object NpsSpecificData  {
  given format: OFormat[NpsSpecificData] = Json.format[NpsSpecificData]
}

final case class VatSpecificData(
  vatReference:   String,
  remittanceType: String // TODO make strong type, enum
) extends PaymentSpecificData {
  override def getReference: String    = vatReference
  override def getRawReference: String = vatReference.dropRight(4)
  override def searchTag: String       = vatReference
}

object VatSpecificData {
  given format: OFormat[VatSpecificData] = Json.format[VatSpecificData]
}

final case class VatIossAndOssSpecificData(
  iossAndOssPaymentType:      IossAndOssPaymentType,
  vatIossRegistrationNumber:  Option[IossRegistrationNumber],
  vatIossPeriod:              Option[CalendarPeriod],
  vatOssVrn:                  Option[OssVrn],
  vatOssPeriod:               Option[CalendarQuarterlyPeriod],
  vatIossOssPenaltyReference: Option[IossOssChargeReference]
) extends PaymentSpecificData {

  override def getReference: String = iossAndOssPaymentType match {

    case IossAndOssPaymentTypes.Ioss =>
      val reference: Option[String] = for {
        iossRef <- vatIossRegistrationNumber
        period  <- vatIossPeriod
      } yield s"${iossRef.value}M${period.asReferenceSuffix}"

      reference.getOrElse(throw new IllegalStateException("vatIossRegistrationNumber and vatIossPeriod are required for IOSS payment type"))

    case IossAndOssPaymentTypes.Oss =>
      val reference: Option[String] = for {
        vrn    <- vatOssVrn
        period <- vatOssPeriod
      } yield s"NI${vrn.value}Q${period.periodCode}"

      reference.getOrElse(throw new IllegalStateException("vatOssVrn and vatOssPeriod are required for OSS payment type"))

    case IossAndOssPaymentTypes.ChargeReference =>
      vatIossOssPenaltyReference.getOrElse(throw new IllegalStateException("vatIossOssPenaltyReference is required for ChargeReference payment type")).value
  }

  override def getRawReference: String = getReference

  override def searchTag: String = iossAndOssPaymentType match {
    case IossAndOssPaymentTypes.Ioss            =>
      vatIossRegistrationNumber.getOrElse(throw new IllegalStateException("vatIossRegistrationNumber is required for IOSS payment type")).value
    case IossAndOssPaymentTypes.Oss             =>
      vatOssVrn.getOrElse(throw new IllegalStateException("vatOssVrn is required for OSS payment type")).value
    case IossAndOssPaymentTypes.ChargeReference =>
      vatIossOssPenaltyReference.getOrElse(throw new IllegalStateException("vatIossOssPenaltyReference is required for ChargeReference payment type")).value
  }

}

object VatIossAndOssSpecificData {
  given format: OFormat[VatIossAndOssSpecificData] = Json.format[VatIossAndOssSpecificData]
}

final case class PptSpecificData(
  pptReference: String
) extends PaymentSpecificData {
  override def getReference: String    = pptReference
  override def getRawReference: String = pptReference
  override def searchTag: String       = pptReference
}

object PptSpecificData {
  given format: OFormat[PptSpecificData] = Json.format[PptSpecificData]
}

object PaymentSpecificData {

  given writes: Writes[PaymentSpecificData] = Writes[PaymentSpecificData] {
    case pngr: PngrSpecificData                               => PngrSpecificData.format.writes(pngr)
    case mib: MibSpecificData                                 => MibSpecificData.format.writes(mib)
    case childBenefitSpecificData: ChildBenefitSpecificData   =>
      ChildBenefitSpecificData.format.writes(childBenefitSpecificData)
    case sa: SaSpecificData                                   => SaSpecificData.format.writes(sa)
    case sdltSpecificData: SdltSpecificData                   => SdltSpecificData.format.writes(sdltSpecificData)
    case safeSpecificData: SafeSpecificData                   => SafeSpecificData.format.writes(safeSpecificData)
    case cotaxSpecificData: CotaxSpecificData                 => CotaxSpecificData.format.writes(cotaxSpecificData)
    case ntcSpecificData: NtcSpecificData                     => NtcSpecificData.format.writes(ntcSpecificData)
    case payeSpecificData: PayeSpecificData                   => PayeSpecificData.format.writes(payeSpecificData)
    case npsSpecificData: NpsSpecificData                     => NpsSpecificData.format.writes(npsSpecificData)
    case vatSpecificData: VatSpecificData                     => VatSpecificData.format.writes(vatSpecificData)
    case vatIossAndOssSpecificData: VatIossAndOssSpecificData => VatIossAndOssSpecificData.format.writes(vatIossAndOssSpecificData)
    case pptSpecificData: PptSpecificData                     => PptSpecificData.format.writes(pptSpecificData)
  }

  given reads: Reads[PaymentSpecificData] = Reads[PaymentSpecificData] {
    case json: JsObject if json.keys == jsonKeysPngrSpecificData                                                                             =>
      JsSuccess(json.as[PngrSpecificData])
    case json: JsObject if json.keys == jsonKeysPngrSpecificDataLegacy                                                                       =>
      JsSuccess(json.as[PngrSpecificData])
    case json: JsObject if (json.keys == jsonKeysMibSpecificDataVariant1) || (json.keys == jsonKeysMibSpecificDataVariant2)                  =>
      JsSuccess(json.as[MibSpecificData])
    case json: JsObject if json.keys == jsonKeysChildBenefit                                                                                 =>
      JsSuccess(json.as[ChildBenefitSpecificData])
    case json: JsObject if json.keys == jsonKeysSa                                                                                           =>
      JsSuccess(json.as[SaSpecificData])
    case json: JsObject if json.keys == jsonKeysSdlt                                                                                         =>
      JsSuccess(json.as[SdltSpecificData])
    case json: JsObject if json.keys == jsonKeysSafe                                                                                         =>
      JsSuccess(json.as[SafeSpecificData])
    case json: JsObject if json.keys == jsonKeysCotax                                                                                        =>
      JsSuccess(json.as[CotaxSpecificData])
    case json: JsObject if json.keys == jsonKeysNtc                                                                                          =>
      JsSuccess(json.as[NtcSpecificData])
    case json: JsObject if (json.keys == jsonKeysPayeVariant1) || (json.keys == jsonKeysPayeVariant2) || (json.keys == jsonKeysPayeVariant3) =>
      JsSuccess(json.as[PayeSpecificData])
    case json: JsObject if json.keys == jsonKeysNps                                                                                          =>
      JsSuccess(json.as[NpsSpecificData])
    case json: JsObject if json.keys == jsonKeysVat                                                                                          =>
      JsSuccess(json.as[VatSpecificData])
    case json: JsObject
        if (json.keys == jsonKeysVatIossAndOss_IOSS) || (json.keys == jsonKeysVatIossAndOss_OSS) || (json.keys ==
          jsonKeysVatIossAndOss_PENALTY) =>
      JsSuccess(json.as[VatIossAndOssSpecificData])
    case json: JsObject if json.keys == jsonKeysPpt                                                                                          =>
      JsSuccess(json.as[PptSpecificData])
    case _                                                                                                                                   =>
      JsError("Could not read PaymentSpecificData")
  }

  val jsonKeysPngrSpecificData: Set[String]        = Set("chargeReference", "vat", "customs", "excise")
  val jsonKeysPngrSpecificDataLegacy: Set[String]  = Set("chargeReference")
  val jsonKeysMibSpecificDataVariant1: Set[String] = Set("chargeReference", "vat", "customs")
  val jsonKeysMibSpecificDataVariant2: Set[String] = Set("chargeReference", "vat", "customs", "amendmentReference")
  val jsonKeysChildBenefit: Set[String]            = Set("childBenefitYReference")
  val jsonKeysSa: Set[String]                      = Set("saReference")
  val jsonKeysSdlt: Set[String]                    = Set("sdltReference")
  val jsonKeysSafe: Set[String]                    = Set("safeReference")
  val jsonKeysCotax: Set[String]                   = Set("cotaxReference")
  val jsonKeysNtc: Set[String]                     = Set("ntcReference")
  val jsonKeysPayeVariant1: Set[String]            = Set("payeReference", "taxAmount")
  val jsonKeysPayeVariant2: Set[String]            = Set("payeReference", "nicAmount")
  val jsonKeysPayeVariant3: Set[String]            = Set("payeReference", "taxAmount", "nicAmount")
  val jsonKeysNps: Set[String]                     = Set("npsReference", "periodStartDate", "periodEndDate", "npsType", "rate")
  val jsonKeysVat: Set[String]                     = Set("vatReference", "remittanceType")
  val jsonKeysVatIossAndOss_IOSS: Set[String]      = Set("iossAndOssPaymentType", "vatIossRegistrationNumber", "vatIossPeriod")
  val jsonKeysVatIossAndOss_OSS: Set[String]       = Set("iossAndOssPaymentType", "vatOssVrn", "vatOssPeriod")
  val jsonKeysVatIossAndOss_PENALTY: Set[String]   = Set("iossAndOssPaymentType", "vatIossOssPenaltyReference")
  val jsonKeysPpt: Set[String]                     = Set("pptReference")

}
