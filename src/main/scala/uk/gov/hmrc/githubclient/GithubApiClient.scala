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

import scala.concurrent.{Future, ExecutionContext}

class GithubApiClient(gitConfig : GitApiConfig) {

  val httpClient = new HttpClient(gitConfig.user, gitConfig.key)

  val githubEndpoints = new GithubApiEndpoints(gitConfig.apiUrl)

  def getOrganisations(implicit ec: ExecutionContext) = {

    val url: String = githubEndpoints.organisations

    httpClient.get[List[GhOrganisation]](url).map(result => {
      Log.info(s"Got ${result.length} organisations from $url")
      result
    })
  }

  def getTeamsForOrganisation(org: String)(implicit ec: ExecutionContext) = {

    val url: String = githubEndpoints.teamsForOrganisation(org)

    httpClient.get[List[GhTeam]](url).map(result => {
      Log.info(s"Got ${result.length} teams for $org from $url")
      result
    })
  }

  def getReposForTeam(teamId: Long)(implicit ec: ExecutionContext) = {

    val url: String = githubEndpoints.reposForTeam(teamId)

    httpClient.get[List[GhRepository]](url)
  }

  def repoContainsContent(path: String, repoName: String, orgName: String)(implicit ec: ExecutionContext) = {
    val url: String = s"${githubEndpoints.repoContents(orgName, repoName)}/$path"
    httpClient.head(url).map(result => {
      Log.info(s"Got $result when checking for $path folder in $orgName/$repoName from $url")
      result == 200
    })
  }

  def close() = httpClient.close()

}

