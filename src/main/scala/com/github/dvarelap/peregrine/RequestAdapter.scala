package com.github.dvarelap.peregrine

import com.twitter.finagle.http.{Request => FinagleRequest}
import org.jboss.netty.handler.codec.http.HttpMethod
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder

/**
 * Adapts a FinagleRquest to a FinatraRequest
 */
object RequestAdapter {

  def apply(rawRequest: FinagleRequest): Request = {
    val request = new Request(rawRequest)

    request.getContent().markReaderIndex()

    if (request.method == HttpMethod.POST && HttpPostRequestDecoder.isMultipart(request)) {
      request.multiParams = MultipartParsing(request)
    }

    request.getContent().resetReaderIndex()

    request
  }
}
