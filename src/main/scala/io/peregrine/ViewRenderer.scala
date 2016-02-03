package io.peregrine

import scala.collection.mutable
import io.peregrine.view._

trait ViewRenderer {
  def format: String
  def render(template: String, view: View): String
}

private[peregrine] trait ViewRendererHolder {

  private[this] val map: mutable.Map[String, ViewRenderer] = mutable.Map()

  def register(format: String, renderer: ViewRenderer): Unit = map.put(format, renderer)

  def find(format: String): Option[ViewRenderer] = map.get(format)

  def clear: Unit = map.clear
}

object ViewRendererHolder extends ViewRendererHolder

trait ViewsEnabled extends PeregrineLogger {

  lazy val log = logger();
  // Default renderers
  if (config.env() == "dev") {
    registerViewRenderer(LocalTemplateMustacheViewRenderer)
    log.info("mustache view renderer LocalTemplateMustacheViewRenderer was regitered")
  } else {
    registerViewRenderer(ResourceMustacheViewRenderer)
    log.info("mustache view renderer ResourceMustacheViewRenderer was regitered")
  }


  def registerViewRenderer(renderer: ViewRenderer): Unit = {
    if (renderer.format == null || renderer.format == "") {
      throw new Exception(s"Format ${renderer.format} not valid")
    }
    ViewRendererHolder.register(renderer.format, renderer)
  }

  def clearViewRenderers(): Unit = ViewRendererHolder.clear
}

trait ViewsEnabledForTesting  extends ViewsEnabled
