package com.github.dvarelap.peregrine

import com.github.dvarelap.peregrine.test.FlatSpecHelper
import com.twitter.finagle.http.Cookie

class CsrfFilterSpec extends ShouldSpec with FlatSpecHelper {

  class MockView extends View {
    val template = "mock.mustache"
    override def render: String = csrfToken.getOrElse("token_not_found")
  }

  override def server: PeregrineServer = new PeregrineServer {
    addFilter(new CsrfFilter {
      override def generateToken = "TEST_TOKEN"
    })
    register(testController)
  }

  // default behaviour
  object testController extends Controller {

    get("/get_with_no_problems") { req =>
      render.plain("no error").toFuture
    }

    post("/get_with_no_problems") { req =>
      render.plain("no error").toFuture
    }

    post("/post_with_no_csrf_token") { req =>
      render.plain("souldn't see this").toFuture
    }

    post("/to_view") { req =>
      render.view(new MockView()).toFuture
    }

    get("/to_view") { req =>
      render.view(new MockView()).toFuture
    }
  }

  "GET" should "not check for csrf_token" in {
    get("/get_with_no_problems")
    response.code should equal(200)
    response.body should equal("no error")
  }

  "POST with no token" should "fail and show the correct message" in {
    post("/post_with_no_csrf_token")
    response.code should equal(403)
    response.body should equal("CSRF failed")
  }

  "POST with token" should "pass if it's the same token" in {
    post("/get_with_no_problems",
      params  = Map("_csrf_token" -> "TEST_TOKEN"),
      headers = Map("Cookie" -> "_authenticity_token=TEST_TOKEN;")
    )
    response.code should equal(200)
    response.body should equal("no error")
  }

  "POST with token" should "fail if it's not the same token in params" in {
    post("/get_with_no_problems",
      params  = Map("_csrf_token" -> "DIFF_TOKEN"),
      headers = Map("Cookie" -> "_authenticity_token=TEST_TOKEN;")
    )
    response.code should equal(403)
    response.body should equal("CSRF failed")
  }

  "POST with token" should "fail if it's not the same token in cookies" in {
    post("/get_with_no_problems",
      params  = Map("_csrf_token" -> "TEST_TOKEN"),
      headers = Map("Cookie" -> "_authenticity_token=DIFF_TOKEN;")
    )
    response.code should equal(403)
    response.body should equal("CSRF failed")
  }

  "POST to view" should "apply the correct token to view to be rendered" in {
    post("/to_view",
      params  = Map("_csrf_token" -> "TEST_TOKEN"),
      headers = Map("Cookie" -> "_authenticity_token=TEST_TOKEN;")
    )
    response.code should equal(200)
    response.body should equal("TEST_TOKEN")
  }

  "GET to view" should "apply the token to view no matter the GET verb" in {
    get("/to_view")
    response.code should equal(200)
    response.body should equal("TEST_TOKEN")
  }
}
