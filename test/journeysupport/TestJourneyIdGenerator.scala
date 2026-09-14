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

package journeysupport

import tps.journey.model.{JourneyId, JourneyIdGenerator}

import java.util.concurrent.atomic.AtomicReference

/** This is still a random journeyId generator which allows to see what is going to be the next journey Id. Useful in tests.
  */
class TestJourneyIdGenerator extends JourneyIdGenerator:

  private val idIterator: Iterator[JourneyId] = LazyList.from(0).map(_ => super.nextId()).iterator
  private val nextIdCached                    = new AtomicReference[JourneyId](idIterator.next())

  def predictNextId(): JourneyId =
    nextIdCached.get()

  override def nextId(): JourneyId =
    val id = idIterator.next()
    nextIdCached.getAndSet(id)
