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

import enumeratum.{Enum, EnumEntry}
import play.api.libs.json.Format
import play.api.mvc.{PathBindable, QueryStringBindable}
import tps.model.TpsNativeTaxTypes.VatIossAndOss

import scala.collection.immutable

/** Marking type for taxes initiating payments in TPS.
  */
sealed trait TpsNativeTaxType extends TaxType

object TpsNativeTaxType {
  given Format[TpsNativeTaxType] = tps.utils.EnumFormat(TpsNativeTaxTypes)

  given tpsNativeTaxTypePathBindable: PathBindable[TpsNativeTaxType] =
    import TaxTypes._
    val mapping: Map[String, TpsNativeTaxType]        = Map(
      "child-benefit-repayments"                   -> ChildBenefitsRepayments,
      "corporation-tax"                            -> Cotax,
      "nps"                                        -> Nps,
      "tax-credits"                                -> Ntc,
      "paye"                                       -> Paye,
      "plastic-packaging-tax"                      -> Ppt,
      "self-assessment"                            -> Sa,
      "strategic-accounting-framework-environment" -> Safe,
      "stamp-duty-land-tax"                        -> Sdlt,
      "vat"                                        -> Vat,
      "vat-ioss-and-oss"                           -> VatIossAndOss
    )
    val mappingReverse: Map[TpsNativeTaxType, String] = mapping.map(kv => (kv._2, kv._1))
    new PathBindable[TpsNativeTaxType] {
      override def bind(key: String, value: String): Either[String, TpsNativeTaxType] =
        mapping
          .get(value)
          .fold[Either[String, TpsNativeTaxType]](Left(s"could not map [$value] to tax type [key=$key]"))(
            Right(_)
          )

      override def unbind(key: String, a: TpsNativeTaxType): String = mappingReverse(a)
    }
}

/** Marking type for taxes that initiate payments outside of TPS (currently Mib and Pngr). In these cases, TPS serves as a facade to Pci Pal and does not
  * provide functionality for displaying the basket or editing payment details.
  */
sealed trait ExternalTaxType extends TaxType

sealed trait TaxType extends EnumEntry derives CanEqual {
  def pcipalProductionClientId: String
  def pcipalTestClientId: String
  def screenValue: String
  def hod: HeadOfDutyIndicator

  def clientId(usePcipalTestSettings: Boolean): String =
    if usePcipalTestSettings then pcipalTestClientId else pcipalProductionClientId

  def asTpsNativeTaxType: TpsNativeTaxType = this match
    case t: TpsNativeTaxType => t
    case t                   => throw new RuntimeException(s"Unsupported tax type ${t.toString}")
}

object TaxType {
  given Format[TaxType]                          = tps.utils.EnumFormat(TaxTypes)
  given pathBinder: QueryStringBindable[TaxType] = tps.utils.ValueClassBinder.bindableA(_.toString)
}

object TpsNativeTaxTypes extends Enum[TpsNativeTaxType] {

  case object ChildBenefitsRepayments extends TaxType with TpsNativeTaxType {
    override val hod: HeadOfDutyIndicator         = HeadOfDutyIndicators.B
    override val pcipalProductionClientId: String = "CBCE"
    override val pcipalTestClientId: String       = "CBCE"
    override val screenValue: String              = "Repay Child Benefit overpayments"
  }

  case object Sa extends TaxType with TpsNativeTaxType {
    override val hod: HeadOfDutyIndicator         = HeadOfDutyIndicators.K
    override val pcipalProductionClientId: String = "SAPM"
    override val pcipalTestClientId: String       = "SAPD"
    override val screenValue: String              = "SA"
  }

  case object Sdlt extends TaxType with TpsNativeTaxType {
    override val hod: HeadOfDutyIndicator         = HeadOfDutyIndicators.M
    override val pcipalProductionClientId: String = "SDPL"
    override val pcipalTestClientId: String       = "SDPD"
    override val screenValue: String              = "SDLT"
  }

  case object Safe extends TaxType with TpsNativeTaxType {
    override val hod: HeadOfDutyIndicator         = HeadOfDutyIndicators.X
    override val pcipalProductionClientId: String = "SFPL"
    override val pcipalTestClientId: String       = "SFPD"
    override val screenValue: String              = "SAFE"
  }

  case object Cotax extends TaxType with TpsNativeTaxType {
    override val hod: HeadOfDutyIndicator         = HeadOfDutyIndicators.A
    override val pcipalProductionClientId: String = "COPL"
    override val pcipalTestClientId: String       = "COPD"
    override val screenValue: String              = "COTAX"
  }

  case object Ntc extends TaxType with TpsNativeTaxType {
    override val hod: HeadOfDutyIndicator         = HeadOfDutyIndicators.N
    override val pcipalProductionClientId: String = "NTPL"
    override val pcipalTestClientId: String       = "NTPD"
    override val screenValue: String              = "NTC"
  }

