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

package tps.model.vatiossandoss

import enumeratum.{Enum, EnumEntry}
import play.api.libs.json.{Format, Json}
import CalendarQuarter.*
import tps.utils.EnumFormat

import java.time.Month.*
import java.time.{LocalDate, Month}
import scala.collection.immutable

final case class CalendarQuarterlyPeriod(quarter: CalendarQuarter, year: Int) derives CanEqual {
  def previousPeriod: CalendarQuarterlyPeriod = quarter match {
    case JanuaryToMarch => CalendarQuarterlyPeriod(OctoberToDecember, year - 1)
    case _              => CalendarQuarterlyPeriod(quarter.previousTaxQuarter, year)
  }

  def isAfter(other: CalendarQuarterlyPeriod): Boolean =
    (year > other.year) || (year == other.year && quarter.intValue > other.quarter.intValue)

  def isBefore(other: CalendarQuarterlyPeriod): Boolean = this != other && !isAfter(other)

  def periodCode = s"${quarter.intValue.toString}${year.toString.takeRight(2)}"
}

object CalendarQuarterlyPeriod {

  given javaTimeMonthCanEqual: CanEqual[java.time.Month, java.time.Month] = CanEqual.derived

  given format: Format[CalendarQuarterlyPeriod] = Json.format

  def apply(date: LocalDate): CalendarQuarterlyPeriod = date.getMonth match {
    case JANUARY | FEBRUARY | MARCH    => CalendarQuarterlyPeriod(JanuaryToMarch, date.getYear)
    case APRIL | MAY | JUNE            => CalendarQuarterlyPeriod(AprilToJune, date.getYear)
    case JULY | AUGUST | SEPTEMBER     => CalendarQuarterlyPeriod(JulyToSeptember, date.getYear)
    case OCTOBER | NOVEMBER | DECEMBER => CalendarQuarterlyPeriod(OctoberToDecember, date.getYear)
  }
}

sealed abstract class CalendarQuarter private (val intValue: Int, val startMonth: Month, val endMonth: Month) extends EnumEntry derives CanEqual {
  final def nextTaxQuarter: CalendarQuarter = {
    val cycledIndex = if (intValue == 4) 1 else intValue + 1
    CalendarQuarter.calendarQuarters
      .find(_.intValue == cycledIndex)
      .getOrElse(
        throw new RuntimeException(
          s"Trying to get next quarter to ${this.toString} but cycledIndex ${cycledIndex.toString} was invalid."
        )
      )
  }

  final def previousTaxQuarter: CalendarQuarter = {
    val cycledIndex = if (intValue == 1) 4 else intValue - 1
    CalendarQuarter.calendarQuarters
      .find(_.intValue == cycledIndex)
      .getOrElse(
        throw new RuntimeException(
          s"Trying to get previous quarter to ${this.toString} but cycledIndex ${cycledIndex.toString} was invalid."
        )
      )
  }
}

object CalendarQuarter extends Enum[CalendarQuarter] {
  case object JanuaryToMarch    extends CalendarQuarter(1, JANUARY, MARCH)
  case object AprilToJune       extends CalendarQuarter(2, APRIL, JUNE)
  case object JulyToSeptember   extends CalendarQuarter(3, JULY, SEPTEMBER)
  case object OctoberToDecember extends CalendarQuarter(4, OCTOBER, DECEMBER)

  def values: immutable.IndexedSeq[CalendarQuarter] = findValues
  def calendarQuarters: List[CalendarQuarter]       = values.toList

  given javaTimeMonthCanEqual: CanEqual[java.time.Month, java.time.Month] = CanEqual.derived

  def apply(date: LocalDate): CalendarQuarter = date.getMonth match {
    case JANUARY | FEBRUARY | MARCH    => JanuaryToMarch
    case APRIL | MAY | JUNE            => AprilToJune
    case JULY | AUGUST | SEPTEMBER     => JulyToSeptember
    case OCTOBER | NOVEMBER | DECEMBER => OctoberToDecember
  }

  given format: Format[CalendarQuarter] = EnumFormat(CalendarQuarter)
}
