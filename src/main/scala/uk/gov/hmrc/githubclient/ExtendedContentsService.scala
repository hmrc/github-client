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

import java.util

import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.{ContentsService, TeamService}
import org.eclipse.egit.github.core.{IRepositoryIdProvider, Team}

class ExtendedContentsService(client: GitHubClient) extends ContentsService(client) {

  def createFile(orgName: String,
                          repoName: String,
                          pathAndFileName: String,
                          base64EncodedContents: String,
                          commitMessage: String): Unit = {

    val uri = new StringBuilder("/repos")
    uri.append(s"/$orgName")
    uri.append(s"/$repoName")
    uri.append("/contents")
    uri.append(s"/$pathAndFileName")

    val params = new util.HashMap[String, String]()
    params.put("message", commitMessage)
    params.put("content", base64EncodedContents)

    client.put(uri.toString(), params, null)

  }
}
