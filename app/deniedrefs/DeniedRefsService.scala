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

import org.apache.pekko.Done
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.connectors.csv.scaladsl.CsvParsing
import org.apache.pekko.stream.scaladsl.{FileIO, Keep, Sink}
import org.apache.pekko.util.ByteString
import tps.deniedrefs.model.VerifyRefStatuses._
import deniedrefs.model._
import org.mongodb.scala.result.UpdateResult
import play.api.Logger
import tps.deniedrefs.model.VerifyRefsStatus
import tps.model.Reference
import util.Crypto

import java.nio.file.{Files, Path}
import java.time.{Clock, LocalDateTime}
import java.util.concurrent.atomic.AtomicReference
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class DeniedRefsService @Inject() (
  deniedRefsRepo:        DeniedRefsRepo,
  crypto:                Crypto,
  deniedRefsIdGenerator: DeniedRefsIdGenerator,
  clock:                 Clock
)(using ec: ExecutionContext, materializer: Materializer):

  /** Upserts object into mongo. It makes sure refs are encrypted before storing in mongo.
    */
  def upsert(deniedRefs: DeniedRefs): Future[UpdateResult] =
    val deniedRefsEncryted = deniedRefs.copy(refs = deniedRefs.refs.map(ref => Reference(crypto.encrypt(ref.value))))
    deniedRefsRepo.upsert(deniedRefsEncryted)

  /** Finds the latest `DeniedRefsId`
    *
    * @return
    *   None if a csv file has not been uploaded yet Some(id) containing id of recently uploaded csv file
    */
  def findLatestDeniedRefsId(): Future[Option[DeniedRefsId]] = deniedRefsRepo.findLatestDeniedRefsId()

  def getDeniedRefs(deniedRefsId: DeniedRefsId): Future[DeniedRefs] =
    deniedRefsRepo
      .findById(deniedRefsId)
      .map(_.getOrElse(throw new RuntimeException(s"Could not find DeniedRefs by given id [${deniedRefsId.value}]")))
      .map(decryptDeniedRefs)

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  private def decryptDeniedRefs(encryptedDeniedRefs: DeniedRefs): DeniedRefs =
    val decryptionResult: List[Try[String]]    = encryptedDeniedRefs.refs
      .map(ref => Try(crypto.decrypt(ref.value)))
    val successfullyDecrypted: List[Reference] = decryptionResult.collect { case Success(ref) => Reference(ref) }
    decryptionResult.collect { case Failure(ex) =>
      logger.error(
        s"Failed to decrypt ref. Has encryption key changed? [${encryptedDeniedRefs._id.value}] [inserted:${encryptedDeniedRefs.inserted.toString}]",
        ex
      )
    }
    encryptedDeniedRefs.copy(refs = successfullyDecrypted)

  /** Given a file containing list of denied refs at `pathToDeniedRefs` this method:
    *   - parses it as CSV
    *   - unifies Refs (trims, adds missing K, uppercases)
    *   - deletes file when done
    */
  def parseDeniedRefs(pathToDeniedrefs: Path): Future[DeniedRefs] =
    val deniedRefs = FileIO
      .fromPath(pathToDeniedrefs)
      .mapMaterializedValue(_ => Done)
      .via(CsvParsing.lineScanner())
      .map(_.map(_.decodeString(ByteString.UTF_8)))
      .map(_.headOption)
      .collect { case Some(ref) => Reference(ref.toUpperCase()) }
      .toMat(Sink.collection[Reference, List[Reference]])(Keep.right[Done.type, Future[List[Reference]]])
      .mapMaterializedValue(encryptedRefsF =>
        encryptedRefsF
          .map(encryptedRefs => DeniedRefs(_id = deniedRefsIdGenerator.nextId(), refs = encryptedRefs, inserted = LocalDateTime.now(clock)))
      )
      .run()
    deniedRefs.onComplete(_ => deleteTempFile(pathToDeniedrefs))
    deniedRefs

  private def deleteTempFile(pathToCsv: Path): Unit = Future(Files.deleteIfExists(pathToCsv))
    .onComplete:
    case Success(deleted) =>
      if deleted then logger.info(s"Deleted temporary csv file with refs [${pathToCsv.toString}]")
      else logger.warn(s"Could not deleted temporary csv file with refs [${pathToCsv.toString}]")
    case Failure(ex)      => logger.warn(s"Could not deleted temporary csv file with refs [${pathToCsv.toString}]", ex)

  private val cachedDeniedRefs = new AtomicReference[Option[DeniedRefs]](None)

  def verifyRefs(refs: Set[Reference]): VerifyRefsStatus =
    cachedDeniedRefs.get() match
      case Some(cache) =>
        val anyDenied = refs.exists(cache.containsRef)
        if anyDenied then RefDenied else RefPermitted
      case None        => MissingInformation

  def updateCacheIfNeeded(): Future[Unit] =
    val cache: Option[DeniedRefs] = cachedDeniedRefs.get()
    for
      latestId: Option[DeniedRefsId] <- findLatestDeniedRefsId()
      _                              <- (cache, latestId) match
                                          case (_, None)                      =>
                                            logger.info(s"DeniedRefs cache is empty. Missing DeniedRefs in database.")
                                            Future.successful(())
                                          case (None, Some(latestId))         =>
                                            logger.info(s"DeniedRefs cache is empty. Populating it ... [${latestId.toString}]")
                                            updateCache(latestId)
                                          case (Some(cached), Some(latestId)) =>
                                            if cached._id == latestId then
                                              logger.debug(
                                                s"DeniedRefs cache is up to date [inserted:${cached.inserted.toString}] [${latestId.toString}]"
                                              )
                                              Future.successful(())
                                            else
                                              logger.info(s"DeniedRefs cache is invalid. Populating it ... [${latestId.toString}]")
                                              updateCache(latestId)
    yield ()

  private def updateCache(latestId: DeniedRefsId): Future[Unit] = getDeniedRefs(latestId).map { deniedRefs =>
    cachedDeniedRefs.set(Some(deniedRefs))
    logger.info(
      s"DeniedRefs cache updated [size:${deniedRefs.refs.size.toString}] [inserted:${deniedRefs.inserted.toString}] [${latestId.toString}]"
    )
  }

  lazy val logger: Logger = Logger(this.getClass)
