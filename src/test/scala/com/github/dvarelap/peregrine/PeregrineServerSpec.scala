package com.github.dvarelap.peregrine

import com.github.dvarelap.peregrine.test.FlatSpecHelper
import com.twitter.finagle.http.Response
import com.twitter.finagle.{Service, SimpleFilter, http}
import com.twitter.util.Future

class TestApp extends Controller {

  get("/hey") {
    request => render.plain("hello").toFuture
  }

}

class TestFilter extends SimpleFilter[http.Request, Response] {

  def apply(request: http.Request, service: Service[http.Request, Response]): Future[Response] = {
    service(request) map { response =>
       response.setContentString("hello from filter")
       response
    }
  }

}

class PeregrineServerSpec extends FlatSpecHelper {

  val server = new PeregrineServer
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
