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

import org.eclipse.egit.github.core.RepositoryHook
import org.eclipse.egit.github.core.service.RepositoryService

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

trait HooksApi {

  import HooksApi._
  import RateLimit._

  protected val repositoryService: RepositoryService

  def findHooks(orgName: OrganisationName, repoName: RepositoryName)(implicit ec: ExecutionContext): Future[Set[Hook]] =
    Future {
      repositoryService
        .getHooks(IdProvider(orgName, repoName))
        .asScala
        .map { repositoryHook =>
          Hook(
            HookId(repositoryHook.getId),
            Url(repositoryHook.getUrl),
            HookName(repositoryHook.getName),
            repositoryHook.isActive,
            HookConfig(
              Url(repositoryHook.getConfig.get("url")),
              repositoryHook.getConfig.asScala.get("content_type") map HookContentType.apply,
              repositoryHook.getConfig.asScala.get("secret") map HookSecret.apply
            )
          )
        }
        .toSet
    }.checkForApiRateLimitError

  def createHook(
    orgName: OrganisationName,
    repoName: RepositoryName,
    config: HookConfig,
    events: Set[HookEvent] = Set.empty,
    active: Boolean        = true)(implicit ec: ExecutionContext): Future[RepositoryHook] =
    Future {
      repositoryService.createHook(
        IdProvider(orgName, repoName),
        NewWebHook(config, active, events)
      )
    }.checkForApiRateLimitError

  def deleteHook(orgName: OrganisationName, repoName: RepositoryName, hookId: HookId)(
    implicit ec: ExecutionContext): Future[Unit] =
    Future {
      repositoryService.deleteHook(
        IdProvider(orgName, repoName),
        hookId.value.toInt
      )
    }.checkForApiRateLimitError
}

object HooksApi {

  private[githubclient] case class NewWebHook(events: Array[String]) extends RepositoryHook

  private[githubclient] object NewWebHook {

    def apply(config: HookConfig, active: Boolean, events: Set[HookEvent] = Set.empty): RepositoryHook =
      NewWebHook(events.map(_.toString).toArray)
        .setName(HookName.Web.value)
        .setConfig(config.toMap)
        .setActive(active)

    private implicit class HookConfigOps(hookConfig: HookConfig) {
      def toMap: java.util.Map[String, String] =
        Seq(
          Option("url" -> hookConfig.url.toString),
          hookConfig.contentType.map(ct => "content_type" -> ct.toString),
          hookConfig.secret.map(s => "secret"             -> s.toString)
        ).flatten.toMap.asJava
    }
  }
}
