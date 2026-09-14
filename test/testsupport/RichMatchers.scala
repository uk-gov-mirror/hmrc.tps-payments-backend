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

package testsupport

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.scalatest._
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters.IterableHasAsScala

object RichMatchers extends RichMatchers

trait RichMatchers
    extends Matchers
    with TryValues
    with EitherValues
    with OptionValues
    with AppendedClues
    with ScalaFutures
    with StreamlinedXml
    with Inside
    with Eventually
    with IntegrationPatience:

  /** Returns recorded by WireMock request. Asserts there was only one request made to wire mock. Use it in Connector unit tests.
    */
  def getRecordedRequest: LoggedRequest =
    val allRecordedRequests: List[LoggedRequest] = WireMock.getAllServeEvents.asScala.map(_.getRequest).toList
    allRecordedRequests should have length 1 withClue "there suppose to be only one request recorded"
    allRecordedRequests.headOption.value
