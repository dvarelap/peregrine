package com.github.dvarelap.stilt


import com.github.dvarelap.stilt._
import com.github.dvarelap.stilt.test.FlatSpecHelper

/* This test is used as the base for generating the
 README.markdown, all new generated apps, and the finatra_example repo
 */

class ControllerSpec extends FlatSpecHelper {


  class TestController  extends Controller {
    get("/test") { req =>
      render.plain("success").toFuture
    }
  }

  val server = new StiltServer
  server.register(new TestController, "/api")

  "GET /test" should "respond 404" in {
    get("/test")
    response.code   should equal (404)
  }

  "GET /api/test" should "respond 200" in {
    get("/api/test")
    response.body   should equal ("success")
    response.code   should equal (200)
  }
}
