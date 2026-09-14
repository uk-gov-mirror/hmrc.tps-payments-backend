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

package model.vatiossandoss

import play.api.libs.json.JsString
import play.api.libs.json.Json.toJson
import testsupport.Givens.canEqualJsValue
import testsupport.UnitSpec
import tps.model.vatiossandoss.{IossAndOssPaymentType, IossAndOssPaymentTypes}

import scala.collection.immutable

class IossAndOssPaymentTypesSpec extends UnitSpec {

  "IossAndOssPaymentTypes" - {

    "should have the correct values" in {
      IossAndOssPaymentTypes.values should contain theSameElementsAs Seq(
        IossAndOssPaymentTypes.Ioss,
        IossAndOssPaymentTypes.Oss,
        IossAndOssPaymentTypes.ChargeReference
      )
    }

    "de/serialize IossAndOssPaymentType" in {

      val iossAndOssPaymentTypes: immutable.Seq[(String, IossAndOssPaymentType)] = List[(String, IossAndOssPaymentType)](
        "Ioss"            -> IossAndOssPaymentTypes.Ioss,
        "Oss"             -> IossAndOssPaymentTypes.Oss,
        "ChargeReference" -> IossAndOssPaymentTypes.ChargeReference
      )

      iossAndOssPaymentTypes.foreach { tt =>
        val jsValue = toJson(tt._2)
        jsValue shouldBe JsString(tt._1) withClue s"serialize ${tt.toString}"
        jsValue.as[IossAndOssPaymentType] shouldBe tt._2 withClue s"deserialize ${tt.toString}"
      }
    }
  }

}
