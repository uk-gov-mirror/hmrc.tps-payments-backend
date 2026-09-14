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
import tps.model.vatiossandoss.CalendarQuarter.{AprilToJune, JanuaryToMarch, JulyToSeptember, OctoberToDecember}
import tps.model.vatiossandoss.CalendarQuarterlyPeriod

import java.time.LocalDate

class CalendarQuarterlyPeriodSpec extends UnitSpec {

  "CalendarQuarterlyPeriod" - {
    "previousPeriod" - {
      "should return previous quarter in the same year for Q2-Q4" in {
        CalendarQuarterlyPeriod(AprilToJune, 2026).previousPeriod shouldBe CalendarQuarterlyPeriod(JanuaryToMarch, 2026)
        CalendarQuarterlyPeriod(JulyToSeptember, 2026).previousPeriod shouldBe CalendarQuarterlyPeriod(AprilToJune, 2026)
        CalendarQuarterlyPeriod(OctoberToDecember, 2026).previousPeriod shouldBe CalendarQuarterlyPeriod(JulyToSeptember, 2026)
      }

      "should roll back to previous year when current quarter is Q1" in {
        CalendarQuarterlyPeriod(JanuaryToMarch, 2026).previousPeriod shouldBe CalendarQuarterlyPeriod(OctoberToDecember, 2025)
      }
    }

    "isAfter" - {
      "should return true when year is greater" in {
        CalendarQuarterlyPeriod(JanuaryToMarch, 2027).isAfter(CalendarQuarterlyPeriod(OctoberToDecember, 2026)) shouldBe true
      }

      "should return true when year is equal and quarter is later" in {
        CalendarQuarterlyPeriod(JulyToSeptember, 2026).isAfter(CalendarQuarterlyPeriod(AprilToJune, 2026)) shouldBe true
      }

      "should return false when periods are equal" in {
        CalendarQuarterlyPeriod(AprilToJune, 2026).isAfter(CalendarQuarterlyPeriod(AprilToJune, 2026)) shouldBe false
      }

      "should return false when period is earlier" in {
        CalendarQuarterlyPeriod(AprilToJune, 2026).isAfter(CalendarQuarterlyPeriod(JulyToSeptember, 2026)) shouldBe false
      }
    }

    "isBefore" - {
      "should return true when period is earlier" in {
        CalendarQuarterlyPeriod(AprilToJune, 2026).isBefore(CalendarQuarterlyPeriod(JulyToSeptember, 2026)) shouldBe true
      }

      "should return true when year is lower" in {
        CalendarQuarterlyPeriod(OctoberToDecember, 2025).isBefore(CalendarQuarterlyPeriod(JanuaryToMarch, 2026)) shouldBe true
      }

      "should return false when periods are equal" in {
        CalendarQuarterlyPeriod(AprilToJune, 2026).isBefore(CalendarQuarterlyPeriod(AprilToJune, 2026)) shouldBe false
      }

      "should return false when period is later" in {
        CalendarQuarterlyPeriod(JulyToSeptember, 2026).isBefore(CalendarQuarterlyPeriod(AprilToJune, 2026)) shouldBe false
      }
    }

    "periodCode" - {
      "should return quarter number and two digit year" in {
        CalendarQuarterlyPeriod(JanuaryToMarch, 2026).periodCode shouldBe "126"
        CalendarQuarterlyPeriod(OctoberToDecember, 1999).periodCode shouldBe "499"
      }
    }

    "json format" - {
      "should serialise and deserialise correctly" in {
        val period = CalendarQuarterlyPeriod(AprilToJune, 2026)
        Json.toJson(period) shouldBe Json.obj("quarter" -> "AprilToJune", "year" -> 2026)
        Json.fromJson[CalendarQuarterlyPeriod](Json.obj("quarter" -> "AprilToJune", "year" -> 2026)).get shouldBe period
      }
    }
  }

  "CalendarQuarterlyPeriod companion apply(LocalDate)" - {
    "should map dates in Jan-Mar to Q1" in {
      CalendarQuarterlyPeriod(LocalDate.of(2026, 1, 1)) shouldBe CalendarQuarterlyPeriod(JanuaryToMarch, 2026)
      CalendarQuarterlyPeriod(LocalDate.of(2026, 2, 15)) shouldBe CalendarQuarterlyPeriod(JanuaryToMarch, 2026)
      CalendarQuarterlyPeriod(LocalDate.of(2026, 3, 31)) shouldBe CalendarQuarterlyPeriod(JanuaryToMarch, 2026)
    }

    "should map dates in Apr-Jun to Q2" in {
      CalendarQuarterlyPeriod(LocalDate.of(2026, 4, 1)) shouldBe CalendarQuarterlyPeriod(AprilToJune, 2026)
      CalendarQuarterlyPeriod(LocalDate.of(2026, 5, 15)) shouldBe CalendarQuarterlyPeriod(AprilToJune, 2026)
      CalendarQuarterlyPeriod(LocalDate.of(2026, 6, 30)) shouldBe CalendarQuarterlyPeriod(AprilToJune, 2026)
    }

    "should map dates in Jul-Sep to Q3" in {
      CalendarQuarterlyPeriod(LocalDate.of(2026, 7, 1)) shouldBe CalendarQuarterlyPeriod(JulyToSeptember, 2026)
      CalendarQuarterlyPeriod(LocalDate.of(2026, 8, 15)) shouldBe CalendarQuarterlyPeriod(JulyToSeptember, 2026)
      CalendarQuarterlyPeriod(LocalDate.of(2026, 9, 30)) shouldBe CalendarQuarterlyPeriod(JulyToSeptember, 2026)
    }

    "should map dates in Oct-Dec to Q4" in {
      CalendarQuarterlyPeriod(LocalDate.of(2026, 10, 1)) shouldBe CalendarQuarterlyPeriod(OctoberToDecember, 2026)
      CalendarQuarterlyPeriod(LocalDate.of(2026, 11, 15)) shouldBe CalendarQuarterlyPeriod(OctoberToDecember, 2026)
      CalendarQuarterlyPeriod(LocalDate.of(2026, 12, 31)) shouldBe CalendarQuarterlyPeriod(OctoberToDecember, 2026)
    }
  }
}
