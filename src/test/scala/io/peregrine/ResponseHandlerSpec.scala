package io.peregrine

import io.peregrine.test.FlatSpecHelper

class ResponseHandlerSpec extends ShouldSpec with FlatSpecHelper {

  case class User(name: String, age: Int)
  val testController = new Controller {
    get("/json") { _ =>
      json(User("Mark", 23))
    }

    get("/html") { _ =>
      html("<h1>Header</h1>")
    }

    get("/plain") { _ =>
      "plain"
    }

  }

  override def server: PeregrineServer = new PeregrineServer {
    register(testController)
  }

  "json" should "return a formatted response with the correct code" in {
    get("/json")
    response.code should equal(200)
    response.body should equal("""{"name":"Mark","age":23}""")
    response.getHeader("Content-Type") should equal("application/json")
  }

  "html" should "return a formatted response with the correct code" in {
    get("/html")
    response.code should equal(200)
    response.body should equal("""<h1>Header</h1>""")
    response.getHeader("Content-Type") should equal("text/html")
  }

  "plain" should "return a formatted response with the correct code" in {
    get("/plain")
    response.code should equal(200)
    response.body should equal("""plain""")
    response.getHeader("Content-Type") should equal("text/plain")
  }
}
