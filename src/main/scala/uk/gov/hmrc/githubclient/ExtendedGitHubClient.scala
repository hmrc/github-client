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

import java.io.{IOException, InputStream}
import java.lang.reflect.Type
import java.net.{HttpURLConnection, URL}
import java.util

import org.eclipse.egit.github.core.client.{GitHubClient, GitHubRequest, GitHubResponse}

object ExtendedGitHubClient
{
  def apply(url: String, metrics: GithubClientMetrics): ExtendedGitHubClient = {
    try {
      var e = new URL(url).getHost
      if("github.com".equals(e) || "gist.github.com".equals(e)) {
        e = "api.github.com"
      }

      new ExtendedGitHubClient(e, metrics)
    } catch { case ex: IOException =>
      throw new IllegalArgumentException(ex)
    }
  }
}

class ExtendedGitHubClient(hostName: String, metrics: GithubClientMetrics) extends GitHubClient(hostName) {
  private def sendJson[V](request: HttpURLConnection, params: util.HashMap[String, String], typeOf: Type) : Option[V] = {
    this.sendParams(request, params)
    val code = request.getResponseCode

    this.updateRateLimits(request)

    if (this.isOk(code)) {
      if (typeOf != null) this.parseJson(this.getStream(request), typeOf)
      else throw this.createException(this.getStream(request), code, "Unable to parse response")
    }
    else if (this.isEmpty(code)) null
    else throw this.createException(this.getStream(request), code, request.getResponseMessage)
  }

  def put[V](uri: String, params: util.HashMap[String, String], headers: Map[String, String], typeof: Type): Option[V] = {
    val request = headers.foldLeft(this.createPut(uri)) {
      (a: HttpURLConnection, b: (String, String)) => {
        a.setRequestProperty(b._1, b._2)
        a
      }
    }

    this.sendJson(request, params, typeof)
  }

  override def get(request: GitHubRequest): GitHubResponse = metrics.withCounter(request.getUri) { super.get(request) }

  override def getStream(request: HttpURLConnection): InputStream = metrics.withCounter(request.getURL.getPath) { super.getStream(request) }

  override def post[V](uri: String, params: scala.Any, `type`: Type): V = metrics.withCounter(uri) { super.post(uri, params, `type`)}

  override def postStream(uri: String, params: scala.Any): InputStream = metrics.withCounter(uri) { super.postStream(uri, params) }

  override def put[V](uri: String, params: scala.Any, `type`: Type): V = metrics.withCounter(uri) { super.put(uri, params, `type`) }

  override def delete(uri: String, params: scala.Any): Unit = metrics.withCounter(uri) { super.delete(uri, params)}
}
