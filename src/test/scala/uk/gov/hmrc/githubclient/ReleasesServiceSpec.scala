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

import org.eclipse.egit.github.core.client.{GitHubClient, GitHubRequest, GitHubResponse}
import org.mockito.Matchers._
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

class ReleasesServiceSpec extends WordSpec with Matchers with MockitoSugar {

  private val githubClient: GitHubClient = mock[GitHubClient]

  private val releasesService: ReleaseService = new ReleaseService(githubClient)

  "ReleasesService.getReleases" should {

    "return true if it contains content at the given path" in {

      val response: GitHubResponse = mock[GitHubResponse]

      Mockito.when(githubClient.get(any[GitHubRequest])).thenReturn(response)

      releasesService.getReleases("orgA", "repoA")


      val captor = ArgumentCaptor.forClass(classOf[GitHubRequest])

      Mockito.verify(githubClient).get(captor.capture())

      captor.getValue.getUri shouldBe "/repos/orgA/repoA/releases"

    }

  }
  
  "ReleasesService.getTags" should {

    "return true if it contains content at the given path" in {

      val response: GitHubResponse = mock[GitHubResponse]

      Mockito.when(githubClient.get(any[GitHubRequest])).thenReturn(response)

      releasesService.getTags("orgA", "repoA")


      val captor = ArgumentCaptor.forClass(classOf[GitHubRequest])

      Mockito.verify(githubClient).get(captor.capture())

      captor.getValue.getUri shouldBe "/repos/orgA/repoA/tags"

    }

  }

}
