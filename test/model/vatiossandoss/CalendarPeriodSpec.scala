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
import testsupport.UnitSpec
import tps.model.vatiossandoss.CalendarPeriod

class CalendarPeriodSpec extends UnitSpec {

  "CalendarPeriod should de/serialise from json" in {
    val jsValue = Json.toJson(CalendarPeriod(1, 2027))
    jsValue shouldEqual Json.parse("""{"month":1,"year":2027}""") withClue "serialize"
    jsValue.as[CalendarPeriod] shouldEqual CalendarPeriod(1, 2027) withClue "deserialize"
  }

  "as reference suffix" in {
    CalendarPeriod(1, 2027).asReferenceSuffix shouldBe "0127"
    CalendarPeriod(2, 2027).asReferenceSuffix shouldBe "0227"
    CalendarPeriod(10, 2027).asReferenceSuffix shouldBe "1027"
    CalendarPeriod(12, 2027).asReferenceSuffix shouldBe "1227"

    CalendarPeriod(1, 27).asReferenceSuffix shouldBe "0127"
    CalendarPeriod(2, 27).asReferenceSuffix shouldBe "0227"
    CalendarPeriod(10, 27).asReferenceSuffix shouldBe "1027"
    CalendarPeriod(12, 27).asReferenceSuffix shouldBe "1227"
  }

  "year 2 digits" in {
    CalendarPeriod(1, 2027).year2Digit shouldBe "27"
    CalendarPeriod(1, 27).year2Digit shouldBe "27"
    CalendarPeriod(1, 2).year2Digit shouldBe "02"
  }

  "month 2 digits" in {
    CalendarPeriod(1, 2027).month2Digits shouldBe "01"
    CalendarPeriod(12, 2027).month2Digits shouldBe "12"
  }
}
