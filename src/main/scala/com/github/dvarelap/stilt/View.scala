package com.github.dvarelap.stilt

import java.util.concurrent.Callable

trait View extends Callable[String] {
  def render:      String
  def call:        String         = render
  def contentType: Option[String] = None
}


