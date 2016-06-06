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

import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service._
import play.Logger

import scala.concurrent.{ExecutionContext, Future}

trait GithubApiClient {

  import scala.collection.JavaConversions._

  val orgService: OrganizationService
  val teamService: TeamService
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

  def repoContainsContent(path: String, repoName: String, orgName: String)(implicit ec: ExecutionContext) = Future {

    try {
      val idProvider = new IRepositoryIdProvider {
        val generateId: String = orgName + "/" + repoName
      }
      contentsService.getContents(idProvider).exists(_.getPath == path)
    } catch {
      case e =>
        Log.warn(s"error getting content for :$repoName :$orgName errMessage : ${e.getMessage}")
        false
    }

  }

}

object GithubApiClient {

  def apply(apiUrl: String, apiToken: String): GithubApiClient = {

    val client: GitHubClient = GitHubClient.createClient(apiUrl).setOAuth2Token(apiToken)

    new GithubApiClient {
      val orgService: OrganizationService = new OrganizationService(client)
      val teamService: TeamService = new TeamService(client)
      val repositoryService: RepositoryService = new RepositoryService(client)
      val contentsService: ContentsService = new ContentsService(client)
      val releaseService: ReleaseService = new ReleaseService(client)
    }


  }
}

