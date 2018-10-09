/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait GithubClientMetrics {

  def metricName: String

  def increment(name: String): Unit

  def withCounter[T](path: String)(call: Future[T])(implicit ec: ExecutionContext): Future[T] = {

    val name = path
      .replace("/api/v3", "")
      .split("/")
      .find(_.nonEmpty)
      .getOrElse("default")

    call.andThen {
      case Success(_) =>
        increment(s"$metricName.$name.success")
        Logger.trace(s"Increment metric for $metricName.$name.success, path: $path")
      case Failure(_) =>
        increment(s"$metricName.$name.failure")
        Logger.trace(s"Increment metric for $metricName.$name.failure, path: $path")
    }
  }
}

object DefaultGithubClientMetrics extends GithubClientMetrics {
  override def metricName: String = ""

  override def increment(name: String): Unit = {}
}
