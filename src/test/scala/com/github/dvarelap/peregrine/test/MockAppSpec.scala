package com.github.dvarelap.peregrine.test

import com.github.dvarelap.peregrine.{Controller, MockApp}
import com.twitter.finagle.http.Request
import org.jboss.netty.handler.codec.http.HttpMethod
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class MockAppSpec extends FlatSpec with Matchers {
  val server = MockApp(new Controller{})

  "#toByteArray" should "directly convert String to Array[Byte]" in {
    val value = "hello world"
    server.toByteArray(value).get should be(value.getBytes)
  }

  it should "also directly convert Array[Byte] to Array[Byte]" in {
    val value = "hello world".getBytes
    server.toByteArray(value).get should be(value)
  }

  it should "convert Map[String, String] to url-encoded form data" in {
    val value = Map("hello" -> "world")
    server.toByteArray(value).get should be("hello=world".getBytes)
  }

  it should "convert util.Map[String, String] to url-encoded form data" in {
    val value = Map("hello" -> "world").asJava
    server.toByteArray(value).get should be("hello=world".getBytes)
  }

  it should "convert null to None" in {
    server.toByteArray(null) should be(None)
  }

  it should "attempt to convert other objects to a json equivalent" in {
    val sample = Sample("matt", "matt@does-not-exist.com")
    server.toByteArray(sample).get should be( """{"name":"matt","email":"matt@does-not-exist.com"}""".getBytes)
  }

  "#buildRequest" should "apply body if present" in {
    val sample = Sample("matt", "matt@does-not-exist.com")

    // When
    val request: Request = server.buildRequest(HttpMethod.POST, "/", body = sample)

    // Then
    request.contentString should be(MockApp.mapper.writeValueAsString(sample))
  }

  it should "not allow both params AND a non-null body in the same request" in {
    val sample = Sample("matt", "matt@does-not-exist.com")
    a [RuntimeException] should be thrownBy {
      server.buildRequest(HttpMethod.POST, "/", params = Map("hello" -> "world"), body = sample)
    }
  }

  case class Sample(name: String, email: String)
}
