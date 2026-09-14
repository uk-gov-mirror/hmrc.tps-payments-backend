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

package journey

import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import play.api.libs.json.Json.toJson
import play.api.libs.json.{Format, Json, OFormat, Reads}
import repository.{Repo, RepoConfig}
import tps.journey.model.{Journey, JourneyId, JourneyState}
import tps.model.{Navigation, PaymentItem, PaymentItemId, PaymentSpecificData, TaxTypes}
import tps.pcipalmodel.{PcipalSessionId, PcipalSessionLaunchRequest, PcipalSessionLaunchResponse}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

@Singleton
final class JourneyRepo @Inject() (
  mongoComponent: MongoComponent,
  config:         RepoConfig
)(using ec: ExecutionContext)
    extends Repo[JourneyId, Journey](
      // TODO: at some point address the name of the collection. Rename it to journey, rename existing collection to journey
      collectionName = "tps-payments",
      mongoComponent = mongoComponent,
      indexes = JourneyRepo.indexes(config.expireMongo.toSeconds),
      extraCodecs = Seq.empty,
      replaceIndexes = true
    )(
      manifest = implicitly[ClassTag[Journey]],
      domainFormat = JourneyRepo.formatMongo,
      executionContext = implicitly[ExecutionContext]
    ) {

  def findByPaymentItemId(id: PaymentItemId): Future[List[Journey]] =
    find(JourneyRepo.`payments.paymentItemId` -> id)

  def getPayment(journeyId: JourneyId): Future[Journey] = findById(journeyId).map {
    case Some(tpsPayment) => tpsPayment
    case None             => throw new RuntimeException(s"Record with id ${journeyId.value} not found")
  }

  def findByPcipalSessionId(id: PcipalSessionId): Future[List[Journey]] =
    find(JourneyRepo.`pcipalSessionLaunchResponse.Id` -> id.value)

  def surfaceModsDataForRecon(modsReferences: List[String]): Future[List[PaymentSpecificData]] =
    find(JourneyRepo.`payments.chargeReference` -> Json.obj("$in" -> toJson(modsReferences)))
      .map { listOfPayments =>
        listOfPayments
          .flatMap { tpsPayments =>
            tpsPayments.payments
              .filter(_.taxType == TaxTypes.MIB)
              .map { tpsPaymentItem =>
                tpsPaymentItem.paymentSpecificData
              }
          }
      }

  def findBySearchTag(references: Seq[String]): Future[Seq[Journey]] =
    find(JourneyRepo.`payments.searchTag` -> Json.obj("$in" -> toJson(references)))

}

object JourneyRepo {

  private val `payments.searchTag`: String             = "payments.searchTag"
  private val `payments.paymentItemId`: String         = "payments.paymentItemId"
  private val `pcipalSessionLaunchResponse.Id`: String = "pcipalSessionLaunchResponse.Id"
  private val `payments.chargeReference`: String       = "payments.chargeReference"

  def indexes(cacheTtlInSeconds: Long): Seq[IndexModel] = Seq(
    IndexModel(
      keys = Indexes.ascending("created"),
      indexOptions = IndexOptions().expireAfter(cacheTtlInSeconds, TimeUnit.SECONDS).name("createdIdx")
    ),
    IndexModel(
      keys = Indexes.ascending("pciPalSessionId"),
      indexOptions = IndexOptions().name("pciPalSessionId")
    ),
    IndexModel(
      keys = Indexes.ascending(`payments.paymentItemId`),
      indexOptions = IndexOptions().name("paymentItemIdIdx")
    ),
    IndexModel(
      keys = Indexes.ascending(`pcipalSessionLaunchResponse.Id`),
      indexOptions = IndexOptions().name("pcipalSessionLaunchResponseIdIdx")
    ),
    IndexModel(
      keys = Indexes.ascending(`payments.chargeReference`),
      indexOptions = IndexOptions().name("chargeReferenceIdx")
    ),
    IndexModel(
      keys = Indexes.ascending(`payments.searchTag`),
      indexOptions = IndexOptions().name("searchTagIdx")
    )
  )

  final case class LegacyJourney(
    _id:                         JourneyId,
    pid:                         String,
    journeyState:                Option[JourneyState], // HERE the difference, recently added this state
    created:                     Instant,
    payments:                    List[
      PaymentItem
    ], // note that field is in mongo query, don't refactor wisely making sure historical records are also updated
    navigation:                  Option[Navigation],   // HERE the difference
    pcipalSessionLaunchRequest:  Option[PcipalSessionLaunchRequest] = None,
    pcipalSessionLaunchResponse: Option[PcipalSessionLaunchResponse] = None
  )

  /** This format stores date time in mongo specific way. For example: {{{"\$date":{"\$numberLong":"2837003631880"}}}} Don't change it. Use
    * https://www.epochconverter.com/ to quickly decode Long to Instant.
    */
  given formatMongo: OFormat[Journey] = {
    // before OPS-9461 "created" was stored as string and in java.time.LocalDateTime format
    // TODO: Delete this legacy reads in 2024
    val legacyCreatedReads: Reads[Instant] =
      Reads.of[String].map(s => LocalDateTime.parse(s) toInstant (ZoneOffset.UTC))

    given instantFormatSupportingLegacyReads: Format[Instant] = Format(
      MongoJavatimeFormats.instantReads.orElse(legacyCreatedReads),
      MongoJavatimeFormats.instantWrites
    )

    val dummyNavigation = Navigation("dummy", "dummy", "dummy", "dummy")

    val journeyReads: Reads[Journey] = Json
      .reads[LegacyJourney]
      .map[Journey](lg =>
        Journey(
          _id = lg._id,
          journeyState = lg.journeyState.getOrElse(JourneyState.ReceivedNotification),
          pid = lg.pid,
          created = lg.created,
          payments = lg.payments,
          navigation = lg.navigation.getOrElse(dummyNavigation),
          pcipalSessionLaunchRequest = lg.pcipalSessionLaunchRequest,
          pcipalSessionLaunchResponse = lg.pcipalSessionLaunchResponse
        )
      )
    OFormat[Journey](journeyReads, Json.writes[Journey])
  }
}
