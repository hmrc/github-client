/*
 * Copyright 2018 HM Revenue & Customs
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

import java.util.Date

case class GhOrganisation(login: String, id: Int = 0)

case class GhTeam(name: String, id: Long)

case class GhRepository(
  name: String,
  description: String,
  id: Long,
  htmlUrl: String,
  fork: Boolean = false,
  createdDate: Long,
  lastActiveDate: Long,
  isPrivate: Boolean,
  language: String
)

case class GhRepoRelease(id: Long, tagName: String, createdAt: Date)

case class GhRepoTag(name: String)

case class OrganisationName(value: String) extends AnyVal {
  override def toString: String = value
}

case class RepositoryName(value: String) extends AnyVal {
  override def toString: String = value
}

case class HookConfig(url: Url, contentType: Option[ContentType] = None, secret: Option[Secret] = None)

case class Url(value: String) extends AnyVal {
  override def toString: String = value
}

sealed abstract class ContentType(val value: String) {
  override def toString: String = value
}

object ContentType {
  case object Form extends ContentType("form")
  case object Json extends ContentType("json")

  def apply(value: String): ContentType = value match {
    case Form.value => ContentType.Form
    case Json.value => ContentType.Json
    case other      => throw new IllegalArgumentException(s"'$other' is not a valid ContentType")
  }
}

case class Secret(value: String) extends AnyVal {
  override def toString: String = value
}

case class WebHook(id: WebHookId, url: Url, name: WebHookName, active: Boolean, config: HookConfig)

case class WebHookId(value: Long) extends AnyVal {
  override def toString: String = value.toString
}

sealed trait WebHookName {
  val value: String
  override def toString: String = value
}

object WebHookName {
  case object Web extends WebHookName {
    override val value: String = "web"
  }
  case class OtherWebHookName(value: String) extends WebHookName

  def apply(value: String): WebHookName = value match {
    case Web.value => Web
    case other     => OtherWebHookName(other)
  }
}

sealed abstract class WebHookEvent(val value: String) {
  override def toString: String = value
}

object WebHookEvent {
  case object CheckRun extends WebHookEvent("check_run")
  case object CheckSuite extends WebHookEvent("check_suite")
  case object CommitComment extends WebHookEvent("commit_comment")
  case object Create extends WebHookEvent("create")
  case object Delete extends WebHookEvent("delete")
  case object Deployment extends WebHookEvent("deployment")
  case object DeploymentStatus extends WebHookEvent("deployment_status")
  case object Download extends WebHookEvent("download")
  case object Follow extends WebHookEvent("follow")
  case object Fork extends WebHookEvent("fork")
  case object ForkApply extends WebHookEvent("fork_apply")
  case object GithubAppAuthorization extends WebHookEvent("github_app_authorization")
  case object Gist extends WebHookEvent("gist")
  case object Gollum extends WebHookEvent("gollum")
  case object Installation extends WebHookEvent("installation")
  case object InstallationRepositories extends WebHookEvent("installation_repositories")
  case object IssueComment extends WebHookEvent("issue_comment")
  case object Issues extends WebHookEvent("issues")
  case object Label extends WebHookEvent("label")
  case object MarketplacePurchase extends WebHookEvent("marketplace_purchase")
  case object Member extends WebHookEvent("member")
  case object Membership extends WebHookEvent("membership")
  case object Milestone extends WebHookEvent("milestone")
  case object Organization extends WebHookEvent("organization")
  case object OrgBlock extends WebHookEvent("org_block")
  case object PageBuild extends WebHookEvent("page_build")
  case object ProjectCard extends WebHookEvent("project_card")
  case object ProjectColumn extends WebHookEvent("project_column")
  case object Project extends WebHookEvent("project")
  case object Public extends WebHookEvent("public")
  case object PullRequest extends WebHookEvent("pull_request")
  case object PullRequestReview extends WebHookEvent("pull_request_review")
  case object PullRequestReviewComment extends WebHookEvent("pull_request_review_comment")
  case object Push extends WebHookEvent("push")
  case object Release extends WebHookEvent("release")
  case object Repository extends WebHookEvent("repository")
  case object RepositoryImport extends WebHookEvent("repository_import")
  case object RepositoryVulnerabilityAlert extends WebHookEvent("repository_vulnerability_alert")
  case object Status extends WebHookEvent("status")
  case object Team extends WebHookEvent("team")
  case object TeamAdd extends WebHookEvent("team_add")
  case object Watch extends WebHookEvent("watch")

  val all: Set[WebHookEvent] = Set(
    CheckRun,
    CheckSuite,
    CommitComment,
    Create,
    Delete,
    Deployment,
    DeploymentStatus,
    Fork,
    GithubAppAuthorization,
    Gollum,
    Installation,
    InstallationRepositories,
    IssueComment,
    Issues,
    Label,
    MarketplacePurchase,
    Member,
    Membership,
    Milestone,
    Organization,
    OrgBlock,
    PageBuild,
    ProjectCard,
    ProjectColumn,
    Project,
    Public,
    PullRequest,
    PullRequestReview,
    PullRequestReviewComment,
    Push,
    Release,
    Repository,
    RepositoryImport,
    RepositoryVulnerabilityAlert,
    Status,
    Team,
    TeamAdd,
    Watch
  )

  def apply(name: String): WebHookEvent =
    all
      .find(_.value == name)
      .getOrElse {
        throw new IllegalArgumentException(s"'$name' unknown event name")
      }

  def apply(names: String*): Set[WebHookEvent] =
    names.toSet map WebHookEvent.apply
}
