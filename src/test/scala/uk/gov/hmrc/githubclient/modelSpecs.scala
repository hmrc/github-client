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

import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.scalatest.prop.TableDrivenPropertyChecks

class ContentTypeSpec extends WordSpec {

  "ContentType.apply" should {
    "return ContentType.Form for 'form'" in {
      ContentType("form") shouldBe ContentType.Form
    }
    "return ContentType.Json for 'json'" in {
      ContentType("json") shouldBe ContentType.Json
    }
    "throw an IllegalArgumentException if value is neither 'json' nor 'form'" in {
      an[IllegalArgumentException] should be thrownBy ContentType("abc")
    }
  }
}

class WebHookNameSpec extends WordSpec {

  "WebHookName.apply" should {
    "return WebHookName.Web if value is 'web'" in {
      WebHookName("web") shouldBe WebHookName.Web
    }
    "return WebHookName if value is different than 'web'" in {
      WebHookName("abc") shouldBe WebHookName.OtherWebHookName("abc")
    }
  }
}

class WebHookEventSpec extends WordSpec with TableDrivenPropertyChecks {

  import WebHookEvent._

  private val scenarios = Table(
    "event name"                     -> "expected type",
    "check_run"                      -> CheckRun,
    "check_suite"                    -> CheckSuite,
    "commit_comment"                 -> CommitComment,
    "create"                         -> Create,
    "delete"                         -> Delete,
    "deployment"                     -> Deployment,
    "deployment_status"              -> DeploymentStatus,
    "fork"                           -> Fork,
    "github_app_authorization"       -> GithubAppAuthorization,
    "gollum"                         -> Gollum,
    "installation"                   -> Installation,
    "installation_repositories"      -> InstallationRepositories,
    "issue_comment"                  -> IssueComment,
    "issues"                         -> Issues,
    "label"                          -> Label,
    "marketplace_purchase"           -> MarketplacePurchase,
    "member"                         -> Member,
    "membership"                     -> Membership,
    "milestone"                      -> Milestone,
    "organization"                   -> Organization,
    "org_block"                      -> OrgBlock,
    "page_build"                     -> PageBuild,
    "project_card"                   -> ProjectCard,
    "project_column"                 -> ProjectColumn,
    "project"                        -> Project,
    "public"                         -> Public,
    "pull_request_review_comment"    -> PullRequestReviewComment,
    "pull_request_review"            -> PullRequestReview,
    "pull_request"                   -> PullRequest,
    "push"                           -> Push,
    "repository"                     -> Repository,
    "repository_import"              -> RepositoryImport,
    "repository_vulnerability_alert" -> RepositoryVulnerabilityAlert,
    "release"                        -> Release,
    "status"                         -> Status,
    "team"                           -> Team,
    "team_add"                       -> TeamAdd,
    "watch"                          -> Watch
  )

  "WebHookName.apply" should {
    "return an instance of WebHookName for a known value" in {
      forAll(scenarios) { (eventName, eventType) =>
        WebHookEvent(eventName) shouldBe eventType
      }
    }
    "return a set of instances of WebHookNames for a set of known values" in {
      WebHookEvent("push", "watch") shouldBe Set(Push, Watch)
    }
    "throw an IllegalArgumentException if value is not on the list of known events" in {
      an[IllegalArgumentException] should be thrownBy WebHookEvent("abc")
    }
  }
}
