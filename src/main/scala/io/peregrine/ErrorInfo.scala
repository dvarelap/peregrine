package io.peregrine

import java.io._
import com.twitter.util._

case class ListItem(name: String, value: String)

class ErrorInfo(ex: Exception, req: Request, val status: Int) {
  lazy val location    = req.path
  lazy val env         = config.env()
  lazy val desc        = ex.toString
  lazy val file        = loc.map(_.getFileName)
  lazy val lineNo      = loc.map(_.getLineNumber)
  private lazy val loc = Try(ex.getStackTrace()(0)).toOption

  lazy val reqInfo: Seq[ListItem]  = Seq(
    ListItem("path",           req.path),
    ListItem("method",         req.method.toString),
    // ListItem("protocol",            req.remoteHost),
    ListItem("host",           req.remoteHost),
    ListItem("Content-Type",   req.contentType.getOrElse("None"))
  )

  lazy val params:  Seq[ListItem]  = {
    req.routeParams.map {case (n, v) => ListItem(s":$n", v)}.toSeq ++
    req.params.map {case (n, v) => ListItem(n, v)}.toSeq
  }

  lazy val cookies: Seq[ListItem] = req.cookies.map {case (n, v) => ListItem(v.name, v.value)}.toSeq
  lazy val headers: Seq[ListItem] = req.headerMap.map {case (n, v) => ListItem( n, v.toString)}.toSeq

  lazy val stackTraceLog = {
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    ex.printStackTrace(pw)
    sw.toString
  }
}
