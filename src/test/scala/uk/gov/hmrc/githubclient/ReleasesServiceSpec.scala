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

import org.eclipse.egit.github.core.client.{GitHubClient, GitHubRequest, GitHubResponse}
import org.mockito.{ArgumentCaptor, ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ReleasesServiceSpec
  extends AnyWordSpecLike
     with Matchers
     with MockitoSugar
     with ArgumentMatchersSugar {

  "ReleasesService.getReleases" should {
    val githubClient: GitHubClient = mock[GitHubClient]
    val releasesService: ReleaseService = new ReleaseService(githubClient)

    "return true if it contains content at the given path" in {
      val response: GitHubResponse = mock[GitHubResponse]
      when(githubClient.get(any[GitHubRequest]))
        .thenReturn(response)
      // mock is returning `""` rather than `null` when not defined. Without this, it will go into an infinite loop, requesting the next page of results
      when(response.getNext)
        .thenReturn(null)

      releasesService.getReleases("orgA", "repoA")

      val captor = ArgumentCaptor.forClass(classOf[GitHubRequest])

      verify(githubClient).get(captor.capture())

      captor.getValue.getUri shouldBe "/repos/orgA/repoA/releases"
    }
  }

  "ReleasesService.getTags" should {
    val githubClient: GitHubClient = mock[GitHubClient]
    val releasesService: ReleaseService = new ReleaseService(githubClient)

    "return true if it contains content at the given path" in {
      val response: GitHubResponse = mock[GitHubResponse]
      when(githubClient.get(any[GitHubRequest]))
        .thenReturn(response)
      // mock is returning `""` rather than `null` when not defined. Without this, it will go into an infinite loop, requesting the next page of results
      when(response.getNext)
        .thenReturn(null)

      releasesService.getTags("orgA", "repoA")

      val captor = ArgumentCaptor.forClass(classOf[GitHubRequest])

      verify(githubClient).get(captor.capture())

      captor.getValue.getUri shouldBe "/repos/orgA/repoA/tags"
    }
  }
}
