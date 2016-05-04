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

package uk.gov.hmrc.gitclient

import com.github.tomakehurst.wiremock.http.RequestMethod.{GET, HEAD}
import org.scalatest.Matchers
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext.Implicits.global

class GithubApiClientSpec extends WireMockSpec with ScalaFutures with Matchers with DefaultPatienceConfig {


  val testEndpoints = new GithubApiEndpoints(endpointMockUrl)

  def githubApiClient = new GithubApiClient(GitApiConfig("", "", endpointMockUrl))


  "GitHubAPIClient.getOrganisations" should {
    "get all organizations" in {

      val getOrganizationResponse =
        s"""[
           |{"login": "ORG1",
           |"id": 1
           |},
           |{
           |"login": "ORG2",
           |"id": 2
           |}
           |]""".stripMargin


      givenGitHubExpects(
        method = GET,
        url = testEndpoints.organisations,
        willRespondWith = (200, Some(getOrganizationResponse))
      )

      githubApiClient.getOrganisations.futureValue shouldBe List(GhOrganisation("ORG1", 1), GhOrganisation("ORG2", 2))

    }
  }

  "GitHubAPIClient.getTeamsForOrganisation" should {
    "get all team for organization" in {

      val teamsJson =
        s"""[{
           |"name": "Ateam",
           |"id": 1,
           |"slug": "Ateam",
           |"description": "Ateam",
           |"permission": "admin",
           |"url": "${testEndpoints.apiBaseUrl}/api/v3/teams/1",
           |"repositories_url": "${testEndpoints.apiBaseUrl}/api/v3/teams/1/repos",
           |"privacy": "secret"
           |}]""".stripMargin


      givenGitHubExpects(
        method = GET,
        url = testEndpoints.teamsForOrganisation(
          "ORG1"),
        willRespondWith = (200, Some(teamsJson))
      )

      githubApiClient.getTeamsForOrganisation("ORG1").futureValue shouldBe List(GhTeam("Ateam", 1))


    }
  }


  "GitHubAPIClient.getReposForTeam" should {
    "get all team for organization" in {

      val reposJson =
        s"""[{
           |"id": 1,
           |"name": "repoA",
           |"private": false,
           |"html_url": "http://some/html/url",
           |"fork": true
           |}]""".stripMargin

      givenGitHubExpects(
        method = GET,
        url = testEndpoints.reposForTeam(1),
        willRespondWith = (200, Some(reposJson))
      )

      githubApiClient.getReposForTeam(1).futureValue shouldBe List(GhRepository("repoA", 1, "http://some/html/url", true))

    }
  }

  "GitHubAPIClient.repoContainsFolder" should {
    "retrun true if it contains the folder" in {
      val folderName = "folder"

      givenGitHubExpects(
        method = HEAD,
        url = s"${testEndpoints.repoContents("OrgA", "repoA")}/$folderName",
        willRespondWith = (200, None)
      )

      githubApiClient.repoContainsFolder(folderName, "repoA", "OrgA").futureValue shouldBe true

    }

    "retrun false if it does not contain the folder" in {
      val folderName = "folder"

      givenGitHubExpects(
        method = HEAD,
        url = s"${testEndpoints.repoContents("OrgA", "repoA")}/$folderName",
        willRespondWith = (404, None)
      )

      githubApiClient.repoContainsFolder(folderName, "repoA", "OrgA").futureValue shouldBe false

    }

  }
}
