package io.peregrine

import io.peregrine.test.FlatSpecHelper

class RouteParamsSpec extends FlatSpecHelper {

  class ExampleApp extends Controller {
    get("/:foo") { request =>
      val decoded = request.routeParams.get("foo").get
      render.plain(decoded).toFuture
    }
  }

  val server = new PeregrineServer
  server.register(new ExampleApp)

  "Response" should "contain decoded params" in {
    get("/hello%3Aworld")
    response.body should equal("hello:world")
  }


}
