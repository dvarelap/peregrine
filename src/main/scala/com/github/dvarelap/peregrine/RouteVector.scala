package com.github.dvarelap.peregrine

import com.twitter.util.Future
import org.jboss.netty.handler.codec.http._

case class Route(method: HttpMethod, path: String, pattern: PathPattern, callback: Request => Future[ResponseBuilder])

class RouteVector {
  var vector = Vector[Route]()

  def add(x: Route) {
    vector = x +: vector
  }

  def withPrefix(prefix: String) = {
    vector = vector.map { r =>
      val composedPath = prefix + r.path
      r.copy(
        path    = composedPath,
        pattern = SinatraPathPatternParser(composedPath)
      )
    }
  }
}
