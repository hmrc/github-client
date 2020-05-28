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

import java.time.LocalDate
import java.util.Date

import org.eclipse.egit.github.core.client.{GitHubRequest, GitHubResponse}
import org.mockito.Matchers.any
import org.mockito.Mockito
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

class ExtendedRepositoryServiceSpec extends WordSpec with Matchers with MockitoSugar {

  "ExtendedRepositoryService.getOrgExtendedRepositories" should {

    "return the expected repositories including archived status from the Github API" in {

      val nowDate = new Date()
      val fiveDaysAgo     = LocalDate.now().minusDays(5).toEpochDay
      val fiveDaysAgoDate = new Date(fiveDaysAgo)

      val repository = ExtendedRepository(
        name = "repoA",
        description = "some desc",
        id = 1,
        htmlUrl = "http://some/html/url",
        fork = true,
        createdAt = fiveDaysAgoDate,
        pushedAt = nowDate,
        isPrivate = true,
        language = "Scala",
        archived = true
      )

      val response: GitHubResponse = mock[GitHubResponse]
      Mockito.doReturn(repository).when(response).getBody

      val githubClient = mock[ExtendedGitHubClient]
      Mockito.when(githubClient.get(any[GitHubRequest])).thenReturn(response)

      val repositoryService = new ExtendedRepositoryService(githubClient)
      val repositoriesResponse = repositoryService.getOrgExtendedRepositories("HMRC")

      val expectedRepos = List(repository)
      repositoriesResponse shouldBe expectedRepos
    }
  }

  "throw an exception if the organisation name is an empty string" in {
    val githubClient = mock[ExtendedGitHubClient]

    val repositoryService = new ExtendedRepositoryService(githubClient)
    val expectedError = intercept[IllegalArgumentException] {
      repositoryService.getOrgExtendedRepositories("")
    }
    expectedError.getMessage shouldBe "Organization cannot be null or empty"
  }

  "throw an exception if the organisation name is null" in {
    val githubClient = mock[ExtendedGitHubClient]

    val repositoryService = new ExtendedRepositoryService(githubClient)
    val expectedError = intercept[IllegalArgumentException] {
      repositoryService.getOrgExtendedRepositories(null)
    }
    expectedError.getMessage shouldBe "Organization cannot be null or empty"
  }
}
