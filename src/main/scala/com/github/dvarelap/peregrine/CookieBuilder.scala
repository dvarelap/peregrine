package com.github.dvarelap.peregrine

import com.twitter.finagle.http.Cookie
import org.jboss.netty.handler.codec.http.DefaultCookie

class CookieBuilder {
  private var secure  : Option[Boolean] = None
  private var httpOnly: Option[Boolean] = None
  private var name    : Option[String]  = None
  private var value   : Option[String]  = None
  private var path    : Option[String]  = None

  def secure(secure: Boolean): CookieBuilder = {
    this.secure = Option(secure)
    this
  }

  def httpOnly(httpOnly: Boolean): CookieBuilder = {
    this.httpOnly = Option(httpOnly)
    this
  }

  def name(cookieName: String): CookieBuilder = {
    this.name = Option(cookieName)
    this
  }

  def value(value: String): CookieBuilder = {
    this.value = Option(value)
    this
  }

  def path(path: String): CookieBuilder = {
    this.path = Option(path)
    this
  }

  def build(): Cookie = {
    if (name.isEmpty) throw new Exception("name cannot be empty")

    val cookie = new DefaultCookie(name.getOrElse(""), value.getOrElse(""))
    cookie.setHttpOnly(httpOnly.getOrElse(false))
    cookie.setSecure(secure.getOrElse(false))
    cookie.setPath(path.getOrElse("/"))
    new Cookie(cookie)
  }
}
