package com.github.dvarelap.peregrine

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request => FinagleRequest, Response => FinagleResponse}
import com.twitter.util.{Await, Future}

class AppService(controllers: ControllerCollection)
  extends Service[FinagleRequest, FinagleResponse] {

  def render: ResponseBuilder = new ResponseBuilder

  def apply(rawRequest: FinagleRequest): Future[FinagleResponse] = {
    val adaptedRequest = RequestAdapter(rawRequest)

    try {
      attemptRequest(rawRequest).handle {
        case t: Throwable =>
          Await.result(
            ErrorHandler(adaptedRequest, t, controllers)
          )
      }
    } catch {
      case e: Exception =>
        ErrorHandler(adaptedRequest, e, controllers)
    }
  }

  def attemptRequest(rawRequest: FinagleRequest): Future[FinagleResponse] = {
    val adaptedRequest = RequestAdapter(rawRequest)

    controllers.dispatch(rawRequest) match {
      case Some(response) =>
        response.asInstanceOf[Future[FinagleResponse]]
      case None           =>
        ResponseAdapter(adaptedRequest, controllers.notFoundHandler(adaptedRequest))
    }
  }
}
