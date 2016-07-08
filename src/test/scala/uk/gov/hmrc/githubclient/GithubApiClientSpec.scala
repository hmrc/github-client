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

import java.io.IOException

import org.eclipse.egit.github.core._
import org.eclipse.egit.github.core.client.RequestException
import org.eclipse.egit.github.core.service.{ContentsService, OrganizationService, RepositoryService, TeamService}
import org.mockito.Matchers.{any, same}
import org.mockito.Matchers.{eq => meq}
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GithubApiClientSpec extends WordSpec with MockitoSugar with ScalaFutures with Matchers{

  val mockOrgService: OrganizationService = mock[OrganizationService]
  val mockTeamService: ExtendedTeamService = mock[ExtendedTeamService]
  val mockRepositoryService: RepositoryService = mock[RepositoryService]
  val mockContentsService: ContentsService = mock[ContentsService]
  val mockReleaseService: ReleaseService = mock[ReleaseService]

  def githubApiClient = new GithubApiClient {

    val orgService: OrganizationService = mockOrgService
    val teamService: ExtendedTeamService = mockTeamService
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
      val path = "folder"

      val contents: java.util.List[RepositoryContents] = List(
        new RepositoryContents().setPath("folder"),
        new RepositoryContents().setPath("someOtherfolder")
      )

      Mockito.when(mockContentsService.getContents(any[IRepositoryIdProvider], same(path))).thenReturn(contents)

      githubApiClient.repoContainsContent(path, "repoA", "OrgA").futureValue shouldBe true

      val captor = ArgumentCaptor.forClass(classOf[IRepositoryIdProvider])

      Mockito.verify(mockContentsService).getContents(captor.capture(), same(path))

      captor.getValue.generateId() shouldBe "OrgA/repoA"

    }

    "return false if it does not contain content at the given path" in {
      val path = "folder"

      Mockito.when(mockContentsService.getContents(any[IRepositoryIdProvider], same(path))).thenReturn(List())

      githubApiClient.repoContainsContent(path, "repoA", "OrgA").futureValue shouldBe false
    }
  }

  "GitHubAPIClient.containsRepo" should {
    val owner = "HMRC"
    val repoName = "test-repo"

    "return true when the repo exists" in {
      val repository = new Repository().setName(repoName)

      Mockito.when(mockRepositoryService.getRepository(owner, repoName)).thenReturn(repository)

      githubApiClient.containsRepo(owner, repoName).futureValue shouldBe true
    }

    "return false when the repo does not exist" in {
      val repository = new Repository().setName(repoName)

      Mockito.when(mockRepositoryService.getRepository(owner, repoName)).thenReturn(null)

      githubApiClient.containsRepo(owner, repoName).futureValue shouldBe false
    }

    "throw exception when egit encounters an error" in {
      val repository = new Repository().setName(repoName)

      Mockito.when(mockRepositoryService.getRepository(owner, repoName)).thenThrow(new RequestException(new RequestError(), 500))

      intercept[Exception] {
        githubApiClient.containsRepo(owner, repoName).futureValue
      }
    }
  }

  "GitHubAPIClient.teamId" should {
    val organisation = "HMRC"
    val teamName = "test-team"

    "find a team ID for a team name when the team exists" in {
      val team = new Team().setId(123).setName(teamName)
      val anotherTeam = new Team().setId(321).setName("another-team")

      Mockito.when(mockTeamService.getTeams(organisation)).thenReturn(List(team, anotherTeam))

      githubApiClient.teamId(organisation, teamName).futureValue.get shouldBe 123
    }

    "return None when the team does not exist" in {
      val anotherTeam = new Team().setId(321).setName("another-team")

      Mockito.when(mockTeamService.getTeams(organisation)).thenReturn(List(anotherTeam))

      githubApiClient.teamId(organisation, teamName).futureValue shouldBe None
    }
  }

  "GitHubAPIClient.createRepo" should {
    val organisation = "HMRC"
    val repoName = "test-repo"
    val cloneUrl = s"git@github.com:hmrc/$repoName.git"

    "return the clone url for a successfully created repo" in {
      val repository = new Repository().setName(repoName).setCloneUrl(cloneUrl)

      Mockito.when(mockRepositoryService.createRepository(same(organisation), any[Repository])).thenReturn(repository)

      val createdUrl = githubApiClient.createRepo(organisation,repoName).futureValue
      createdUrl shouldBe cloneUrl
    }
  }

  "GitHubAPIClient.addRepoToTeam" should {
    val organisation = "HMRC"
    val repoName = "test-repo"

    "add a repository to a team in" in {
      githubApiClient.addRepoToTeam(organisation, repoName, 99)

      val captor = ArgumentCaptor.forClass(classOf[IRepositoryIdProvider])
      Mockito.verify(mockTeamService).addRepository(meq(99), captor.capture())

      captor.getValue.generateId() shouldBe "HMRC/test-repo"
    }
  }

  "GitHubAPIClient.createHook" should {
    val organisation = "HMRC"
    val repoName = "test-repo"
    val hookName = "jenkins"
    val config = Map("jenkins_hook_url" -> "url")

    "add the given hook to the repository" in {
      githubApiClient.createHook(organisation, repoName, hookName, config)

      val captor = ArgumentCaptor.forClass(classOf[IRepositoryIdProvider])
      val hookCapture = ArgumentCaptor.forClass(classOf[RepositoryHook])

      Mockito.verify(mockRepositoryService).createHook(captor.capture(), hookCapture.capture())

      captor.getValue.generateId() shouldBe "HMRC/test-repo"

      val hook = hookCapture.getValue
      hook.getConfig()("jenkins_hook_url") shouldBe "url"
      hook.getName shouldBe "jenkins"
      hook.isActive shouldBe true
    }
  }

}
