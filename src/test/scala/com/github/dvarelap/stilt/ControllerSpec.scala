package com.github.dvarelap.peregrine


import com.github.dvarelap.peregrine._
import com.github.dvarelap.peregrine.test.FlatSpecHelper

/* This test is used as the base for generating the
 README.markdown, all new generated apps, and the finatra_example repo
 */

class ControllerSpec extends FlatSpecHelper {


  class TestController  extends Controller {
    get("/test") { req =>
      render.plain("success").toFuture
    }

    get("/foo", "/bar", "/zoo") { req =>
      render.plain("three results").toFuture
    }

    get("/params/:id") { req =>
      val params = for {
        id    <- param(req)("id")
        name  <- param(req)("name")
      } yield id + "-" + name
      render.plain(params.getOrElse("nothing")).toFuture
    }
  }

  val server = new PeregrineServer
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

  "multiple paths" should "should work as expected" in {
    get("/api/foo")
    response.body   should equal ("three results")
    response.code   should equal (200)

    get("/api/bar")
    response.body   should equal ("three results")
    response.code   should equal (200)

    get("/api/zoo")
    response.body   should equal ("three results")
    response.code   should equal (200)
  }

  "params gathering" should "should find first route params, then mutli and then normal params" in {
    get("/api/params/1?name=dan")
    response.body   should equal ("1-dan")
    response.code   should equal (200)

    get("/api/params/1")
    response.body   should equal ("nothing")
    response.code   should equal (200)

  }
}
