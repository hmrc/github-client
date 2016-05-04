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

package uk.gov.hmrc.gitclient

import java.io.File
import java.nio.file.Path

case class GitApiConfig(user: String, key: String, apiUrl :String)

object GitApiConfig {

   def fromFile(configFilePath: String): GitApiConfig = {
    findGithubCredsInFile(new File(configFilePath).toPath).getOrElse(throw new RuntimeException(s"could not find github credential in file : $configFilePath"))
  }

  private def findGithubCredsInFile(file: Path): Option[GitApiConfig] = {
    val conf = new ConfigFile(file)

    for {
      user <- conf.get("user")
      token <- conf.get("token")
      apiUrl <- conf.get("api-url")
    } yield GitApiConfig(user, token, apiUrl)
  }

}


