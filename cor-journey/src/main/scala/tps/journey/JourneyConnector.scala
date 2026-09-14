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

package tps.journey

import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.mvc.RequestHeader
import tps.journey.model.{Journey, JourneyId, StartJourneyResponse}
import tps.startjourneymodel.{StartJourneyRequestMib, StartJourneyRequestMibOrPngr, StartJourneyRequestPngr}
import tps.utils.HttpReadsInstances.*
import tps.utils.RequestSupport.*
import uk.gov.hmrc.http.StringContextOps
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyConnector(
  httpClient: HttpClientV2,
  baseUrl:    String
)(using ec: ExecutionContext):

  def startMibOrPngrJourney(
    startJourneyRequest: StartJourneyRequestMibOrPngr
  )(using request: RequestHeader): Future[JourneyId] =
    httpClient
      .post(url"$baseUrl/tps-payments-backend/tps-payments")
      .withBody(Json.toJson(startJourneyRequest))
      .execute[JourneyId]

  def startJourneyMib(
    startJourneyRequestMib: StartJourneyRequestMib
  )(using request: RequestHeader): Future[StartJourneyResponse] =
    httpClient
      .post(url"$baseUrl/tps-payments-backend/start-tps-journey/mib")
      .withBody(Json.toJson(startJourneyRequestMib))
      .execute[StartJourneyResponse]

  def startJourneyPngr(
    startJourneyRequestPngr: StartJourneyRequestPngr
  )(using request: RequestHeader): Future[StartJourneyResponse] =
    httpClient
      .post(url"$baseUrl/tps-payments-backend/start-tps-journey/pngr")
      .withBody(Json.toJson(startJourneyRequestPngr))
      .execute[StartJourneyResponse]

  def upsert(journey: Journey)(using request: RequestHeader): Future[Unit] =
    httpClient
      .post(url"$baseUrl/tps-payments-backend/journey")
      .withBody(Json.toJson(journey))
      .execute[Unit]

  def find(journeyId: JourneyId)(using request: RequestHeader): Future[Option[Journey]] =
    httpClient
      .get(url"$baseUrl/tps-payments-backend/journey/${journeyId.value}")
      .execute[Option[Journey]]

  @Inject()
  def this(httpClient: HttpClientV2, servicesConfig: ServicesConfig)(using ec: ExecutionContext) = this(
    httpClient,
    servicesConfig.baseUrl("tps-payments-backend")
  )
