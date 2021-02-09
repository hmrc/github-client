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

import org.eclipse.egit.github.core.service.RepositoryService
import java.util
import com.google.gson.reflect.TypeToken
import org.eclipse.egit.github.core.client.PagedRequest
import scala.collection.JavaConverters._

class ExtendedRepositoryService(client: ExtendedGitHubClient) extends RepositoryService(client) {

  println(s"in ExtendedRepositoryService")

  // These values are copied from the underlying RespositoryService
  private val pageSize = 100
  private val pageFirst = 1

  def getOrgExtendedRepositories(org: String): List[ExtendedRepository] = {
    if (org == null || org.length == 0)
      throw new IllegalArgumentException("Organization cannot be null or empty")

    val uri = new StringBuilder("/orgs")
    uri.append('/').append(org)
    uri.append("/repos")

    val request =
      createPagedRequest(pageFirst, pageSize)
        .setUri(uri.toString())
        .setType(new TypeToken[util.List[ExtendedRepository]]() {}.getType)
        .asInstanceOf[PagedRequest[ExtendedRepository]]

    getAll(request).asScala.toList
  }
}
