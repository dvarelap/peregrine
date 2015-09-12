package io.peregrine

import io.peregrine.test.FlatSpecHelper
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

  val server = new PeregrineServer
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
