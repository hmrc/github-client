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

import java.time.Duration
import java.util.concurrent._

import play.api.libs.json.{JsValue, Json, Reads}
import play.api.libs.ws.ning.{NingAsyncHttpClientConfigBuilder, NingWSClient}
import play.api.libs.ws.{DefaultWSClientConfig, WSAuthScheme, WSRequestHolder, WSResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


object WsClient {

  val corePoolSize = Integer.getInteger("github.client.threadPoolCoreSize", 10)
  val maximumPoolSize = Integer.getInteger("github.client.threadPoolMaxSize", 10)
  val keepAliveTime = Integer.getInteger("github.client.threadPoolKeepAliveTimeInSec", 60).toLong

  def asyncHttpClientConfig = {

    println(s"github.client.threadPoolCoreSize : $corePoolSize")
    println(s"github.client.threadPoolMaxSize : $maximumPoolSize")
    println(s"github.client.threadPoolKeepAliveTimeInSec : $keepAliveTime")

    val executor = new ThreadPoolExecutor(
      corePoolSize, //corePoolSize
      maximumPoolSize, //maximumPoolSize
      keepAliveTime, //keepAliveTime
      TimeUnit.SECONDS, //unit
      new LinkedBlockingDeque[Runnable](), //workQueue
      Executors.defaultThreadFactory(), //threadFactory
      new ThreadPoolExecutor.CallerRunsPolicy() //rejected task handler
    )
    executor.allowCoreThreadTimeOut(true)


    val clientConfig = new DefaultWSClientConfig()
    val secureDefaults = new NingAsyncHttpClientConfigBuilder(clientConfig).build()
    val builder = new com.ning.http.client.AsyncHttpClientConfig.Builder(secureDefaults)
    builder.setCompressionEnabled(true)
    builder.setExecutorService(executor)
    builder.setIdleConnectionTimeoutInMs(Duration.ofMinutes(1).toMillis.toInt)

    builder.build()
  }

  val client = new NingWSClient(asyncHttpClientConfig)

}

class HttpClient(user: String, apiKey: String) {


  val ws = WsClient.client

  def close() = ws.close()

  def get[T](url: String)(implicit ec: ExecutionContext, r: Reads[T]): Future[T] = withErrorHandling("GET", url) {
    case s if s.status >= 200 && s.status < 300 =>
      Try {
        s.json.as[T]
      } match {
        case Success(a) => a
        case Failure(e) =>
          Log.error(s"Error paring response failed body was: ${s.body} root url : $url")
          throw e
      }
    case res =>
      throw new RuntimeException(s"Unexpected response status : ${res.status}  calling url : $url response body : ${res.body}")
  }

  def delete(url: String)(implicit ec: ExecutionContext): Future[WSResponse] = {
    withErrorHandling("DELETE", url) {
      case rs if (rs.status >= 200 && rs.status < 300) || (rs.status == 404) => rs
      case _@rs =>
        val msg = s"Didn't get expected status code when writing to Github. Got status ${rs.status}: DELETE $url ${rs.body}"
        Log.error(msg)
        throw new RuntimeException(msg)
    }
  }


  def post[T](url: String, body: Option[String] = None)(implicit ec: ExecutionContext, r: Reads[T]): Future[T] = {
    postWithStringResponse(url, body).map(x => Json.parse(x).as[T])
  }

  def postWithStringResponse(url: String, body: Option[String] = None)(implicit ec: ExecutionContext): Future[String] = {
    withErrorHandling("POST", url, body.map(Json.parse)) {
      case rs if rs.status >= 200 && rs.status < 300 => rs.body
      case _@rs =>
        val msg = s"Didn't get expected status code when writing to Github. Got status ${rs.status}: POST $url ${rs.body}"
        Log.error(msg)
        throw new RuntimeException(msg)
    }
  }


  private def withErrorHandling[T](method: String, url: String, body: Option[JsValue] = None)(f: WSResponse => T)(implicit ec: ExecutionContext): Future[T] = {
    buildCall(method, url, body).execute().transform(
      f,
      e =>
        throw new RuntimeException(s"Error connecting  $url", e)
    )
  }

  private def buildCall(method: String, url: String, body: Option[JsValue] = None): WSRequestHolder = {
    val req = ws.url(url)
      .withMethod(method)
      .withAuth(user, apiKey, WSAuthScheme.BASIC)
      .withHeaders("content-type" -> "application/json")

    body.map { b =>
      req.withBody(b)
    }.getOrElse(req)
  }

  def head(url: String)(implicit ec: ExecutionContext): Future[Int] = withErrorHandling("HEAD", url)(_.status)


}