  case object Paye extends TaxType with TpsNativeTaxType {
    override val hod: HeadOfDutyIndicator         = HeadOfDutyIndicators.P
    override val pcipalProductionClientId: String = "PAPL"
    override val pcipalTestClientId: String       = "PAPD"
    override val screenValue: String              = "PAYE"
  }

  case object Nps extends TaxType with TpsNativeTaxType {
    override val hod: HeadOfDutyIndicator         = HeadOfDutyIndicators.J
    override val pcipalProductionClientId: String = "NPPL"
    override val pcipalTestClientId: String       = "NPPD"
    override val screenValue: String              = "NPS"
  }

  case object Vat extends TaxType with TpsNativeTaxType {
    override val hod: HeadOfDutyIndicator         = HeadOfDutyIndicators.V
    override val pcipalProductionClientId: String = "VAPM"
    override val pcipalTestClientId: String       = "VAPD"
    override val screenValue: String              = "VAT"
  }

  case object VatIossAndOss extends TaxType with TpsNativeTaxType {
    override val hod: HeadOfDutyIndicator         = HeadOfDutyIndicators.D // set to D, but in fact it can be D, E or F depending on the type of payment.
    override val pcipalProductionClientId: String = "OSSM"
    override val pcipalTestClientId: String       = "OSSM"
    override val screenValue: String              = "VAT IOSS and OSS"
  }

  private val genericPcipalProductionClientId = "TPSETMP"
  private val genericPcipalTestClientId       = "TPSP800"

  case object Ppt extends TaxType with TpsNativeTaxType {
    override val hod: HeadOfDutyIndicator         = HeadOfDutyIndicators.C
    override val pcipalProductionClientId: String = genericPcipalProductionClientId
    override val pcipalTestClientId: String       = genericPcipalTestClientId
    override val screenValue: String              = "PPT"
  }

  override val values: immutable.IndexedSeq[TpsNativeTaxType] = findValues
}

object ExternalTaxTypes extends Enum[ExternalTaxType] {

  case object MIB extends TaxType with ExternalTaxType {
    override def hod: HeadOfDutyIndicator = throw new UnsupportedOperationException(s"MIB should not be looking for its HoD")

    override val pcipalProductionClientId: String = "MBML"
    override val pcipalTestClientId: String       = "MPCE"
    override val screenValue: String              = "MIB"
  }

  case object PNGR extends TaxType with ExternalTaxType {
    override def hod: HeadOfDutyIndicator = throw new UnsupportedOperationException(s"PNGR should not be looking for its HoD")

    override val pcipalProductionClientId: String = "PSML"
    override val pcipalTestClientId: String       = "PPCE"
    override val screenValue: String              = "PNGR"
  }

  override val values: immutable.IndexedSeq[ExternalTaxType] = findValues
}
object TaxTypes extends Enum[TaxType] {

  val usedOnFrontend: Seq[TpsNativeTaxType] = TpsNativeTaxTypes.values

  val MIB: ExternalTaxTypes.MIB.type   = ExternalTaxTypes.MIB
  val PNGR: ExternalTaxTypes.PNGR.type = ExternalTaxTypes.PNGR

  val ChildBenefitsRepayments: TpsNativeTaxTypes.ChildBenefitsRepayments.type = TpsNativeTaxTypes.ChildBenefitsRepayments
  val Sa: TpsNativeTaxTypes.Sa.type                                           = TpsNativeTaxTypes.Sa
  val Sdlt: TpsNativeTaxTypes.Sdlt.type                                       = TpsNativeTaxTypes.Sdlt
  val Safe: TpsNativeTaxTypes.Safe.type                                       = TpsNativeTaxTypes.Safe
  val Cotax: TpsNativeTaxTypes.Cotax.type                                     = TpsNativeTaxTypes.Cotax
  val Ntc: TpsNativeTaxTypes.Ntc.type                                         = TpsNativeTaxTypes.Ntc
  val Paye: TpsNativeTaxTypes.Paye.type                                       = TpsNativeTaxTypes.Paye
  val Nps: TpsNativeTaxTypes.Nps.type                                         = TpsNativeTaxTypes.Nps
  val Vat: TpsNativeTaxTypes.Vat.type                                         = TpsNativeTaxTypes.Vat
  val VatIossAndOss: TpsNativeTaxTypes.VatIossAndOss.type                     = TpsNativeTaxTypes.VatIossAndOss
  val Ppt: TpsNativeTaxTypes.Ppt.type                                         = TpsNativeTaxTypes.Ppt

  override def values: IndexedSeq[TaxType] = (findValues ++ TpsNativeTaxTypes.values ++ ExternalTaxTypes.values).distinct
}
