package io.peregrine.view

import io.peregrine._
import com.github.mustachejava._
import com.google.common.base.Charsets
import com.twitter.mustache._
import com.twitter.util._
import java.io._
import java.util.concurrent.Executors

/*

class PeregrineMustacheFactory(templatePath: String)
  extends DefaultMustacheFactory(templatePath) {

  def invalidateCaches() : Unit = {
    mustacheCache.clear()
    templateCache.clear()
  }
}
*/

/*
private[peregrine] object MustacheViewFactoryHolder {
  val templatePath  = config.templatePath()
  lazy val factory  = new PeregrineMustacheFactory(templatePath)

  factory.setObjectHandler(new ScalaObjectHandler())
  factory.setExecutorService(Executors.newCachedThreadPool)
}
*/

/*
trait MustacheViewRenderer extends ViewRenderer {

  val format = "mustache"

  lazy val location = MustacheViewFactoryHolder.templatePath
  lazy val factory  = MustacheViewFactoryHolder.factory

  def render(templateName: String, view: View) = {
    if (config.env() == "development") {
      factory.invalidateCaches()
    }

    getPath(templateName) match {
      case None            =>
        throw new FileNotFoundException(s"""Template file [$templateName] not found in [
          ${System.getProperty("user.dir")}/app$location,
          ${getClass.getResource("")}
        ]""")

      case Some(reader)  =>

        val mustache = factory.compile(reader, templateName)
        val output   = new StringWriter
        mustache.execute(output, view).flush()
        output.toString
    }
  }

  def getPath(templateName: String): Option[Reader] = {
    val templatePathName = if (location == "/") s"/$templateName.mustache" else s"$location/$templateName.mustache"
    val path = s"${System.getProperty("user.dir")}$templatePathName"
    val file = new File(path)
    if(file.exists && file.isFile) {
      Some(new BufferedReader(new InputStreamReader(new FileInputStream(file))))
    } else {
      Option(getClass.getResourceAsStream(templatePathName)).map(r => new BufferedReader(new InputStreamReader(r)))
    }
  }
}

object MustacheViewRenderer extends MustacheViewRenderer
*/
