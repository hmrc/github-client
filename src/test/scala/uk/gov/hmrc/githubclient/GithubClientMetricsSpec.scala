/*
 * Copyright 2020 HM Revenue & Customs
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

import java.io.IOException

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{Matchers, WordSpec}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GithubClientMetricsSpec extends WordSpec with ScalaFutures with Matchers with IntegrationPatience {

  "GithubClientMetrics" should {

    "record successes" in new TestCase {
      metrics
        .withCounter("/repos/my-repository") {
          Future { 1 } // performs the successful operation of returning 1
        }
        .futureValue

      capturedMetrics should contain {
        "test.repos.success" -> 1
      }
    }

    "record failures" in new TestCase {
      intercept[Exception] {
        metrics
          .withCounter("/repos/my-repository") {
            Future { throw new IOException("API rate limit exceeded for service-platform-operations.") }
          }
          .futureValue
      }

      capturedMetrics should contain {
        "test.repos.failure" -> 1
      }
    }

    "parse the github enterprise path" in new TestCase {

      metrics.withCounter("/api/v3/repos/my-repository") { Future { 1 } }.futureValue

      capturedMetrics should contain {
        "test.repos.success" -> 1
      }
    }
    "parse the github.com path" in new TestCase {

      metrics.withCounter("/repos/my-repository") { Future { 1 } }.futureValue

      capturedMetrics should contain {
        "test.repos.success" -> 1
      }
    }
  }

}

trait TestCase {
  var capturedMetrics: mutable.Map[String, Int] = mutable.Map.empty
  val metrics = new GithubClientMetrics {
    override def metricName: String = "test"

    override def increment(name: String): Unit = capturedMetrics.put(name, capturedMetrics.getOrElse(name, 0) + 1)
  }
}
