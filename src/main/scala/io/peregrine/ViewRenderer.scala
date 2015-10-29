package io.peregrine

import scala.collection.mutable

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

trait ViewsEnabled {

  // Default renderers
  registerViewRenderer(HbsViewRenderer)

  def registerViewRenderer(renderer: ViewRenderer): Unit = {
    if (renderer.format == null || renderer.format == "") {
      throw new Exception(s"Format ${renderer.format} not valid")
    }
    ViewRendererHolder.register(renderer.format, renderer)
  }

  def clearViewRenderers(): Unit = ViewRendererHolder.clear
}

trait ViewsEnabledForTesting  extends ViewsEnabled
