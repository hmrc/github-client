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

import org.eclipse.egit.github.core.{IRepositoryIdProvider, Repository, RepositoryHook}
import org.eclipse.egit.github.core.client.{GitHubClient, RequestException}
import org.eclipse.egit.github.core.service._
import play.Logger

import scala.concurrent.{ExecutionContext, Future}

trait GithubApiClient {
  import scala.collection.JavaConversions._

  val orgService: OrganizationService
  val teamService: ExtendedTeamService
  val repositoryService: RepositoryService
  val contentsService: ContentsService
  val releaseService: ReleaseService

  def getOrganisations(implicit ec: ExecutionContext): Future[List[GhOrganisation]] = Future {
    orgService.getOrganizations.toList.map { go =>
      GhOrganisation(go.getLogin, go.getId)
    }
  }

  def getTeamsForOrganisation(org: String)(implicit ec: ExecutionContext): Future[List[GhTeam]] = Future {
    teamService.getTeams(org).toList.map { gt =>
      GhTeam(gt.getName, gt.getId)
    }
  }

  def getReposForTeam(teamId: Long)(implicit ec: ExecutionContext): Future[List[GhRepository]] = Future {
    teamService.getRepositories(teamId.toInt).toList.map { gr =>
      GhRepository(gr.getName, gr.getId, gr.getHtmlUrl, gr.isFork)
    }
  }

  def getReleases(org: String, repoName: String)(implicit ec: ExecutionContext): Future[List[GhRepoRelease]] = Future {
    releaseService.getReleases(org, repoName)
  }

  def getTags(org: String, repoName: String)(implicit ec: ExecutionContext): Future[List[String]] = Future {

    repositoryService.getTags(repositoryId(repoName, org)).map(_.getName).toList

  }


  def repoContainsContent(path: String, repoName: String, orgName: String)(implicit ec: ExecutionContext) = Future {
    try {
      contentsService.getContents(repositoryId(repoName, orgName), path).nonEmpty
    } catch {
      case e: Throwable =>
        Log.warn(s"error getting content for :$repoName :$orgName errMessage : ${e.getMessage}")
        false
    }
  }

  def repositoryId(repoName: String, orgName: String): IRepositoryIdProvider= {
    new IRepositoryIdProvider {
      val generateId: String = orgName + "/" + repoName
    }
  }

  def containsRepo(orgName: String, repoName: String)(implicit ec: ExecutionContext): Future[Boolean] = Future {
    try {
      repositoryService.getRepository(orgName, repoName) != null
    }
    catch { case ex: RequestException =>
      Log.warn(s"error getting content for :$repoName :$orgName errMessage : ${ex.getMessage}")
      if (ex.getMessage.contains("404")) false
      else throw ex;
    }

  }

  def teamId(orgName: String, team: String)(implicit ec: ExecutionContext): Future[Option[Int]] = Future {
    teamService.getTeams(orgName).toList.find(t => t.getName == team).map(_.getId)
  }

  def createRepo(orgName: String, repoName: String)(implicit ec: ExecutionContext) = Future {
    val newRepo = repositoryService.createRepository(
      orgName,
      new Repository().setName(repoName).setPrivate(false).setHasIssues(true).setHasWiki(true).setHasDownloads(true))

    newRepo.getCloneUrl
  }

  def addRepoToTeam(orgName: String, repoName: String, teamId: Int)(implicit ec: ExecutionContext) : Future[Unit] = Future {
    teamService.addRepository(teamId, repositoryId(repoName, orgName))
  }

  def createHook(orgName: String,
                 repoName: String,
                 hookName: String,
                 config: Map[String, String],
                 active: Boolean = true)(implicit ec: ExecutionContext) : Future[Unit] = Future {

    val idProvider = new IRepositoryIdProvider {
      val generateId: String = orgName + "/" + repoName
    }

    repositoryService.createHook(
      idProvider,
      new RepositoryHook().setName(hookName).setConfig(config).setActive(active))
  }
}

object GithubApiClient {

  def apply(apiUrl: String, apiToken: String): GithubApiClient = {
    val client: ExtendedGitHubClient = ExtendedGitHubClient(apiUrl)
      .setOAuth2Token(apiToken)
      .asInstanceOf[ExtendedGitHubClient]

    new GithubApiClient {
      val orgService: OrganizationService = new OrganizationService(client)
      val teamService: ExtendedTeamService = new ExtendedTeamService(client)
      val repositoryService: RepositoryService = new RepositoryService(client)
      val contentsService: ContentsService = new ContentsService(client)
      val releaseService: ReleaseService = new ReleaseService(client)
    }
  }

}

