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

package deniedrefs

import com.mongodb.client.model.Sorts
import deniedrefs.model.{DeniedRefs, DeniedRefsId}
import org.bson.codecs.Codec
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes, Projections}
import play.api.libs.json.{JsDefined, JsObject, JsString}
import repository.Repo
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

object DeniedRefsRepo:
  def indexes(): Seq[IndexModel] = Seq(
    IndexModel(
      keys = Indexes.ascending("inserted"),
      indexOptions = IndexOptions().unique(true).name("inserted")
    )
  )

  import play.api.libs.json._

  private val jsFormat: OFormat[JsObject] = new OFormat[JsObject]:
    override def reads(json: JsValue): JsResult[JsObject] = json match
      case obj: JsObject => JsSuccess(obj)
      case _: JsValue    => JsError("Invalid JSON format for JsObject")

    override def writes(o: JsObject): JsObject = o

  val jsObjectCodec: Codec[JsObject] = Codecs.playFormatCodec(jsFormat)

@Singleton
final class DeniedRefsRepo @Inject() (
  mongoComponent: MongoComponent
)(using ec: ExecutionContext)
    extends Repo[DeniedRefsId, DeniedRefs](
      collectionName = "denied-refs",
      mongoComponent = mongoComponent,
      indexes = DeniedRefsRepo.indexes(),
      extraCodecs = Seq(DeniedRefsRepo.jsObjectCodec),
      replaceIndexes = true
    ):

  def findLatestDeniedRefsId(): Future[Option[DeniedRefsId]] =
    // TODO: could be less boilerplate implementation
    findLatestDeniedRefsIdJson()
      .map(_.map(_ \ "_id" match {
        case JsDefined(JsString(value)) => DeniedRefsId(value)
        case other                      => throw new RuntimeException(s"Denied refs returns no '_id' field: ${other.toString}")
      }))

  /** Projection is used (i.e. slice("_id", 1) ) to limit the number of records returned to just one. Projection is also used (i.e. slice("refs", 1) ) to limit
    * the number of refs returned to just one. We don't need them and it can introduce performance issue if there are lots in list of refs inside DeniedRefs
    * Don't remove this... unless you know what you're doing ;)
    */
  private[deniedrefs] def findLatestDeniedRefsIdJson(): Future[Option[JsObject]] = collection
    .find[JsObject]()
    .projection(Projections.include("_id"))
    .sort(Sorts.descending(inserted))
    .headOption()

  private lazy val inserted = "inserted"
