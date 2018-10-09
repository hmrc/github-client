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

import com.google.gson.Gson
import org.eclipse.egit.github.core.service.{OrganizationService, RepositoryService}
import org.eclipse.egit.github.core.{IRepositoryIdProvider, RepositoryHook}
import org.scalamock.scalatest.MockFactory
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.githubclient.GithubApiClient.WebHook

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

class HookFunctionsSpec extends WordSpec with MockFactory with ScalaFutures {

  "GitHubAPIClient.createHook" should {

    "add the given hook to the repository" in new Setup {
      val receivedHook = new RepositoryHook()

      (repositoryServiceMock
        .createHook(_: IRepositoryIdProvider, _: RepositoryHook))
        .expects(
          argAssert { provider: IRepositoryIdProvider =>
            provider.generateId() shouldBe s"$organisation/$repoName"
          },
          argAssert { hook: WebHook =>
            hook.getConfig.asScala("url") shouldBe "jenkins_hook_url"
            hook.getName                  shouldBe "web"
            hook.isActive                 shouldBe false
            hook.events                   should contain only "push"
          }
        )
        .returning(receivedHook)

      githubClient
        .createHook(organisation, repoName, config, events, active = false)
        .futureValue shouldBe receivedHook
    }

    "handle API rate limit error" in new Setup {

      val runtimeException = new RuntimeException("api rate limit exceeded")
      (repositoryServiceMock.createHook _)
        .expects(*, *)
        .throwing(runtimeException)

      intercept[APIRateLimitExceededException] {
        Await.result(githubClient.createHook(organisation, repoName, config), 1 second)
      } shouldBe APIRateLimitExceededException(runtimeException)
    }

    "WebHook" should {

      "get serialized with all the fields" in new Setup {
        val hookConfig = HookConfig(
          Url("jenkins_hook_url"),
          Some(ContentType.Form),
          Some(Secret("some_secret"))
        )

        val json: JsValue = Json.parse(new Gson().toJson(WebHook(hookConfig, active = true, events)))

        (json \ "name").as[String]                    shouldBe "web"
        (json \ "events").as[Seq[String]].toSet       shouldBe events
        (json \ "active").as[Boolean]                 shouldBe true
        (json \ "config" \ "url").as[String]          shouldBe "jenkins_hook_url"
        (json \ "config" \ "content_type").as[String] shouldBe ContentType.Form.toString
        (json \ "config" \ "secret").as[String]       shouldBe "some_secret"
      }

      "get serialized when hook config have Nones where possible" in new Setup {
        val hookConfig = HookConfig(
          Url("jenkins_hook_url"),
          contentType = None,
          secret      = None
        )

        val json: JsValue = Json.parse(new Gson().toJson(WebHook(hookConfig, active = true, events)))

        (json \ "name").as[String]                       shouldBe "web"
        (json \ "events").as[Seq[String]].toSet          shouldBe events
        (json \ "active").as[Boolean]                    shouldBe true
        (json \ "config" \ "url").as[String]             shouldBe "jenkins_hook_url"
        (json \ "config" \ "content_type").asOpt[String] shouldBe None
        (json \ "config" \ "secret").asOpt[String]       shouldBe None
      }
    }
  }

  private trait Setup {
    val organisation = OrganisationName("HMRC")
    val repoName     = RepositoryName("test-repo")
    val events       = Set("push")
    val config       = HookConfig(Url("jenkins_hook_url"))

    val extendedGithubClientMock: ExtendedGitHubClient = mock[ExtendedGitHubClient]
    val organizationServiceMock: OrganizationService   = mock[OrganizationService]
    val repositoryServiceMock: RepositoryService       = mock[RepositoryService]

    def githubClient: GithubApiClient = new GithubApiClient {
      val orgService: OrganizationService          = organizationServiceMock
      val teamService: ExtendedTeamService         = new ExtendedTeamService(extendedGithubClientMock)
      val repositoryService: RepositoryService     = repositoryServiceMock
      val contentsService: ExtendedContentsService = new ExtendedContentsService(extendedGithubClientMock)
      val releaseService: ReleaseService           = new ReleaseService(extendedGithubClientMock)
      val metrics: GithubClientMetrics             = DefaultGithubClientMetrics
    }
  }
}
