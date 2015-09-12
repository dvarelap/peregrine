package io.peregrine.test

import io.peregrine.{Controller, MockApp}
import org.scalatest.{FlatSpec, Matchers}

class SampleSpec extends FlatSpec with Matchers {

  class SampleController extends Controller {
    get("/testing") {
      request => render.body("hello world").status(200).toFuture
    }
  }

  "Sample Use Case" should "allow us to instantiate separate controller for each test" in {
    val app: MockApp = MockApp(new SampleController)

    // When
    val response = app.get("/testing")

    // Then
    response.code should be(200)
    response.body should be("hello world")
  }

  class EchoController extends Controller {
    post("/testing") {
      request => render.body(request.getContentString()).status(200).toFuture
    }

    put("/testing") {
      request => render.body(request.getContentString()).status(200).toFuture
    }

    options("/testing") {
      request => render.body(request.getContentString()).status(200).toFuture
    }
  }

  "SpecHelper" should "allow us to submit an HTTP body for POST method" in {
    val app: MockApp = MockApp(new EchoController)

    val content = "Hello, World!"

    // When
    val response = app.post("/testing", body = content)

    // Then
    response.code should be(200)
    response.body should be(content)
  }

  "SpecHelper" should "allow us to submit an HTTP body for PUT method" in {
    val app: MockApp = MockApp(new EchoController)

    val content = "Hello, World!"

    // When
    val response = app.put("/testing", body = content)

    // Then
    response.code should be(200)
    response.body should be(content)
  }

  /*
   According to http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html, the OPTIONS
   method can support an entity-body.
   */
  "SpecHelper" should "allow us to submit an HTTP body for OPTIONS method" in {
    val app: MockApp = MockApp(new EchoController)

    val content = "Hello, World!"

    // When
    val response = app.options("/testing", body = content)

    // Then
    response.code should be(200)
    response.body should be(content)
  }
}
