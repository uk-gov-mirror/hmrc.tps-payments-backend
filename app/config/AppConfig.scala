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

package config

import play.api.Configuration

import java.net.URI
import javax.inject.{Inject, Singleton}
import scala.util.Try

@Singleton
class AppConfig @Inject() (
  val config: Configuration
):

  /** URL for sending notifications. On production it is given to PciPal so they know where to sent updates.
    */
  val paymentNotificationUrl: String = readConfigAsValidUrlString("paymentNotificationUrl")
  val tpsFrontendBaseUrl: String     = readConfigAsValidUrlString("tps-frontend-base-url")

  /** The application loads the configuration from the provided `configPath` and checks if it's a valid URL. If it's not a valid URL, an exception is thrown.
    * This exception is triggered early during the application's startup to highlight a malformed configuration, thus increasing the chances of it being
    * rectified promptly.
    */
  private def readConfigAsValidUrlString(configPath: String): String =
    val url: String = config.get[String](configPath)
    Try(URI.create(url).toURL.toString).fold[String](
      e => throw new RuntimeException(s"Invalid URL in config under [$configPath]", e),
      _ => url
    )
