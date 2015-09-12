package io.peregrine

import com.google.common.base.Splitter
import com.twitter.finagle.http.{Request => FinagleRequest, Version}
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.handler.codec.http.{DefaultHttpRequest, HttpMethod}

import scala.collection.JavaConversions._
import scala.util.Sorting

class RequestSpec extends ShouldSpec {

  "AcceptOrdering" should "understand accept header ordering" in {
    val accept = "application/xhtml+xml;q=2,application/xml;q=0.9,*/*;q=0.8,text/html;q=0.2"
    val parts = Splitter.on(',').split(accept).toArray
    Sorting.quickSort(parts)(AcceptOrdering)
    parts(3) should equal("text/html;q=0.2")
    parts(2) should equal("*/*;q=0.8")
    parts(1) should equal("application/xml;q=0.9")
    parts(0) should equal("application/xhtml+xml;q=2")
  }

  "Url encoded params" should "work with PUT even with query strings" in {
    val nettyRequest = new DefaultHttpRequest(Version.Http11, HttpMethod.PUT, "/params?q=hi")
    nettyRequest.setContent(ChannelBuffers.wrappedBuffer("test=foo".getBytes))
    nettyRequest.headers().add("Content-Type", "application/x-www-form-urlencoded")
    val finagleRequest = FinagleRequest(nettyRequest)
    val request = new Request(finagleRequest)
    request.params.get("test") should equal(Some("foo"))
    request.params.get("q") should equal(Some("hi"))
  }

}
