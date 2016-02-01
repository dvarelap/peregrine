package io.peregrine
import io.peregrine.jackson._

trait RenderBuilder {
  def render: ResponseBuilder        = new ResponseBuilder(jsonSerializer)
  val jsonSerializer: JsonSerializer = DefaultJacksonJsonSerializer
}

trait ResponseHandler extends RenderBuilder {

  def json[T](jsonObj: T): ResponseBuilder    = render.json(jsonObj)
  def html(htmlCode: String): ResponseBuilder = render.html(htmlCode)
  def static(file: String): ResponseBuilder   = render.static(file)
  def plain(s: String): ResponseBuilder       = render.plain(s)
  def status(s: Int): ResponseBuilder         = render.status(s)
}

trait ViewResponseHandler extends RenderBuilder {
  def mustache(template: String, model: Any, contentType: Option[String] = Some("text/html")) = {
    render.view(View("mustache", template, model, contentType))
  }
}
