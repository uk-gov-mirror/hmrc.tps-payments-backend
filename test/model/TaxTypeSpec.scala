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

package model

import play.api.libs.json.{JsString, Json}
import testsupport.Givens.canEqualJsValue
import testsupport.UnitSpec
import tps.model.TaxTypes.*
import tps.model.*
import tps.model.TpsNativeTaxTypes.VatIossAndOss

class TaxTypeSpec extends UnitSpec {

  "Native tax types" in {
    val expectedUsedInFrontendInCorrectOrder: Seq[TaxType] =
      Seq[TaxType](ChildBenefitsRepayments, Sa, Sdlt, Safe, Cotax, Ntc, Paye, Nps, Vat, VatIossAndOss, Ppt)
    TaxTypes.usedOnFrontend shouldBe expectedUsedInFrontendInCorrectOrder
  }

  "path bind" in {

    TpsNativeTaxType.tpsNativeTaxTypePathBindable.unbind("whatever", ChildBenefitsRepayments) shouldBe "child-benefit-repayments"
    TpsNativeTaxType.tpsNativeTaxTypePathBindable.unbind("whatever", Cotax) shouldBe "corporation-tax"
    TpsNativeTaxType.tpsNativeTaxTypePathBindable.unbind("whatever", Nps) shouldBe "nps"
    TpsNativeTaxType.tpsNativeTaxTypePathBindable.unbind("whatever", Ntc) shouldBe "tax-credits"
    TpsNativeTaxType.tpsNativeTaxTypePathBindable.unbind("whatever", Paye) shouldBe "paye"
    TpsNativeTaxType.tpsNativeTaxTypePathBindable.unbind("whatever", Ppt) shouldBe "plastic-packaging-tax"
    TpsNativeTaxType.tpsNativeTaxTypePathBindable.unbind("whatever", Sa) shouldBe "self-assessment"
    TpsNativeTaxType.tpsNativeTaxTypePathBindable.unbind("whatever", Safe) shouldBe "strategic-accounting-framework-environment"
    TpsNativeTaxType.tpsNativeTaxTypePathBindable.unbind("whatever", Sdlt) shouldBe "stamp-duty-land-tax"
    TpsNativeTaxType.tpsNativeTaxTypePathBindable.unbind("whatever", Vat) shouldBe "vat"
    TpsNativeTaxType.tpsNativeTaxTypePathBindable.unbind("whatever", VatIossAndOss) shouldBe "vat-ioss-and-oss"

    TaxTypes.usedOnFrontend.foreach(tt =>
      TpsNativeTaxType.tpsNativeTaxTypePathBindable.unbind("whatever", tt)
    ) withClue "all supported tax types should be able to unbind"

    TpsNativeTaxType.tpsNativeTaxTypePathBindable.bind("whatever", "child-benefit-repayments") shouldBe Right(ChildBenefitsRepayments)
    TpsNativeTaxType.tpsNativeTaxTypePathBindable.bind("whatever", "corporation-tax") shouldBe Right(Cotax)
    TpsNativeTaxType.tpsNativeTaxTypePathBindable.bind("whatever", "nps") shouldBe Right(Nps)
    TpsNativeTaxType.tpsNativeTaxTypePathBindable.bind("whatever", "tax-credits") shouldBe Right(Ntc)
    TpsNativeTaxType.tpsNativeTaxTypePathBindable.bind("whatever", "paye") shouldBe Right(Paye)
    TpsNativeTaxType.tpsNativeTaxTypePathBindable.bind("whatever", "plastic-packaging-tax") shouldBe Right(Ppt)
    TpsNativeTaxType.tpsNativeTaxTypePathBindable.bind("whatever", "self-assessment") shouldBe Right(Sa)
    TpsNativeTaxType.tpsNativeTaxTypePathBindable.bind("whatever", "strategic-accounting-framework-environment") shouldBe Right(Safe)
    TpsNativeTaxType.tpsNativeTaxTypePathBindable.bind("whatever", "stamp-duty-land-tax") shouldBe Right(Sdlt)
    TpsNativeTaxType.tpsNativeTaxTypePathBindable.bind("whatever", "vat") shouldBe Right(Vat)
    TpsNativeTaxType.tpsNativeTaxTypePathBindable.bind("whatever", "vat-ioss-and-oss") shouldBe Right(VatIossAndOss)

    TpsNativeTaxType.tpsNativeTaxTypePathBindable.bind("whatever", "notatax") shouldBe Left("could not map [notatax] to tax type [key=whatever]")

  }

  "ExternalTaxTypes vs TpsNativeTaxTypes" in {
    TaxTypes.values.toSet shouldBe (ExternalTaxTypes.values ++ TpsNativeTaxTypes.values).toSet
  }

  "json De/Serialize" in {
    Json.toJson(ChildBenefitsRepayments: TaxType) shouldBe JsString("ChildBenefitsRepayments")
    Json.toJson(Sa: TaxType) shouldBe JsString("Sa")
    Json.toJson(Sdlt: TaxType) shouldBe JsString("Sdlt")
    Json.toJson(Safe: TaxType) shouldBe JsString("Safe")
    Json.toJson(Cotax: TaxType) shouldBe JsString("Cotax")
    Json.toJson(Ntc: TaxType) shouldBe JsString("Ntc")
    Json.toJson(Paye: TaxType) shouldBe JsString("Paye")
    Json.toJson(Nps: TaxType) shouldBe JsString("Nps")
    Json.toJson(Vat: TaxType) shouldBe JsString("Vat")
    Json.toJson(VatIossAndOss: TaxType) shouldBe JsString("VatIossAndOss")
    Json.toJson(Ppt: TaxType) shouldBe JsString("Ppt")
    Json.toJson(MIB: TaxType) shouldBe JsString("MIB")
    Json.toJson(PNGR: TaxType) shouldBe JsString("PNGR")

    JsString("Sa").as[TaxType] shouldBe Sa
    JsString("Sdlt").as[TaxType] shouldBe Sdlt
    JsString("Safe").as[TaxType] shouldBe Safe
    JsString("Cotax").as[TaxType] shouldBe Cotax
    JsString("Ntc").as[TaxType] shouldBe Ntc
    JsString("Paye").as[TaxType] shouldBe Paye
    JsString("Nps").as[TaxType] shouldBe Nps
    JsString("Vat").as[TaxType] shouldBe Vat
    JsString("VatIossAndOss").as[TaxType] shouldBe VatIossAndOss
    JsString("Ppt").as[TaxType] shouldBe Ppt
    JsString("MIB").as[TaxType] shouldBe MIB
    JsString("PNGR").as[TaxType] shouldBe PNGR

    TaxTypes.values.foreach { (tt: TaxType) =>
      val json = Json.toJson(tt)
      val t    = json.as[TaxType]
      tt shouldBe t withClue "tt <> json map is bidirectional"
    }
  }
}
