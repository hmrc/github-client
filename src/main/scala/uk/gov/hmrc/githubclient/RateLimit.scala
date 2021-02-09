/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.githubclient

import scala.concurrent.{ExecutionContext, Future}
import org.slf4j.{Logger, LoggerFactory}

private object RateLimit {
  val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  def isRateLimit(e: Throwable): Boolean =
    e.getMessage.toLowerCase.contains("api rate limit exceeded")

  implicit class RateLimitOps[T](theFuture: Future[T]) {
    def checkForApiRateLimitError(implicit executionContext: ExecutionContext): Future[T] =
      theFuture.recoverWith {
        case e if isRateLimit(e) => rateLimitError(e)
      }
  }

  def rateLimitError[T](e: Throwable): T = {
    logger.error("=== API rate limit has been reached ===", e)
    throw APIRateLimitExceededException(e)
  }
}

case class APIRateLimitExceededException(exception: Throwable) extends RuntimeException(exception)
