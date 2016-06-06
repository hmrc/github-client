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

import com.github.tomakehurst.wiremock.http.RequestMethod.{GET, HEAD}
import org.eclipse.egit.github.core._
import org.eclipse.egit.github.core.service.{OrganizationService, TeamService, RepositoryService, ContentsService}
import org.mockito.Matchers.any
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{WordSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext.Implicits.global

class GithubApiClientSpec extends WordSpec with MockitoSugar with ScalaFutures with Matchers{

  val mockOrgService: OrganizationService = mock[OrganizationService]
  val mockTeamService: TeamService = mock[TeamService]
  val mockRepositoryService: RepositoryService = mock[RepositoryService]
  val mockContentsService: ContentsService = mock[ContentsService]
  val mockReleaseService: ReleaseService = mock[ReleaseService]

  def githubApiClient = new GithubApiClient {

    val orgService: OrganizationService = mockOrgService
    val teamService: TeamService = mockTeamService
    val repositoryService: RepositoryService = mockRepositoryService
    val contentsService: ContentsService = mockContentsService
    val releaseService = mockReleaseService
  }

  import scala.collection.JavaConversions._

  "GitHubAPIClient.getOrganisations" should {

    "get all organizations" in {

      val users: java.util.List[User] = List(new User().setLogin("ORG1").setId(1), new User().setLogin("ORG2").setId(2))

      Mockito.when(mockOrgService.getOrganizations).thenReturn(users)

      githubApiClient.getOrganisations.futureValue shouldBe List(GhOrganisation("ORG1", 1), GhOrganisation("ORG2", 2))

    }
  }

  "GitHubAPIClient.getTeamsForOrganisation" should {
    "get all team for organization" in {

      val teams: java.util.List[Team] = List(new Team().setName("Ateam").setId(1))

      Mockito.when(mockTeamService.getTeams("ORG1")).thenReturn(teams)

      githubApiClient.getTeamsForOrganisation("ORG1").futureValue shouldBe List(GhTeam("Ateam", 1))
    }
  }


  "GitHubAPIClient.getReposForTeam" should {
    "get all team for organization" in {

      val repos: java.util.List[Repository] = List(
        new Repository().setName("repoA").setId(1).setHtmlUrl("http://some/html/url").setFork(true)
      )

      Mockito.when(mockTeamService.getRepositories(1)).thenReturn(repos)

      githubApiClient.getReposForTeam(1).futureValue shouldBe List(GhRepository("repoA", 1, "http://some/html/url", true))
    }
  }

  "GitHubAPIClient.repoContainsContent" should {

    "return true if it contains content at the given path" in {
      val folderName = "folder"

      val contents: java.util.List[RepositoryContents] = List(
        new RepositoryContents().setPath("folder"),
        new RepositoryContents().setPath("someOtherfolder")
      )

      Mockito.when(mockContentsService.getContents(any[IRepositoryIdProvider])).thenReturn(contents)

      githubApiClient.repoContainsContent(folderName, "repoA", "OrgA").futureValue shouldBe true

      val captor = ArgumentCaptor.forClass(classOf[IRepositoryIdProvider])

      Mockito.verify(mockContentsService).getContents(captor.capture())

      captor.getValue.generateId() shouldBe "OrgA/repoA"

    }

    "return false if it does not contain content at the given path" in {
      val folderName = "folder"

      val contents: java.util.List[RepositoryContents] = List(
        new RepositoryContents().setPath("someOtherfolder")
      )

      Mockito.when(mockContentsService.getContents(any[IRepositoryIdProvider])).thenReturn(contents)

      githubApiClient.repoContainsContent(folderName, "repoA", "OrgA").futureValue shouldBe false
    }
  }
}
