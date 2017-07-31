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

import com.google.gson.reflect.TypeToken
import org.eclipse.egit.github.core.{Repository, RepositoryId}
import org.eclipse.egit.github.core.client.{GitHubClient, PagedRequest}
import org.eclipse.egit.github.core.service.{GitHubService, RepositoryService}
import org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS


class ReleaseService(client: GitHubClient) extends GitHubService(client) {

  def getReleases(org: String, repoName: String): List[GhRepoRelease] = {
    import scala.collection.JavaConversions._

    val request: PagedRequest[GhRepoRelease] = createPagedRequest()
    request.setUri(s"$SEGMENT_REPOS/$org/$repoName/releases")
    request.setType(new TypeToken[java.util.List[GhRepoRelease]]() {
    }.getType)
    getAll(request).toList
  }

  def getTags(org: String, repoName: String): List[GhRepoTag] = {
    import scala.collection.JavaConversions._

    val request: PagedRequest[GhRepoTag] = createPagedRequest()
    request.setUri(s"$SEGMENT_REPOS/$org/$repoName/tags")
    request.setType(new TypeToken[java.util.List[GhRepoTag]]() {
    }.getType)
    getAll(request).toList
  }

}
