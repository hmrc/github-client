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

trait GithubEndpoints {
  def apiBaseUrl: String

  def reposForTeam(teamId: Long): String

  def teamsForOrganisation(organisation: String): String

  def organisations: String

  def repoContents(orgName: String, repositoryName: String): String

}

class GithubApiEndpoints(val apiBaseUrl: String) extends GithubEndpoints {

  def reposForTeam(teamId: Long) = s"${apiBaseUrl}/teams/$teamId/repos?per_page=100"

  def teamsForOrganisation(organisation: String) = s"${apiBaseUrl}/orgs/$organisation/teams?per_page=100"

  def organisations = s"${apiBaseUrl}/user/orgs"

  def repoContents(orgName: String, repositoryName: String) = s"${apiBaseUrl}/repos/$orgName/$repositoryName/contents"

}

