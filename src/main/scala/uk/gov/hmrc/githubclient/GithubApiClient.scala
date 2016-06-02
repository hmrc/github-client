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

import java.net.URL

import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.{ContentsService, OrganizationService, RepositoryService, TeamService}

import scala.concurrent.{ExecutionContext, Future}

class GithubApiClient(gitConfig: GitApiConfig) {

  import scala.collection.JavaConversions._

  val gitHubUrl = new URL(gitConfig.apiUrl)

  println("using new github URL")

  private[githubclient] val client = new GitHubClient(gitHubUrl.getHost, gitHubUrl.getPort, gitHubUrl.getProtocol)
    .setCredentials(gitConfig.user, gitConfig.key)

  // all these could be mocked and tested
  private[githubclient] val httpClient = new HttpClient(gitConfig.user, gitConfig.key)
  private[githubclient] val githubEndpoints = new GithubApiEndpoints(gitConfig.apiUrl)
  private[githubclient] val orgService = new OrganizationService(client)
  private[githubclient] val teamService = new TeamService(client)
  private[githubclient] val repositoryService = new RepositoryService(client)
  private[githubclient] val contentsService = new ContentsService(client)

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

  def repoContainsContent(path: String, repoName: String, orgName: String)(implicit ec: ExecutionContext) = Future {

    contentsService.getContents(repositoryService.getRepository(orgName, repoName)).contains(path)

  }

}

