/*
 * Copyright 2017 HM Revenue & Customs
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
import scala.util.{Failure, Success}

trait GithubClientMetrics {

  def metricName: String

  def increment(name: String): Unit

  def withCounter[T](name: String)(f: Future[T])(implicit ec: ExecutionContext): Future[T] = {
    f.andThen {
      case Success(_) =>
        increment(s"$metricName.$name.success")
      case Failure(_) =>
        increment(s"$metricName.$name.failure")
    }
  }
}

object DefaultGithubClientMetrics extends GithubClientMetrics {
  override def metricName: String = ""
  override def increment(name: String): Unit = {}
}
