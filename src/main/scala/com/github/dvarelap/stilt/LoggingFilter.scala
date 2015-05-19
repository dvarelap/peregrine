package com.github.dvarelap.stilt

import com.twitter.finagle.http.{Request => FinagleRequest, Response => FinagleResponse}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.logging.Logger
import com.twitter.util.Future

class LoggingFilter extends SimpleFilter[FinagleRequest, FinagleResponse] {
  private val logger: Logger = Logger.get("stilt")

  def apply(request: FinagleRequest, service: Service[FinagleRequest, FinagleResponse]): Future[FinagleResponse] = {
    val start = System.currentTimeMillis()
    service(request) map { response =>
      val end = System.currentTimeMillis()
      val duration = end - start
      logger.info("%s %s %d %dms".format(request.method, request.uri, response.statusCode, duration))
      response
    }
  }
}
