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

import java.util.Base64

import com.google.common.io.BaseEncoding
import org.eclipse.egit.github.core.client.RequestException
import org.eclipse.egit.github.core.service._
import org.eclipse.egit.github.core.{IRepositoryIdProvider, Repository, RepositoryContents}

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConverters._

trait GithubApiClient extends HooksApi {

  import RateLimit._

  protected val orgService: OrganizationService
  protected val teamService: ExtendedTeamService
  protected val repositoryService: ExtendedRepositoryService
  protected val contentsService: ExtendedContentsService
  protected val releaseService: ReleaseService

  def getOrganisations(implicit ec: ExecutionContext): Future[List[GhOrganisation]] =
    Future {
      orgService.getOrganizations.asScala.toList
        .map(go => GhOrganisation(go.getLogin, go.getId))
    }.checkForApiRateLimitError

  def getTeamsForOrganisation(org: String)(implicit ec: ExecutionContext): Future[List[GhTeam]] =
    Future {
      teamService.getTeams(org).asScala.toList
        .map(gt => GhTeam(gt.getName, gt.getId))
    }.checkForApiRateLimitError

  def getReposForTeam(teamId: Long)(implicit ec: ExecutionContext): Future[List[GhRepository]] =
    Future {
      teamService.getExtendedRepositories(teamId.toInt).map { gr =>
        GhRepository(
          gr.name,
          Option(gr.description).getOrElse(""),
          gr.id,
          gr.htmlUrl,
          gr.fork,
          gr.createdAt.getTime,
          gr.pushedAt.getTime,
          gr.`private`,
          Option(gr.language).getOrElse(""),
          gr.archived
        )
      }
    }.checkForApiRateLimitError

  def getReposForOrg(org: String)(implicit ec: ExecutionContext): Future[List[GhRepository]] =
    Future {
      repositoryService
        .getOrgExtendedRepositories(org).map { gr: ExtendedRepository =>
        GhRepository(
            gr.name,
            Option(gr.description).getOrElse(""),
            gr.id,
            gr.htmlUrl,
            gr.fork,
            gr.createdAt.getTime,
            gr.pushedAt.getTime,
            gr.`private`,
            Option(gr.language).getOrElse(""),
            gr.archived
          )
        }
    }.checkForApiRateLimitError

  def getReleases(org: String, repoName: String)(implicit ec: ExecutionContext): Future[List[GhRepoRelease]] =
    Future {
      releaseService.getReleases(org, repoName)
    }.checkForApiRateLimitError

  def getTags(org: String, repoName: String)(implicit ec: ExecutionContext): Future[List[String]] =
    Future {
      repositoryService.getTags(repositoryId(repoName, org)).asScala.toList.map(_.getName)
    }.checkForApiRateLimitError

  def repoContainsContent(path: String, repoName: String, orgName: String)(
      implicit ec: ExecutionContext): Future[Boolean] =
    Future {
      try {
        contentsService.getContents(repositoryId(repoName, orgName), path).asScala.nonEmpty
      } catch {
        case e if isRateLimit(e) =>
          rateLimitError(e)
        case e: RequestException if e.getStatus == 404 =>
          false
        }
    }

  def listContent(repoName: String, orgName: String)(
      implicit ec: ExecutionContext): Future[List[RepositoryContents]] =
    Future {
      contentsService.getContents(repositoryId(repoName, orgName)).asScala.toList
    }.checkForApiRateLimitError

  private def isDirectory(path: String, contents: Seq[RepositoryContents]) =
    contents.head.getPath != path

  def getFileContent(path: String, repoName: String, orgName: String)(
    implicit ec: ExecutionContext): Future[Option[String]] = Future {
    try {
      val contents = contentsService.getContents(repositoryId(repoName, orgName), path).asScala
      if (contents.isEmpty || isDirectory(path, contents)) None
      else Some(new String(Base64.getMimeDecoder.decode(contents.head.getContent)))
    } catch {
      case e if isRateLimit(e) =>
        rateLimitError(e)
      case e: RequestException if e.getStatus == 404 =>
        None
    }
  }

  def repositoryId(repoName: String, orgName: String): IRepositoryIdProvider =
    new IRepositoryIdProvider {
      val generateId: String = orgName + "/" + repoName
    }

  def containsRepo(orgName: String, repoName: String)(implicit ec: ExecutionContext): Future[Boolean] = Future {
    try {
      repositoryService.getRepository(orgName, repoName) != null
    } catch {
      case e if isRateLimit(e) =>
        rateLimitError(e)
      case e: RequestException if e.getStatus == 404 =>
        false
    }
  }

  def teamId(orgName: String, team: String)(implicit ec: ExecutionContext): Future[Option[Int]] =
    Future {
      teamService.getTeams(orgName).asScala.toList.find(t => t.getName == team).map(_.getId)
    }.checkForApiRateLimitError

  def createRepo(orgName: String, repoName: String)(implicit ec: ExecutionContext): Future[String] =
    Future {
      val newRepo = repositoryService.createRepository(
        orgName,
        new Repository().setName(repoName).setPrivate(false).setHasIssues(true).setHasWiki(true).setHasDownloads(true))

      newRepo.getCloneUrl
    }.checkForApiRateLimitError

  def addRepoToTeam(orgName: String, repoName: String, teamId: Int)(implicit ec: ExecutionContext): Future[Unit] =
    Future {
      teamService.addRepository(teamId, repositoryId(repoName, orgName))
    }.checkForApiRateLimitError

  def createFile(orgName: String, repoName: String, pathAndFileName: String, contents: String, message: String)(
    implicit ec: ExecutionContext): Future[Unit] =
    if (message.isEmpty)
      Future.failed(new RuntimeException("Commit message must be provided"))
    else
      Future {
        val encodedContents = BaseEncoding.base64().encode(contents.getBytes)
        contentsService.createFile(orgName, repoName, pathAndFileName, encodedContents, message)
      }.checkForApiRateLimitError
}

object GithubApiClient {

  def apply(
    apiUrl: String,
    apiToken: String,
    metrics: GithubClientMetrics = DefaultGithubClientMetrics): GithubApiClient = {
    val client: ExtendedGitHubClient = ExtendedGitHubClient(apiUrl, metrics)
      .setOAuth2Token(apiToken)
      .asInstanceOf[ExtendedGitHubClient]

    new GithubApiClient {
      val orgService: OrganizationService              = new OrganizationService(client)
      val teamService: ExtendedTeamService             = new ExtendedTeamService(client)
      val repositoryService: ExtendedRepositoryService = new ExtendedRepositoryService(client)
      val contentsService: ExtendedContentsService     = new ExtendedContentsService(client)
      val releaseService: ReleaseService               = new ReleaseService(client)
    }
  }
}
