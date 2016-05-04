/*
 * Copyright 2016 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.RequestMethod._
import org.scalatest.Matchers
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global


class HttpClientSpec extends WireMockSpec with ScalaFutures with Matchers with DefaultPatienceConfig {

  case class Response(success: Boolean)
  object Response {implicit val formats = Json.format[Response]}

  val httpClient = new HttpClient("", "")

  "HttpClientSpec.delete" should {
    "report success if the deleted resource is not found (i.e 404 status)" in {

      givenGitHubExpects(
        method = DELETE,
        url = s"$endpointMockUrl/resource/1",
        willRespondWith = (404, None)
      )

      noException should be thrownBy httpClient.delete(s"$endpointMockUrl/resource/1").futureValue

    }

    "report exception if correct http status is not returned" in {

      givenGitHubExpects(
        method = DELETE,
        url = s"$endpointMockUrl/resource/1",
        willRespondWith = (500, None)
      )

      a[RuntimeException] should be thrownBy httpClient.delete(s"$endpointMockUrl/resource/1").futureValue
    }


  }


  "HttpClientSpec.postWithStringResponse" should {
    "report success with string body" in {

      givenGitHubExpects(
        method = RequestMethod.POST,
        url = s"$endpointMockUrl/resource/1",
        willRespondWith = (200, Some( """{"success" : true}"""))
      )

      httpClient.postWithStringResponse(s"$endpointMockUrl/resource/1", Some( """{"b" : "a"}""")).futureValue should be( """{"success" : true}""")

      assertRequest(
        method = POST,
        url = s"$endpointMockUrl/resource/1",
        jsonBody = Some( """{"b" : "a"}""")
      )

    }

    "report exception if correct http status is not returned" in {

      givenGitHubExpects(
        method = POST,
        url = s"$endpointMockUrl/resource/1",
        willRespondWith = (500, None)
      )

      a[RuntimeException] should be thrownBy httpClient.postWithStringResponse(s"$endpointMockUrl/resource/1").futureValue
    }

  }

  "HttpClientSpec.post" should {

    "report success with string body" in {

      givenGitHubExpects(
        method = RequestMethod.POST,
        url = s"$endpointMockUrl/resource/1",
        willRespondWith = (200, Some( """{"success" : true}"""))
      )

      httpClient.post[Response](s"$endpointMockUrl/resource/1", Some( """{"b" : "a"}""")).futureValue should be(Response(true))

      assertRequest(
        method = POST,
        url = s"$endpointMockUrl/resource/1",
        jsonBody = Some( """{"b" : "a"}""")
      )

    }

    "report exception if correct http status is not returned" in {

      givenGitHubExpects(
        method = POST,
        url = s"$endpointMockUrl/resource/1",
        willRespondWith = (500, None)
      )

      a[RuntimeException] should be thrownBy httpClient.post[Response](s"$endpointMockUrl/resource/1").futureValue
    }


  }
}
