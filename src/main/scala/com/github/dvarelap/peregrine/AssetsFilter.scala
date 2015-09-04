package com.github.dvarelap.peregrine

import com.twitter.finagle.http.{Request => FinagleRequest, Response => FinagleResponse}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util._

class AssetsFilter extends SimpleFilter[FinagleRequest, FinagleResponse] {
  def apply(req: FinagleRequest, service: Service[FinagleRequest, FinagleResponse]): Future[FinagleResponse] = {
    if (req.path.startsWith(config.assetsPathPrefix())) {
      // TODO dan: improve logging
      val path = req.path.replace(config.assetsPathPrefix(), "")
      Try(render.static(path).build) match {
        case Return(resp) => Future(resp)
        case Throw(t)     => Future(render.notFound.build)
      }
    } else {
      service(req)
    }
  }

  protected def render = new ResponseBuilder()
}
