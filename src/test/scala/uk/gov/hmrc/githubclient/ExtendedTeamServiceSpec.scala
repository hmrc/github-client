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

import java.time.LocalDate
import java.util.Date

import org.eclipse.egit.github.core.client.{GitHubRequest, GitHubResponse}
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ExtendedTeamServiceSpec
  extends AnyWordSpecLike
     with Matchers
     with MockitoSugar
     with ArgumentMatchersSugar {

  "ExtendedTeamServiceSpec.getExtendedRepositories" should {
    "return the expected repositories including archived status" in {
      val nowDate         = new Date()
      val fiveDaysAgo     = LocalDate.now().minusDays(5).toEpochDay
      val fiveDaysAgoDate = new Date(fiveDaysAgo)

      val repository = ExtendedRepository(
        name        = "repoA",
        description = "some desc",
        id          = 1,
        htmlUrl     = "http://some/html/url",
        fork        = true,
        createdAt   = fiveDaysAgoDate,
        pushedAt    = nowDate,
        `private`   = true,
        language    = "Scala",
        archived    = true
      )

      val response: GitHubResponse = mock[GitHubResponse]
      when(response.getBody)
        .thenReturn(repository)
      // mock is returning `""` rather than `null` when not defined. Without this, it will go into an infinite loop, requesting the next page of results
      when(response.getNext)
        .thenReturn(null)

      val githubClient = mock[ExtendedGitHubClient]
      when(githubClient.get(any[GitHubRequest]))
        .thenReturn(response)

      val repositoryService = new ExtendedTeamService(githubClient)
      val repositoriesResponse = repositoryService.getExtendedRepositories(1)

      val expectedRepos = List(repository)
      repositoriesResponse shouldBe expectedRepos
    }
  }
}
