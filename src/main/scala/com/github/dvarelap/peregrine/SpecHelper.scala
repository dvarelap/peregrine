package com.github.dvarelap.peregrine

import com.twitter.finagle.http.{Request => FinagleRequest, Response => FinagleResponse}
import com.twitter.util.{Await, Future}
import org.jboss.netty.handler.codec.http.HttpMethod
import org.jboss.netty.util.CharsetUtil.UTF_8

import scala.collection.Map

class MockResponse(val originalResponse: FinagleResponse) {

  def status                  = originalResponse.getStatus()
  def code                    = originalResponse.getStatus().getCode
  def body                    = originalResponse.getContent().toString(UTF_8)
  def getHeader(name: String) = originalResponse.headers().get(name)
  def getHeaders              = originalResponse.headerMap

}

trait SpecHelper {

  def response  = new MockResponse(Await.result(lastResponse))
  var lastResponse: Future[FinagleResponse] = null

  def server: PeregrineServer

  def get(path:String, params:Map[String,String]=Map(), headers:Map[String,String]=Map()) {
    executeRequest(HttpMethod.GET,path,params,headers)
  }

  def post(path:String, params:Map[String,String]=Map(), headers:Map[String,String]=Map(), body:AnyRef=null) {
    executeRequest(HttpMethod.POST,path,params,headers,body)
  }

  def put(path:String, params:Map[String,String]=Map(), headers:Map[String,String]=Map(), body:AnyRef=null) {
    executeRequest(HttpMethod.PUT,path,params,headers,body)
  }

  def delete(path:String, params:Map[String,String]=Map(), headers:Map[String,String]=Map()) {
    executeRequest(HttpMethod.DELETE,path,params,headers)
  }

  def head(path:String,params:Map[String,String]=Map(), headers:Map[String,String]=Map()) {
    executeRequest(HttpMethod.HEAD,path,params,headers)
  }

  def patch(path:String, params:Map[String,String]=Map(), headers:Map[String,String]=Map()) {
    executeRequest(HttpMethod.PATCH,path,params,headers)
  }

  def options(path:String, params:Map[String,String]=Map(), headers:Map[String,String]=Map(), body:AnyRef=null) {
    executeRequest(HttpMethod.OPTIONS,path,params,headers,body)
  }

  def send(request: FinagleRequest) {
    executeRequest(request)
  }

  private def executeRequest(
                              method: HttpMethod,
                              path: String,
                              params: Map[String, String] = Map(),
                              headers: Map[String,String] = Map(),
                              body: AnyRef = null
                              ) {
    val app = MockApp(server)
    val result: MockResult = app.execute(method = method, path = path, params = params, headers = headers, body = body)
    lastResponse = result.response
  }

  private def executeRequest(request: FinagleRequest) {
    val app = MockApp(server)
    val result: MockResult = app.execute(request)
    lastResponse = result.response
  }

}

