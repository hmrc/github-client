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

import org.scalatest.Matchers._
import org.scalatest.WordSpec

class ContentTypeSpec extends WordSpec {

  "ContentType.apply" should {
    "return ContentType.Form for 'form'" in {
      ContentType("form") shouldBe ContentType.Form
    }
    "return ContentType.Json for 'json'" in {
      ContentType("json") shouldBe ContentType.Json
    }
    "throw an IllegalArgumentException if value is neither 'json' nor 'form'" in {
      an[IllegalArgumentException] should be thrownBy ContentType("abc")
    }
  }
}

class WebHookNameSpec extends WordSpec {

  "WebHookName.apply" should {
    "return WebHookName.Web if value is 'web'" in {
      WebHookName("web") shouldBe WebHookName.Web
    }
    "return WebHookName if value is different than 'web'" in {
      WebHookName("abc") shouldBe WebHookName.OtherWebHookName("abc")
    }
  }
}
