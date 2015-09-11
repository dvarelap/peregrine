package com.github.dvarelap.peregrine

import com.twitter.finagle.http.{Request => FinagleRequest, Response => FinagleResponse}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.logging.Logger
import com.twitter.util._
import com.twitter.conversions.time._

class LoggingFilter extends SimpleFilter[FinagleRequest, FinagleResponse] with LoggingFilterHelper {
  def apply(request: FinagleRequest, service: Service[FinagleRequest, FinagleResponse]): Future[FinagleResponse] = {
    applyLogging(request, req => service(req))
  }
}

trait LoggingFilterHelper extends LoggerColors {
  private val logger: Logger = PeregrineLogger.logger()

  private[peregrine] def applyLogging(request: FinagleRequest,
                                      func: FinagleRequest => Future[FinagleResponse]): Future[FinagleResponse] = {
    val elapsed = Stopwatch.start()
    func(request) map { response =>

      val duration = elapsed().inMicroseconds/1000.0
      val mColor = methodColor(response.statusCode)
      logger.info("%s%s %s\"%s\" %s%d %sin %s%.3fms%s",
          ANSI_PURPLE, request.method,
          ANSI_BLUE, request.uri,
          mColor, response.statusCode,
          ANSI_RESET,
          ANSI_GREEN, duration,
          ANSI_RESET
      )
      response
    }
  }

  private[this] def methodColor(statusCode: Int) = statusCode match {
    case code if code >= 200 && code < 300 => ANSI_GREEN
    case code if code >= 400 && code < 500 => ANSI_YELLOW
    case code if code >= 500 && code < 600 => ANSI_RED
    case _                                 => ANSI_RESET
  }
}
