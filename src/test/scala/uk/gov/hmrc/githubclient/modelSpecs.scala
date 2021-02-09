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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.prop.TableDrivenPropertyChecks

class OrganisationNameSpec extends AnyWordSpecLike with Matchers {
  "OrganisationName" should {
    "be a NonEmptyString" in {
      OrganisationName("a") shouldBe a[NonEmptyString]
    }
  }
}

class RepositoryNameSpec extends AnyWordSpecLike with Matchers {
  "RepositoryName" should {
    "be a NonEmptyString" in {
      RepositoryName("a") shouldBe a[NonEmptyString]
    }
  }
}

class UrlSpec extends AnyWordSpecLike with Matchers {
  "Url" should {
    "be a NonEmptyString" in {
      Url("a") shouldBe a[NonEmptyString]
    }
  }
}

class HookSecretSpec extends AnyWordSpecLike with Matchers {
  "HookSecret" should {
    "be a NonEmptyString" in {
      HookSecret("a") shouldBe a[NonEmptyString]
    }
  }
}

class NonEmptyStringSpec extends AnyWordSpecLike with Matchers {

  "NonEmptyString" should {
    "not throw exception when instantiated with a non-empty String" in {
      an[IllegalArgumentException] should be thrownBy new NonEmptyString {
        override def value: String = "  "
      }
    }

    "throw exception when instantiated with an empty String" in {
      an[IllegalArgumentException] should be thrownBy new NonEmptyString {
        override def value: String = "  "
      }
    }
  }
}

class HookContentTypeSpec extends AnyWordSpecLike with Matchers {

  "ContentType.apply" should {
    "return ContentType.Form for 'form'" in {
      HookContentType("form") shouldBe HookContentType.Form
    }

    "return ContentType.Json for 'json'" in {
      HookContentType("json") shouldBe HookContentType.Json
    }

    "throw an IllegalArgumentException if value is neither 'json' nor 'form'" in {
      an[IllegalArgumentException] should be thrownBy HookContentType("abc")
    }
  }
}

class HookNameSpec extends AnyWordSpecLike with Matchers {
  "HookName.apply" should {
    "return Web if value is 'web'" in {
      HookName("web") shouldBe HookName.Web
    }

    "return NonWebHookName if value is different than 'web'" in {
      HookName("abc") shouldBe HookName.NonWebHookName("abc")
    }

    "return NonWebHookName which is a NonEmptyString" in {
      HookName("abc") shouldBe a[NonEmptyString]
    }
  }
}

class HookEventSpec extends AnyWordSpecLike with Matchers with TableDrivenPropertyChecks {

  import HookEvent._

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

  "HookName.apply" should {
    "return an instance of HookName for a known value" in {
      forAll(scenarios) { (eventName, eventType) =>
        HookEvent(eventName) shouldBe eventType
      }
    }

    "return a set of instances of HookNames for a set of known values" in {
      HookEvent("push", "watch") shouldBe Set(Push, Watch)
    }

    "throw an IllegalArgumentException if value is not on the list of known events" in {
      an[IllegalArgumentException] should be thrownBy HookEvent("abc")
    }
  }
}
