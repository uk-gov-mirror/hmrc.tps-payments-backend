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

import play.api.libs.json.Json
import testsupport.Givens.canEqualJsValue
import testsupport.UnitSpec
import tps.model.vatiossandoss.CalendarQuarter
import tps.model.vatiossandoss.CalendarQuarter.{AprilToJune, JanuaryToMarch, JulyToSeptember, OctoberToDecember}

import java.time.LocalDate

class CalendarQuarterSpec extends UnitSpec {

  "CalendarQuarter class methods" - {
    "nextTaxQuarter should return the next quarter, wrapping from Q4 to Q1" in {
      JanuaryToMarch.nextTaxQuarter shouldBe AprilToJune
      AprilToJune.nextTaxQuarter shouldBe JulyToSeptember
      JulyToSeptember.nextTaxQuarter shouldBe OctoberToDecember
      OctoberToDecember.nextTaxQuarter shouldBe JanuaryToMarch
    }

    "previousTaxQuarter should return the previous quarter, wrapping from Q1 to Q4" in {
      JanuaryToMarch.previousTaxQuarter shouldBe OctoberToDecember
      AprilToJune.previousTaxQuarter shouldBe JanuaryToMarch
      JulyToSeptember.previousTaxQuarter shouldBe AprilToJune
      OctoberToDecember.previousTaxQuarter shouldBe JulyToSeptember
    }
  }

  "CalendarQuarter object" - {
    "values should contain all quarters in order" in {
      CalendarQuarter.values shouldBe IndexedSeq(JanuaryToMarch, AprilToJune, JulyToSeptember, OctoberToDecember)
    }

    "calendarQuarters should contain all quarters in order as a list" in {
      CalendarQuarter.calendarQuarters shouldBe List(JanuaryToMarch, AprilToJune, JulyToSeptember, OctoberToDecember)
    }

    "apply(LocalDate) should map Jan-Mar to Q1" in {
      CalendarQuarter(LocalDate.of(2026, 1, 1)) shouldBe JanuaryToMarch
      CalendarQuarter(LocalDate.of(2026, 2, 15)) shouldBe JanuaryToMarch
      CalendarQuarter(LocalDate.of(2026, 3, 31)) shouldBe JanuaryToMarch
    }

    "apply(LocalDate) should map Apr-Jun to Q2" in {
      CalendarQuarter(LocalDate.of(2026, 4, 1)) shouldBe AprilToJune
      CalendarQuarter(LocalDate.of(2026, 5, 15)) shouldBe AprilToJune
      CalendarQuarter(LocalDate.of(2026, 6, 30)) shouldBe AprilToJune
    }

    "apply(LocalDate) should map Jul-Sep to Q3" in {
      CalendarQuarter(LocalDate.of(2026, 7, 1)) shouldBe JulyToSeptember
      CalendarQuarter(LocalDate.of(2026, 8, 15)) shouldBe JulyToSeptember
      CalendarQuarter(LocalDate.of(2026, 9, 30)) shouldBe JulyToSeptember
    }

    "apply(LocalDate) should map Oct-Dec to Q4" in {
      CalendarQuarter(LocalDate.of(2026, 10, 1)) shouldBe OctoberToDecember
      CalendarQuarter(LocalDate.of(2026, 11, 15)) shouldBe OctoberToDecember
      CalendarQuarter(LocalDate.of(2026, 12, 31)) shouldBe OctoberToDecember
    }

    "format should serialise and deserialise valid values" in {
      Json.toJson(JanuaryToMarch: CalendarQuarter) shouldBe Json.toJson("JanuaryToMarch")
      Json.toJson(AprilToJune: CalendarQuarter) shouldBe Json.toJson("AprilToJune")
      Json.toJson(JulyToSeptember: CalendarQuarter) shouldBe Json.toJson("JulyToSeptember")
      Json.toJson(OctoberToDecember: CalendarQuarter) shouldBe Json.toJson("OctoberToDecember")
      Json.fromJson[CalendarQuarter](Json.toJson("JanuaryToMarch")).get shouldBe JanuaryToMarch
      Json.fromJson[CalendarQuarter](Json.toJson("AprilToJune")).get shouldBe AprilToJune
      Json.fromJson[CalendarQuarter](Json.toJson("JulyToSeptember")).get shouldBe JulyToSeptember
      Json.fromJson[CalendarQuarter](Json.toJson("OctoberToDecember")).get shouldBe OctoberToDecember
    }

    "format should fail to deserialise invalid values" in {
      Json.fromJson[CalendarQuarter](Json.toJson("NotAQuarter")).isError shouldBe true
    }
  }
}
