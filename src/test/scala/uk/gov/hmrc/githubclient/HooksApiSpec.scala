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

import com.google.gson.Gson
import org.eclipse.egit.github.core.service.RepositoryService
import org.eclipse.egit.github.core.{IRepositoryIdProvider, RepositoryHook}
import org.mockito.{ArgumentCaptor, ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.githubclient.HooksApi.{EditWebHook, NewWebHook}
import uk.gov.hmrc.githubclient.HookEvent.{PullRequest, Push}
import uk.gov.hmrc.githubclient.HookName.NonWebHookName

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global

class HooksApiSpec
  extends AnyWordSpecLike
     with Matchers
     with MockitoSugar
     with ArgumentMatchersSugar
     with ScalaFutures {

  "createWebHook" should {
    "add the given hook to the repository" in new Setup {
      val receivedHook: RepositoryHook = new RepositoryHook()
        .setId(1)
        .setName("web")
        .setActive(true)
        .setUrl("http://github/hook/url/1")
        .setConfig(Map("url" -> "http://webhook.url/1", "content_type" -> "form").asJava)

      when(repositoryServiceMock.createHook(any[IRepositoryIdProvider], any[RepositoryHook]))
        .thenReturn(receivedHook)

      hooksApi
        .createWebHook(organisation, repoName, config, events, active = false)
        .futureValue shouldBe Hook(
          id     = HookId(receivedHook.getId),
          url    = Url(receivedHook.getUrl),
          name   = HookName.Web,
          active = receivedHook.isActive,
          config = HookConfig(
                     Url(receivedHook.getConfig.get("url")),
                     Some(HookContentType(receivedHook.getConfig.get("content_type")))
                   )
        )

      val repositoryIdProviderCaptor = ArgumentCaptor.forClass(classOf[IRepositoryIdProvider])
      val repositoryHookCaptor = ArgumentCaptor.forClass(classOf[NewWebHook])
      verify(repositoryServiceMock).createHook(repositoryIdProviderCaptor.capture(), repositoryHookCaptor.capture())

      repositoryIdProviderCaptor.getValue.generateId() shouldBe s"$organisation/$repoName"

      val hook = repositoryHookCaptor.getValue
      hook.getConfig.asScala("url") shouldBe "jenkins_hook_url"
      hook.getName                  shouldBe "web"
      hook.isActive                 shouldBe false
      hook.events                   should contain only (Push.toString, PullRequest.toString)
    }

    "handle API rate limit error" in new Setup {
      val runtimeException = new RuntimeException("api rate limit exceeded")
      when(repositoryServiceMock.createHook(any[IRepositoryIdProvider], any[RepositoryHook]))
        .thenThrow(runtimeException)

      hooksApi.createWebHook(organisation, repoName, config).failed.futureValue shouldBe APIRateLimitExceededException(runtimeException)
    }
  }

  "editWebHook" should {
    "edit the given hook to the repository" in new Setup {
      private val hookId = 123456

      private val hookName = "web"
      val receivedHook: RepositoryHook = new RepositoryHook()
        .setId(hookId)
        .setName(hookName)
        .setActive(true)
        .setUrl("http://github/hook/url/1")
        .setConfig(Map("url" -> "http://webhook.url/1", "content_type" -> "form").asJava)

      when(repositoryServiceMock.editHook(any[IRepositoryIdProvider], any[RepositoryHook]))
        .thenReturn(receivedHook)

      hooksApi
        .editWebHook(organisation, repoName, Hook(HookId(hookId), Url("hook.com"), HookName(hookName), true, config), events)
        .futureValue shouldBe Hook(HookId(receivedHook.getId),
                                   Url(receivedHook.getUrl),
                                   HookName.Web,
                                   receivedHook.isActive,
                                   HookConfig(
                                     Url(receivedHook.getConfig.get("url")),
                                     Some(HookContentType(receivedHook.getConfig.get("content_type")))
                                   ))
      val repositoryIdProviderCaptor = ArgumentCaptor.forClass(classOf[IRepositoryIdProvider])
      val repositoryHookCaptor = ArgumentCaptor.forClass(classOf[EditWebHook])
      verify(repositoryServiceMock).editHook(repositoryIdProviderCaptor.capture(), repositoryHookCaptor.capture())

      repositoryIdProviderCaptor.getValue.generateId() shouldBe s"$organisation/$repoName"

      val hook = repositoryHookCaptor.getValue
      hook.getId                    shouldBe hookId
      hook.getConfig.asScala("url") shouldBe "jenkins_hook_url"
      hook.getName                  shouldBe hookName
      hook.isActive                 shouldBe true
      hook.events                   should contain only (Push.toString, PullRequest.toString)
    }

    "handle API rate limit error" in new Setup {
      val runtimeException = new RuntimeException("api rate limit exceeded")
      when(repositoryServiceMock.editHook(any[IRepositoryIdProvider], any[RepositoryHook]))
        .thenThrow(runtimeException)

      hooksApi.editWebHook(organisation, repoName, Hook(HookId(12345), Url("hook.com"), HookName("web"), true, config), events)
        .failed.futureValue shouldBe APIRateLimitExceededException(runtimeException)
    }
  }

  "findHooks" should {
    "return all repository's hooks" in new Setup {
      when(repositoryServiceMock.getHooks(any[IRepositoryIdProvider]))
        .thenReturn(
          Seq(
            new RepositoryHook()
              .setId(1)
              .setName("web")
              .setActive(true)
              .setUrl("http://github/hook/url/1")
              .setConfig(Map("url" -> "http://webhook.url/1", "content_type" -> "form").asJava),
            new RepositoryHook()
              .setId(2)
              .setName("non-web")
              .setActive(false)
              .setUrl("http://github/hook/url/2")
              .setConfig(Map("url" -> "http://webhook.url/2", "content_type" -> "json").asJava)
          ).asJava
        )

      hooksApi
        .findHooks(organisation, repoName)
        .futureValue shouldBe Set(
          Hook(
            id     = HookId(1),
            url    = Url("http://github/hook/url/1"),
            name   = HookName.Web,
            active = true,
            config = HookConfig(Url("http://webhook.url/1"), Some(HookContentType.Form))
          ),
          Hook(
            id     = HookId(2),
            url    = Url("http://github/hook/url/2"),
            name   = NonWebHookName("non-web"),
            active = false,
            config = HookConfig(Url("http://webhook.url/2"), Some(HookContentType.Json))
          )
        )

      val repositoryIdProviderCaptor = ArgumentCaptor.forClass(classOf[IRepositoryIdProvider])
      verify(repositoryServiceMock).getHooks(repositoryIdProviderCaptor.capture())
      repositoryIdProviderCaptor.getValue.generateId() shouldBe s"$organisation/$repoName"
    }

    "handle API rate limit error" in new Setup {
      val runtimeException = new RuntimeException("api rate limit exceeded")
      when(repositoryServiceMock.getHooks(any[IRepositoryIdProvider]))
        .thenThrow(runtimeException)

      hooksApi.findHooks(organisation, repoName).failed.futureValue shouldBe APIRateLimitExceededException(runtimeException)
    }

    "ignore travis webhooks" in new Setup {
      /*
       * Existing integration with Travis was achieved by using Github Services which is now deprecated.
       * Travis webhooks started to appear in the array of webhooks at some point but the content of each
       * webhook is different (has different config) than all regular webhooks. We are filtering out travis
       * webhooks.
       */

      when(repositoryServiceMock.getHooks(any[IRepositoryIdProvider]))
        .thenReturn(
          Seq(
            new RepositoryHook()
              .setId(1)
              .setName("web")
              .setActive(true)
              .setUrl("http://github/hook/url/1")
              .setConfig(Map("url" -> "http://webhook.url/1", "content_type" -> "form").asJava),
            new RepositoryHook()
              .setId(2)
              .setName("travis")
              .setActive(true)
              .setUrl("http://github/hook/url/2")
              .setConfig(Map("domain" -> "notify.travis-ci.org", "token" -> "*****").asJava)
          ).asJava
        )

      hooksApi
        .findHooks(organisation, repoName)
        .futureValue shouldBe Set(
          Hook(
            id     = HookId(1),
            url    = Url("http://github/hook/url/1"),
            name   = HookName.Web,
            active = true,
            config = HookConfig(Url("http://webhook.url/1"), Some(HookContentType.Form))
          )
        )

      val repositoryIdProviderCaptor = ArgumentCaptor.forClass(classOf[IRepositoryIdProvider])
      verify(repositoryServiceMock).getHooks(repositoryIdProviderCaptor.capture())
      repositoryIdProviderCaptor.getValue.generateId() shouldBe s"$organisation/$repoName"
    }
  }

  "deleteHook" should {
    val hookId = HookId(1)

    "delete repository hook and return nothing" in new Setup {
      hooksApi
        .deleteHook(organisation, repoName, hookId)
        .futureValue shouldBe (())

      val repositoryIdProviderCaptor = ArgumentCaptor.forClass(classOf[IRepositoryIdProvider])
      val hookIdCaptor = ArgumentCaptor.forClass(classOf[Int])
      verify(repositoryServiceMock).deleteHook(repositoryIdProviderCaptor.capture(), hookIdCaptor.capture())
      repositoryIdProviderCaptor.getValue.generateId() shouldBe s"$organisation/$repoName"
      hookIdCaptor.getValue                            shouldBe hookId.value.toInt
      }

    "handle API rate limit error" in new Setup {
      val runtimeException = new RuntimeException("api rate limit exceeded")
      when(repositoryServiceMock.deleteHook(any[IRepositoryIdProvider], any[Int]))
        .thenThrow(runtimeException)

      hooksApi.deleteHook(organisation, repoName, hookId).failed.futureValue shouldBe APIRateLimitExceededException(runtimeException)
    }
  }

  "NewWebHook" should {
    "get serialized with all the fields" in new Setup {
      val hookConfig = HookConfig(
        Url("jenkins_hook_url"),
        Some(HookContentType.Form),
        Some(HookSecret("some_secret"))
      )

      private val repositoryHook: RepositoryHook = NewWebHook(hookConfig, active = true, events)
      val json: JsValue = Json.parse(new Gson().toJson(repositoryHook))

      (json \ "name").as[String]                    shouldBe "web"
      (json \ "events").as[Seq[String]].toSet       shouldBe events.map(_.toString)
      (json \ "active").as[Boolean]                 shouldBe true
      (json \ "config" \ "url").as[String]          shouldBe "jenkins_hook_url"
      (json \ "config" \ "content_type").as[String] shouldBe HookContentType.Form.toString
      (json \ "config" \ "secret").as[String]       shouldBe "some_secret"
    }

    "get serialized when hook config have Nones where possible" in new Setup {
      val hookConfig = HookConfig(
        Url("jenkins_hook_url"),
        contentType = None,
        secret      = None
      )

      val json: JsValue = Json.parse(new Gson().toJson(NewWebHook(hookConfig, active = true, events)))

      (json \ "name").as[String]                       shouldBe "web"
      (json \ "events").as[Seq[String]].toSet          shouldBe events.map(_.toString)
      (json \ "active").as[Boolean]                    shouldBe true
      (json \ "config" \ "url").as[String]             shouldBe "jenkins_hook_url"
      (json \ "config" \ "content_type").asOpt[String] shouldBe None
      (json \ "config" \ "secret").asOpt[String]       shouldBe None
    }
  }


  private trait Setup {
    val organisation           = OrganisationName("HMRC")
    val repoName               = RepositoryName("test-repo")
    val events: Set[HookEvent] = Set(Push, PullRequest)
    val config                 = HookConfig(Url("jenkins_hook_url"))

    val repositoryServiceMock: RepositoryService = mock[RepositoryService]

    def hooksApi: HooksApi = new HooksApi {
      override protected val repositoryService: RepositoryService = repositoryServiceMock
    }
  }
}
