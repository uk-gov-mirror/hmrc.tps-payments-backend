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

package repository

import org.bson.codecs.Codec
import org.bson.json.JsonObject
import org.mongodb.scala.model.{Filters, IndexModel, ReplaceOptions}
import org.mongodb.scala.result
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.*
import tps.model.repo.{HasId, Id}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

@SuppressWarnings(Array("org.wartremover.warts.Any"))
abstract class Repo[ID <: Id, A <: HasId[ID]](
  collectionName: String,
  mongoComponent: MongoComponent,
  indexes:        Seq[IndexModel],
  extraCodecs:    Seq[Codec[?]],
  replaceIndexes: Boolean = false
)(implicit manifest: ClassTag[A], domainFormat: OFormat[A], executionContext: ExecutionContext)
    extends PlayMongoRepository[A](
      mongoComponent = mongoComponent,
      collectionName = collectionName,
      domainFormat = domainFormat,
      indexes = indexes,
      replaceIndexes = replaceIndexes,
      extraCodecs = extraCodecs
    ):

  /** Update or Insert (UpSert) element `a` identified by `id`
    */
  def upsert(a: A): Future[result.UpdateResult] = collection
    .replaceOne(
      filter = Filters.eq("_id", a.id.value),
      replacement = a,
      options = ReplaceOptions().upsert(true)
    )
    .toFuture()

  def findById(id: ID): Future[Option[A]] = collection
    .find(
      filter = Filters.eq("_id", id.value)
    )
    .headOption()

  def find(query: (String, JsValueWrapper)*): Future[List[A]] = collection
    .find(
      filter = new JsonObject(Json.obj(query: _*).toString())
    )
    .toFuture()
    .map(_.toList)

  def drop(): Future[Boolean] = collection
    .drop()
    .toFuture()
    .map(_ => true)

  def countAll(): Future[Long] = collection
    .countDocuments()
    .toFuture()

  def removeAll(): Future[Unit] = collection
    .deleteMany(Filters.empty())
    .toFuture()
    .map(_ => ())
