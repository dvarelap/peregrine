package com.github.dvarelap.peregrine

import com.twitter.finagle.http.{Request => FinagleRequest, Response => FinagleResponse}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

class AssetsFilter(assetsPrefix: String) extends SimpleFilter[FinagleRequest, FinagleResponse] {
  def apply(req: FinagleRequest, service: Service[FinagleRequest, FinagleResponse]): Future[FinagleResponse] = {
    if (req.path.startsWith(assetsPrefix)) {
      // TODO dan: improve logging
      Future(render.static(req.path.replace(assetsPrefix, "")).build)
    } else {
      service(req)
    }
  }

  protected def render = new ResponseBuilder()
}
