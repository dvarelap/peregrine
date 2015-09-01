package com.github.dvarelap.peregrine

import java.util.concurrent.Callable

trait View extends Callable[String] {
  def render: String
  def call: String                = render
  def contentType: Option[String] = None
}

trait CsrfView {
  def _csrf: String
}
