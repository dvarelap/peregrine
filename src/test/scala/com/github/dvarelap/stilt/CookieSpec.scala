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
package com.github.dvarelap.stilt

import com.github.dvarelap.stilt.test.FlatSpecHelper
import org.jboss.netty.handler.codec.http._


class CookieApp extends Controller {

  get("/sendCookie") {
    request => render.plain("get:path").cookie("Foo", "Bar").toFuture
  }

  get("/sendAdvCookie") {
    val c = new DefaultCookie("Biz", "Baz")
    c.setSecure(true)
    request => render.plain("get:path").cookie(c).toFuture
  }

}

class CookieSpec extends FlatSpecHelper {

  val server = new StiltServer
  server.register(new CookieApp)

  "basic k/v cookie" should "have Foo:Bar" in {
    get("/sendCookie")
    response.getHeader("Set-Cookie") should be ("Foo=Bar")
  }

  "advanced Cookie" should "have Biz:Baz&Secure=true" in {
    get("/sendAdvCookie")
    response.getHeader("Set-Cookie") should be ("Biz=Baz; Secure")
  }

}
