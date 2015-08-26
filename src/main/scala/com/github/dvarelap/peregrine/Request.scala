package com.github.dvarelap.peregrine

import com.google.common.base.Splitter
import com.twitter.finagle.http.{Request => FinagleRequest, RequestProxy}

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.Sorting

object Request {
  def apply() = new Request(FinagleRequest("/"))
  def apply(path: String) = new Request(FinagleRequest(path))
}

class Request(val request: FinagleRequest) extends RequestProxy {

  var multiParams: mutable.Map[String, MultipartItem] = mutable.Map.empty
  val routeParams: mutable.Map[String, String]        = mutable.Map.empty
  var error      : Option[Throwable]                  = None

  def accepts: Seq[ContentType] = {
    val accept = this.headers().get("Accept")

    if (accept != null) {
      val acceptParts = Splitter.on(',').split(accept).toArray

      Sorting.quickSort(acceptParts)(AcceptOrdering)

      val seq = acceptParts.map { xs =>
        val part = Splitter.on(";q=").split(xs).toArray.head

        ContentType(part).getOrElse(new ContentType.All)
      }.toSeq

      seq
    } else {
      Seq.empty[ContentType]
    }
  }
}

object AcceptOrdering extends Ordering[String] {

  def getWeight(str: String): Double = {
    val parts = Splitter.on(';').split(str).toArray

    if (parts.length < 2) {
      1.0
    } else {
      try {
        Splitter.on("q=").split(parts(1)).toArray.last.toFloat
      } catch {
        case e: java.lang.NumberFormatException =>
          1.0
      }
    }
  }

  def compare(a: String, b: String): Int = {
    getWeight(b) compare getWeight(a)
  }
}
