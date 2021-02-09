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

import java.util

import org.eclipse.egit.github.core.client.{GitHubClient, GitHubResponse}
import org.mockito.{ArgumentCaptor, ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ExtendedContentsServiceSpec
  extends AnyWordSpecLike
     with Matchers
     with MockitoSugar
     with ArgumentMatchersSugar {

  private val githubClient = mock[GitHubClient]

  private val contentsService = new ExtendedContentsService(githubClient)

  "ExtendedContentsService.createFile" should {
    "send the appropriate request to the github API" in {
      val response: GitHubResponse = mock[GitHubResponse]

      when(githubClient.put[GitHubResponse](eqTo("/repos/orgA/repoA/contents/conf/app.conf"), any[util.HashMap[String, String]], eqTo(null)))
        .thenReturn(response)

      contentsService.createFile("orgA", "repoA", "conf/app.conf", "contents", "message")

      val captor = ArgumentCaptor.forClass(classOf[util.HashMap[String, String]])

      verify(githubClient).put(eqTo("/repos/orgA/repoA/contents/conf/app.conf"), captor.capture(), eqTo(null))

      val params = captor.getValue

      params.get("message") shouldBe "message"
      params.get("content") shouldBe "contents"
    }
  }
}
