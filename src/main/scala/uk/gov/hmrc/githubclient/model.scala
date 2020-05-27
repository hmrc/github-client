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
  language: String,
  isArchived: Boolean
)

case class GhRepoRelease(id: Long, tagName: String, createdAt: Date)

case class GhRepoTag(name: String)

case class OrganisationName(value: String) extends NonEmptyString {
  override def toString: String = value
}

case class RepositoryName(value: String) extends NonEmptyString {
  override def toString: String = value
}

case class HookConfig(url: Url, contentType: Option[HookContentType] = None, secret: Option[HookSecret] = None)

case class Url(value: String) extends NonEmptyString {
  override def toString: String = value
}

sealed abstract class HookContentType(val value: String) {
  override def toString: String = value
}

object HookContentType {
  case object Form extends HookContentType("form")
  case object Json extends HookContentType("json")

  def apply(value: String): HookContentType = value match {
    case Form.value => HookContentType.Form
    case Json.value => HookContentType.Json
    case other      => throw new IllegalArgumentException(s"'$other' is not a valid ContentType")
  }
}

case class HookSecret(value: String) extends NonEmptyString {
  override def toString: String = value
}

case class Hook(id: HookId, url: Url, name: HookName, active: Boolean, config: HookConfig)


case class HookId(value: Long) extends AnyVal {
  override def toString: String = value.toString
}

sealed trait HookName {
  val value: String
  override def toString: String = value
}

object HookName {
  case object Web extends HookName {
    override val value: String = "web"
  }
  case class NonWebHookName(value: String) extends HookName with NonEmptyString

  def apply(value: String): HookName = value match {
    case Web.value => Web
    case other     => NonWebHookName(other)
  }
}

sealed abstract class HookEvent(val value: String) {
  override def toString: String = value
}

object HookEvent {
  case object CheckRun extends HookEvent("check_run")
  case object CheckSuite extends HookEvent("check_suite")
  case object CommitComment extends HookEvent("commit_comment")
  case object Create extends HookEvent("create")
  case object Delete extends HookEvent("delete")
  case object Deployment extends HookEvent("deployment")
  case object DeploymentStatus extends HookEvent("deployment_status")
  case object Download extends HookEvent("download")
  case object Follow extends HookEvent("follow")
  case object Fork extends HookEvent("fork")
  case object ForkApply extends HookEvent("fork_apply")
  case object GithubAppAuthorization extends HookEvent("github_app_authorization")
  case object Gist extends HookEvent("gist")
  case object Gollum extends HookEvent("gollum")
  case object Installation extends HookEvent("installation")
  case object InstallationRepositories extends HookEvent("installation_repositories")
  case object IssueComment extends HookEvent("issue_comment")
  case object Issues extends HookEvent("issues")
  case object Label extends HookEvent("label")
  case object MarketplacePurchase extends HookEvent("marketplace_purchase")
  case object Member extends HookEvent("member")
  case object Membership extends HookEvent("membership")
  case object Milestone extends HookEvent("milestone")
  case object Organization extends HookEvent("organization")
  case object OrgBlock extends HookEvent("org_block")
  case object PageBuild extends HookEvent("page_build")
  case object ProjectCard extends HookEvent("project_card")
  case object ProjectColumn extends HookEvent("project_column")
  case object Project extends HookEvent("project")
  case object Public extends HookEvent("public")
  case object PullRequest extends HookEvent("pull_request")
  case object PullRequestReview extends HookEvent("pull_request_review")
  case object PullRequestReviewComment extends HookEvent("pull_request_review_comment")
  case object Push extends HookEvent("push")
  case object Release extends HookEvent("release")
  case object Repository extends HookEvent("repository")
  case object RepositoryImport extends HookEvent("repository_import")
  case object RepositoryVulnerabilityAlert extends HookEvent("repository_vulnerability_alert")
  case object Status extends HookEvent("status")
  case object Team extends HookEvent("team")
  case object TeamAdd extends HookEvent("team_add")
  case object Watch extends HookEvent("watch")

  val all: Set[HookEvent] = Set(
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

  def apply(name: String): HookEvent =
    all
      .find(_.value == name)
      .getOrElse {
        throw new IllegalArgumentException(s"'$name' unknown event name")
      }

  def apply(names: String*): Set[HookEvent] =
    names.toSet map HookEvent.apply
}

trait NonEmptyString {
  def value: String
  require(value.trim.nonEmpty, s"${getClass.getSimpleName} cannot be empty")
}
