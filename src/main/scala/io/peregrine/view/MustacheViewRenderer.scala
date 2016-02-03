package io.peregrine.view

import io.peregrine._
import com.github.mustachejava._
import com.google.common.base.Charsets
import com.twitter.mustache._
import com.twitter.util._
import java.io._
import java.util.concurrent.Executors


class PeregrineMustacheFactory(templatePath: String)
  extends DefaultMustacheFactory(templatePath) {

  def invalidateCaches() : Unit = {
    mustacheCache.clear()
    templateCache.clear()
  }
}

private[peregrine] object MustacheViewFactoryHolder {
  val templatePath  = config.templatePath()
  lazy val factory  = new PeregrineMustacheFactory(templatePath)

  factory.setObjectHandler(new ScalaObjectHandler())
  factory.setExecutorService(Executors.newCachedThreadPool)
}

trait MustacheViewRenderer extends ViewRenderer  {
  val format  = "mustache"
  val factory = MustacheViewFactoryHolder.factory
  val location = MustacheViewFactoryHolder.templatePath

  def getPath(templateName: String): Try[Reader]

  def render(templateName: String, view: View): String = {

    val templatePathName = if (location == "/") s"/$templateName.mustache" else s"$location/$templateName.mustache"
    getPath(templatePathName) match {
      case Throw(t)       => throw t
      case Return(reader)  =>
        val mustache = factory.compile(reader, templateName + "asdasdq")
        val output   = new StringWriter
        mustache.execute(output, view).flush()
        output.toString
    }
  }
}

trait LocalTemplateMustacheViewRenderer extends MustacheViewRenderer {
  val templateRoot = config.devTemplateRoot()

  def getPath(templateName: String): Try[Reader] = Try {
    val path             =  s"${System.getProperty("user.dir")}$templateRoot$templateName"
    val file             = new File(path)
    new BufferedReader(new InputStreamReader(new FileInputStream(file)))
  }
}

// trait
trait ResourceMustacheViewRenderer extends MustacheViewRenderer {
  def getPath(templateName: String): Try[Reader] = Try {
    val resource = getClass.getResourceAsStream(templateName)
    val reader   = new BufferedReader(new InputStreamReader(resource))
    reader
  }

  // val path             =  s"${System.getProperty("user.dir")}$resourcesRoot/$templatePathName"
  // val file             = new File(path)
  // if(file.exists && file.isFile) {
  //   Some(new BufferedReader(new InputStreamReader(new FileInputStream(file))))
  // } else {
  //
  // }
}



object ResourceMustacheViewRenderer     extends ResourceMustacheViewRenderer
object LocalTemplateMustacheViewRenderer extends LocalTemplateMustacheViewRenderer
