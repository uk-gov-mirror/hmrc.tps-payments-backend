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
import tps.model.{HeadOfDutyIndicator, HeadOfDutyIndicators}

class HeadOfDutyIndicatorsSpec extends UnitSpec {

  "de/serialize headOfDutyIndicators" in {

    val headOfDutyIndicators: List[(String, HeadOfDutyIndicator)] = List[(String, HeadOfDutyIndicator)](
      "B" -> HeadOfDutyIndicators.B,
      "A" -> HeadOfDutyIndicators.A,
      "N" -> HeadOfDutyIndicators.N,
      "M" -> HeadOfDutyIndicators.M,
      "J" -> HeadOfDutyIndicators.J,
      "P" -> HeadOfDutyIndicators.P,
      "V" -> HeadOfDutyIndicators.V,
      "X" -> HeadOfDutyIndicators.X,
      "K" -> HeadOfDutyIndicators.K,
      "C" -> HeadOfDutyIndicators.C,
      "D" -> HeadOfDutyIndicators.D,
      "E" -> HeadOfDutyIndicators.E,
      "F" -> HeadOfDutyIndicators.F
    )

    headOfDutyIndicators.foreach { tt =>
      val jsValue = Json.toJson(tt._2)
      jsValue shouldBe JsString(tt._1) withClue s"serialize ${tt.toString()}"
      jsValue.as[HeadOfDutyIndicator] shouldBe tt._2 withClue s"deserialize ${tt.toString()}"
    }
  }
}
