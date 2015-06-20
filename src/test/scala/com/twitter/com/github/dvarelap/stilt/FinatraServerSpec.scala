/**
 * Copyright (C) 2012 Twitter Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.twitter.com.github.dvarelap.stilt

import com.github.dvarelap.stilt.{Controller, StiltServer}
import com.twitter.com.github.dvarelap.stilt.test.FlatSpecHelper
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future

class TestApp extends Controller {

  get("/hey") {
    request => render.plain("hello").toFuture
  }

}

class TestFilter extends SimpleFilter[Request, Response] {

  def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    service(request) map { response =>
       response.setContentString("hello from filter")
       response
    }
  }

}

class FinatraServerSpec extends FlatSpecHelper {

  val server = new StiltServer
  server.register(new TestApp)

  "app" should "register" in {

    new Runnable {
      def run() = server.start()
    }

    get("/hey")
    response.body should equal("hello")

    server.stop()

  }

  "app" should "add Filter" in {

    server.addFilter(new TestFilter)

    get("/hey")
    response.body should equal("hello from filter")
  }

}
