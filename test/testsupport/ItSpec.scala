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

/*
 * Copyright 2019 HM Revenue & Customs
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

import com.google.inject.{AbstractModule, Provides}
import journey.{JourneyRepo, JourneyService}
import journeysupport.{TestJourneyIdGenerator, TestPaymentItemIdGenerator}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.Injector
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.test.{DefaultTestServerFactory, TestServerFactory}
import play.api.{Application, Mode}
import play.core.server.ServerConfig
import tps.journey.model.{JourneyIdGenerator, PaymentItemIdGenerator}
import tps.testdata.TdAll
import uk.gov.hmrc.http.test.HttpClientV2Support

import java.time.{Clock, Instant, ZoneId}
import javax.inject.Singleton
import scala.annotation.nowarn
import scala.concurrent.ExecutionContext

/** This is common spec for every test case which brings all of useful routines we want to use in our scenarios.
  */

trait ItSpec extends AnyFreeSpecLike with RichMatchers with HttpClientV2Support with WireMockSupport with GuiceOneServerPerSuite {

  val testPort = 19001

  implicit lazy val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(1115, Seconds)), interval = scaled(Span(300, Millis)))

  lazy val frozenInstant: Instant = TdAll.instant

  val clock: Clock = Clock.fixed(frozenInstant, ZoneId.of("UTC"))

  private val module: AbstractModule = new AbstractModule:
    override def configure(): Unit =
      bind(classOf[Clock]).toInstance(clock)

    /** This one is randomised every time new test application is spawned. Thanks to that there will be no collisions in database when 2 tests insert journey.
      */
    @Provides
    @Singleton
    @nowarn // silence "method never used" warning
    def journeyIdGenerator(testJourneyIdGenerator: TestJourneyIdGenerator): JourneyIdGenerator = testJourneyIdGenerator

    @Provides
    @Singleton
    @nowarn // silence "method never used" warning
    def testJourneyIdGenerator(): TestJourneyIdGenerator = new TestJourneyIdGenerator()

    @Provides
    @Singleton
    @nowarn // silence "method never used" warning
    def paymentItemIdGenerator(testPaymentItemIdGenerator: TestPaymentItemIdGenerator): PaymentItemIdGenerator =
      testPaymentItemIdGenerator

    @Provides
    @Singleton
    @nowarn // silence "method never used" warning
    def testPaymentItemIdGenerator(): TestPaymentItemIdGenerator = new TestPaymentItemIdGenerator()

  lazy val overrideModules: List[GuiceableModule] = List()

  protected lazy val configOverrides: Map[String, Any] = Map()

  private val configMap: Map[String, Any] = Map[String, Any](
    "mongodb.uri"                                         -> "mongodb://localhost:27017/tps-payments-backend-it",
    "microservice.services.auth.port"                     -> WireMockSupport.port,
    "microservice.services.email.host"                    -> "localhost",
    "microservice.services.email.port"                    -> WireMockSupport.port,
    "microservice.services.tps-payments-backend.protocol" -> "http",
    "microservice.services.tps-payments-backend.host"     -> "localhost",
    "microservice.services.tps-payments-backend.port"     -> testPort,
    "paymentNotificationUrl"                              -> "http://notification.host/payments/notifications/send-card-payments"
  ) ++ configOverrides

  lazy val injector: Injector             = app.injector
  lazy val journeyRepo: JourneyRepo       = app.injector.instanceOf[JourneyRepo]
  lazy val journeyService: JourneyService = app.injector.instanceOf[JourneyService]

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .overrides((GuiceableModule.fromGuiceModules(Seq(module)) :: overrideModules): _*)
    .configure(configMap)
    .build()

  override def beforeEach(): Unit =
    super.beforeEach()
    journeyRepo.drop().futureValue(Timeout(Span(10, Seconds)))
    ()

  override protected def testServerFactory: TestServerFactory = CustomTestServerFactory

  object CustomTestServerFactory extends DefaultTestServerFactory:
    override protected def serverConfig(app: Application): ServerConfig =
      val sc = ServerConfig(port = Some(testPort), sslPort = None, mode = Mode.Test, rootDir = app.path)
      sc.copy(configuration = sc.configuration.withFallback(overrideServerConfiguration(app)))

}
