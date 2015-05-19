package com.github.dvarelap.stilt

import com.twitter.finagle.http.{Request => FinagleRequest}
import org.jboss.netty.handler.codec.http.multipart.{HttpPostRequestDecoder, MixedAttribute, MixedFileUpload}

import scala.collection.JavaConversions._
import scala.collection.mutable

object MultipartParsing {

  def apply(request: FinagleRequest) = {
    var multiParams = mutable.Map[String, MultipartItem]()

    val dec = new HttpPostRequestDecoder(request)
    if (dec.isMultipart) {
      dec.getBodyHttpDatas.foreach {

        case data: MixedFileUpload =>
          val mpi = new MultipartItem(data.get, data.getName, Some(data.getContentType), Some(data.getFilename))
          multiParams += (data.getName -> mpi)

        case data: MixedAttribute =>
          val mpi = new MultipartItem(data.get, data.getName, None, None)
          multiParams += (data.getName -> mpi)

      }
    }

    multiParams
  }

}