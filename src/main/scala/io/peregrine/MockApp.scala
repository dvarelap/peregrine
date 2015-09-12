package io.peregrine

import java.net.URLEncoder
import java.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request => FinagleRequest, Response => FinagleResponse}
import com.twitter.util.{Await, Future}
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.handler.codec.http.HttpMethod
import org.jboss.netty.util.CharsetUtil._


import scala.collection.JavaConverters._
import scala.collection.Map

class MockApp(service: Service[FinagleRequest, FinagleResponse]) {

  def buildRequest(
                    method: HttpMethod,
                    path: String,
                    params: Map[String, String] = Map(),
                    headers: Map[String, String] = Map(),
                    body: AnyRef = null
                    ): FinagleRequest = {

    // ensure that we don't have both params and body
    if (body != null && params.nonEmpty) {
      throw new RuntimeException("unable to build request.  You can specify EITHER params OR a body, but not BOTH.")
    }

    val request = FinagleRequest(path, params.toList: _*)
    request.httpRequest.setMethod(method)

    // add headers
    headers.foreach {
      header => request.httpRequest.headers().add(header._1, header._2)
    }

    // apply body
    for (buffer <- toByteArray(body)) {
      request.setContent(ChannelBuffers.wrappedBuffer(buffer))
      request.httpRequest.headers().set("Content-Length", buffer.length.toString)
    }

    request
  }

  def execute(
               method: HttpMethod,
               path: String,
               params: Map[String, String] = Map(),
               headers: Map[String, String] = Map(),
               body: AnyRef = null
               ): MockResult = {
    val request = buildRequest(method, path, params, headers, body)
    execute(request)
  }

  def execute(request: FinagleRequest): MockResult = {
    val response: Future[FinagleResponse] = service(request)
    new MockResult(response)
  }

  /**
   * helper method to url encode a value
   */
  private def encode(value: AnyRef) = URLEncoder.encode(value.toString, "UTF-8")

  /**
   * encodes a map into a url form-encoded string
   */
  private def encodeFormData(params: Map[AnyRef, AnyRef]): String = {
    params.map {
      case (key, value) => encode(key.toString) + "=" + encode(value.toString)
    }.mkString("&")
  }

  /**
   * intelligently convert an AnyRef into a Array[Byte] using the following rules:
   *
   * <ul>
   * <li>value: String         => value.getBytes</li>
   * <li>value: Array[Byte]    => value</li>
   * <li>value: Map[_, _]      => url-encoded form data</li>
   * <li>value: util.Map[_, _] => url-encoded form data</li>
   * <li>value: AnyRef         => uses jackson to attempt to convert this to a json string</li>
   * </ul>
   */
   def toByteArray(body: AnyRef): Option[Array[Byte]] = {
    import MockApp._

    val buffer: Array[Byte] = body match {
      case null => null
      case value: String => value.getBytes
      case value: Array[Byte] => value
      case value: util.Map[_, _] => encodeFormData(value.asInstanceOf[util.Map[AnyRef, AnyRef]].asScala.toMap).getBytes
      case value: Map[_, _] => encodeFormData(value.asInstanceOf[Map[AnyRef, AnyRef]]).getBytes
      case anythingElse => mapper.writeValueAsBytes(anythingElse)
    }
    Option(buffer)
  }

  def get(path: String, params: Map[String, String] = Map(), headers: Map[String, String] = Map(), body: AnyRef = null): MockResult = {
    execute(HttpMethod.GET, path, params, headers, body)
  }

  def post(path: String, params: Map[String, String] = Map(), headers: Map[String, String] = Map(), body: AnyRef = null): MockResult = {
    execute(HttpMethod.POST, path, params, headers, body)
  }

  def put(path: String, params: Map[String, String] = Map(), headers: Map[String, String] = Map(), body: AnyRef = null): MockResult = {
    execute(HttpMethod.PUT, path, params, headers, body)
  }

  def delete(path: String, params: Map[String, String] = Map(), headers: Map[String, String] = Map(), body: AnyRef = null): MockResult = {
    execute(HttpMethod.DELETE, path, params, headers, body)
  }

  def head(path: String, params: Map[String, String] = Map(), headers: Map[String, String] = Map(), body: AnyRef = null): MockResult = {
    execute(HttpMethod.HEAD, path, params, headers, body)
  }

  def patch(path: String, params: Map[String, String] = Map(), headers: Map[String, String] = Map(), body: AnyRef = null): MockResult = {
    execute(HttpMethod.PATCH, path, params, headers, body)
  }

  def options(path: String, params: Map[String, String] = Map(), headers: Map[String, String] = Map(), body: AnyRef = null): MockResult = {
    execute(HttpMethod.OPTIONS, path, params, headers, body)
  }

  def send(request: FinagleRequest) {
    execute(request)
  }
}

object MockApp {
  val mapper = {
    val m = new ObjectMapper()
    m.registerModule(DefaultScalaModule)
    m
  }

  def apply(controller: Controller): MockApp = {
    val server = new PeregrineServer
    server.register(controller)
    apply(server)
  }

  def apply(server: PeregrineServer): MockApp = {
    val appService = new AppService(server.controllers)
    val service = server.allFilters(appService)

    new MockApp(service)
  }
}

class MockResult(val response: Future[FinagleResponse]) {
  def originalResponse        = Await.result(response)
  def status                  = originalResponse.getStatus
  def code                    = originalResponse.getStatus.getCode
  def body                    = originalResponse.getContent.toString(UTF_8)
  def getHeader(name: String) = originalResponse.headers.get(name)
  def getHeaders              = originalResponse.headerMap
}