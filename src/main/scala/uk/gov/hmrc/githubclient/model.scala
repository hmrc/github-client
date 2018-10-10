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

import java.util.Date

case class GhOrganisation(login: String, id: Int = 0)

case class GhTeam(name: String, id: Long)

case class GhRepository(
  name: String,
  description: String,
  id: Long,
  htmlUrl: String,
  fork: Boolean = false,
  createdDate: Long,
  lastActiveDate: Long,
  isPrivate: Boolean,
  language: String
)

case class GhRepoRelease(id: Long, tagName: String, createdAt: Date)

case class GhRepoTag(name: String)

case class OrganisationName(value: String) extends AnyVal {
  override def toString: String = value
}

case class RepositoryName(value: String) extends AnyVal {
  override def toString: String = value
}

case class HookConfig(url: Url, contentType: Option[ContentType] = None, secret: Option[Secret] = None)

case class Url(value: String) extends AnyVal {
  override def toString: String = value
}

sealed abstract class ContentType(val value: String) {
  override def toString: String = value
}

object ContentType {
  case object Form extends ContentType("form")
  case object Json extends ContentType("json")

  def apply(value: String): ContentType = value match {
    case Form.value => ContentType.Form
    case Json.value => ContentType.Json
    case other      => throw new IllegalArgumentException(s"'$other' is not a valid ContentType")
  }
}

case class Secret(value: String) extends AnyVal {
  override def toString: String = value
}

case class WebHook(id: WebHookId, url: Url, name: WebHookName, active: Boolean, config: HookConfig)

case class WebHookId(value: Long) extends AnyVal {
  override def toString: String = value.toString
}

sealed trait WebHookName {
  val value: String
  override def toString: String = value
}

object WebHookName {
  case object Web extends WebHookName {
    override val value: String = "web"
  }
  case class OtherWebHookName(value: String) extends WebHookName

  def apply(value: String): WebHookName = value match {
    case Web.value => Web
    case other     => OtherWebHookName(other)
  }
}
