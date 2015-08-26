package com.github.dvarelap.peregrine

import com.twitter.finagle.http.{Request => FinagleRequest, Response => FinagleResponse}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.{Future,Await}
import org.jboss.netty.handler.codec.http.HttpMethod

class CsrfFilter extends SimpleFilter[FinagleRequest, FinagleResponse] with Sessions {

  def apply(req: FinagleRequest, service: Service[FinagleRequest, FinagleResponse]): Future[FinagleResponse] = {
    if (req.method == HttpMethod.GET) {
      for {
        _           <- addCsrfToken(req)
        res         <- service(req)
      } yield res
    } else {
      for {
        csrfToken   <- addCsrfToken(req)
        authToken   <- Future(req.cookies.getOrElse("_authenticity_token", buildVoidCookie).value)
        paramToken  <- Future(req.params.getOrElse("_csrf_token", ""))
        res         <- if (csrfToken == paramToken && csrfToken == authToken) service(req)
                       else Future(ResponseBuilder(403, "CSRF failed"))
      } yield res
    }
  }

  private def addCsrfToken(req: FinagleRequest) = {
    for {
      session     <- session(new Request(req))
      csrfToken   <- session.getOrElseUpdate[String]("_csrf_token", generateToken)
      _           <- Future(req.response.addCookie(buildCsrfCookie(csrfToken)))
    } yield csrfToken
  }

  protected def generateToken = IdGenerator.hexString(32)

  private def buildVoidCookie = new CookieBuilder().name("_authenticity_token").value("").build()
  private def buildCsrfCookie(value: String) = {
    new CookieBuilder().name("_authenticity_token")
      .value(value)
      .httpOnly(httpOnly = true)
      // enables cookies for secure session if cert and key are provided
      .secure(!config.certificatePath().isEmpty && !config.keyPath().isEmpty)
      .build()
  }
}
