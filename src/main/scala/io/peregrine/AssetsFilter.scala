package io.peregrine

import com.twitter.finagle.http.{Request => FinagleRequest, Response => FinagleResponse}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util._

class AssetsFilter extends SimpleFilter[FinagleRequest, FinagleResponse] with LoggingFilterHelper {
  val logger = PeregrineLogger.logger()
  def apply(req: FinagleRequest, service: Service[FinagleRequest, FinagleResponse]): Future[FinagleResponse] = {

    if (req.path.startsWith(config.assetsPathPrefix())) {
      if (config.debugAssets()) {
        applyLogging(req, request => applyAssets(request))
      } else {
        applyAssets(req)
      }
    } else {
      service(req)
    }
  }

  private[this] def applyAssets(req: FinagleRequest) = {
    val path = req.path.replace(config.assetsPathPrefix(), "")
    Try(render.static(path).build) match {
      case Return(resp) => Future(resp)
      case Throw(t)     => Future(render.notFound.build)
    }
  }

  protected def render = new ResponseBuilder()
}
