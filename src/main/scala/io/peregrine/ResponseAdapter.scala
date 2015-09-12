package io.peregrine

import com.twitter.util.Future
import com.twitter.finagle.http.{Response => FinagleResponse}

object ResponseAdapter {
  def apply(req: Request, resp: Future[ResponseBuilder]): Future[FinagleResponse] = {
    resp.map(_.build(req))
  }
}

