package com.github.dvarelap.peregrine

trait Params {
  private[peregrine] def param(req: Request)(key: String): Option[String] = {
    req.routeParams.get(key) orElse {
      req.params.get(key)
    }
  }
}
