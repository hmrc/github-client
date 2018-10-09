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

import java.io.File

import com.typesafe.config.ConfigFactory

case class GitApiConfig(user: String, key: String, apiUrl: String)

object GitApiConfig {

  def fromFile(configFilePath: String): GitApiConfig =
    findGithubCredsInFile(new File(configFilePath))
      .getOrElse(throw new RuntimeException(s"could not find github credential in file : $configFilePath"))

  private def findGithubCredsInFile(file: File): Option[GitApiConfig] = {
    val conf = ConfigFactory.parseFile(file)

    for {
      user   <- Some(conf.getString("user"))
      token  <- Some(conf.getString("token"))
      apiUrl <- Some(conf.getString("api-url"))
    } yield GitApiConfig(user, token, apiUrl)
  }
}
