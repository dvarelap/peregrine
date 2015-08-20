package com.github.dvarelap.stilt

import java.io._

case class MultipartItem(
                          data: Array[Byte],
                          name: String,
                          contentType: Option[String],
                          filename: Option[String]) {

  def value: String = new String(data)

  def writeToFile(path: String) {
    val fileout = new FileOutputStream(path)

    fileout.write(data)
    fileout.close()
  }
}
