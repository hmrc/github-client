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

import scala.collection.JavaConversions._

class ExtendedTeamServiceSpec extends WordSpec with Matchers with MockitoSugar {

  "ExtendedTeamServiceSpec.getExtendedRepositories" should {

    "return the expected repositories including archived status" in {

      val nowDate = new Date()
      val fiveDaysAgo     = LocalDate.now().minusDays(5).toEpochDay
      val fiveDaysAgoDate = new Date(fiveDaysAgo)

      val repository =
        new ExtendedRepository()
          .setName("repoA")
          .setDescription("some desc")
          .setId(1)
          .setHtmlUrl("http://some/html/url")
          .setIsFork(true)
          .setCreatedAt(fiveDaysAgoDate)
          .setPushedAt(nowDate)
          .setIsPrivate(true)
          .setLanguage("Scala")
          .setArchived(true)

      val response: GitHubResponse = mock[GitHubResponse]
      Mockito.doReturn(repository).when(response).getBody

      val githubClient = mock[ExtendedGitHubClient]
      Mockito.when(githubClient.get(any[GitHubRequest])).thenReturn(response)

      val repositoryService = new ExtendedTeamService(githubClient)
      val repositoriesResponse = repositoryService.getExtendedRepositories(1)

      val expectedRepos: java.util.List[ExtendedRepository] = List(repository)
      repositoriesResponse shouldBe expectedRepos
    }
  }
}
