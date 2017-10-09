/*
 * Copyright 2017 HM Revenue & Customs
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
import org.eclipse.egit.github.core.{IRepositoryIdProvider, Repository, RepositoryContents, RepositoryHook}

import scala.concurrent.{ExecutionContext, Future}

case class APIRateLimitExceededException(exception: Throwable) extends RuntimeException(exception)


trait GithubApiClient {

  import GithubApiClient._

  import scala.collection.JavaConversions._

  val orgService: OrganizationService
  val teamService: ExtendedTeamService
  val repositoryService: RepositoryService
  val contentsService: ExtendedContentsService
  val releaseService: ReleaseService

  def getOrganisations(implicit ec: ExecutionContext): Future[List[GhOrganisation]] = Future {
    orgService.getOrganizations.toList.map { go =>
      GhOrganisation(go.getLogin, go.getId)
    }
  }.checkForApiRateLimitError


  def getTeamsForOrganisation(org: String)(implicit ec: ExecutionContext): Future[List[GhTeam]] = Future {
    teamService.getTeams(org).toList.map { gt =>
      GhTeam(gt.getName, gt.getId)
    }
  }.checkForApiRateLimitError

  def getReposForTeam(teamId: Long)(implicit ec: ExecutionContext): Future[List[GhRepository]] = Future {
    teamService.getRepositories(teamId.toInt).toList.map { gr =>
      GhRepository(gr.getName, Option(gr.getDescription).getOrElse(""), gr.getId, gr.getHtmlUrl, gr.isFork, gr.getCreatedAt.getTime, gr.getPushedAt.getTime, gr.isPrivate)
    }
  }.checkForApiRateLimitError

  def getReleases(org: String, repoName: String)(implicit ec: ExecutionContext): Future[List[GhRepoRelease]] = Future {
    releaseService.getReleases(org, repoName)
  }.checkForApiRateLimitError

  def getTags(org: String, repoName: String)(implicit ec: ExecutionContext): Future[List[String]] = Future {
    repositoryService.getTags(repositoryId(repoName, org)).map(_.getName).toList
  }.checkForApiRateLimitError

  def repoContainsContent(path: String, repoName: String, orgName: String)(implicit ec: ExecutionContext): Future[Boolean] = Future {
    try {
      contentsService.getContents(repositoryId(repoName, orgName), path).nonEmpty
    } catch {
      case e if isRateLimit(e) =>
        rateLimitError(e)
      case e: Throwable =>
        Log.warn(s"error getting content for :$repoName :$orgName errMessage : ${e.getMessage}")
        false
    }
  }

  private def isDirectory(path: String, contents: Seq[RepositoryContents]) =
    contents.head.getPath != path

  def getFileContent(path: String, repoName: String, orgName: String)(implicit ec: ExecutionContext): Future[Option[String]] = Future {
    try {
      val contents = contentsService.getContents(repositoryId(repoName, orgName), path)
      if (contents.isEmpty || isDirectory(path, contents)) None else Some(new String(Base64.getMimeDecoder.decode(contents.head.getContent)))
    } catch {
      case e if isRateLimit(e) =>
        rateLimitError(e)
      case e: Throwable =>
        Log.warn(s"error getting content for :$repoName :$orgName errMessage : ${e.getMessage}")
        None
    }
  }

  def repositoryId(repoName: String, orgName: String): IRepositoryIdProvider = {
    new IRepositoryIdProvider {
      val generateId: String = orgName + "/" + repoName
    }
  }

  def containsRepo(orgName: String, repoName: String)(implicit ec: ExecutionContext): Future[Boolean] = Future {
    try {
      repositoryService.getRepository(orgName, repoName) != null
    }
    catch {
      case e if isRateLimit(e) =>
        rateLimitError(e)
      case ex: RequestException =>
        Log.warn(s"error getting content for :$repoName :$orgName errMessage : ${ex.getMessage}")
        if (ex.getMessage.contains("404")) false
        else throw ex;
    }
  }

  def teamId(orgName: String, team: String)(implicit ec: ExecutionContext): Future[Option[Int]] = Future {
    teamService.getTeams(orgName).toList.find(t => t.getName == team).map(_.getId)
  }.checkForApiRateLimitError

  def createRepo(orgName: String, repoName: String)(implicit ec: ExecutionContext) = Future {
    val newRepo = repositoryService.createRepository(
      orgName,
      new Repository().setName(repoName).setPrivate(false).setHasIssues(true).setHasWiki(true).setHasDownloads(true))

    newRepo.getCloneUrl
  }.checkForApiRateLimitError

  def addRepoToTeam(orgName: String, repoName: String, teamId: Int)(implicit ec: ExecutionContext): Future[Unit] = Future {
    teamService.addRepository(teamId, repositoryId(repoName, orgName))
  }.checkForApiRateLimitError

  def createHook(orgName: String,
                 repoName: String,
                 hookName: String,
                 config: Map[String, String],
                 active: Boolean = true)(implicit ec: ExecutionContext): Future[RepositoryHook] = Future {

    val idProvider = new IRepositoryIdProvider {
      val generateId: String = orgName + "/" + repoName
    }

    repositoryService.createHook(
      idProvider,
      new RepositoryHook().setName(hookName).setConfig(config).setActive(active))
  }.checkForApiRateLimitError

  def createFile(orgName: String, repoName: String, pathAndFileName: String, contents: String, message: String)(implicit ec: ExecutionContext): Future[Unit] = {
    if (message.isEmpty)
      Future.failed(new RuntimeException("Commit message must be provided"))
    else
      Future {
        val encodedContents = BaseEncoding.base64().encode(contents.getBytes)
        contentsService.createFile(orgName, repoName, pathAndFileName, encodedContents, message)
      }.checkForApiRateLimitError
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
      val contentsService: ExtendedContentsService = new ExtendedContentsService(client)
      val releaseService: ReleaseService = new ReleaseService(client)
    }
  }

  private def isRateLimit(e: Throwable): Boolean = e.getMessage.toLowerCase.contains("api rate limit exceeded")

  implicit class RateLimit[T](theFuture: Future[T]) {
    def checkForApiRateLimitError(implicit executionContext: ExecutionContext): Future[T] = theFuture.recoverWith { case e if isRateLimit(e) => rateLimitError(e) }
  }

  private def rateLimitError(e: Throwable) = {
    Log.error("=== API rate limit has been reached ===", e)
    throw APIRateLimitExceededException(e)
  }


}
