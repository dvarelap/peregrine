package io.peregrine
import io.peregrine.jackson._

trait ResponseHandler {
  val jsonSerializer: JsonSerializer          = DefaultJacksonJsonSerializer
  def render: ResponseBuilder                 = new ResponseBuilder(jsonSerializer)
  def json[T](jsonObj: T): ResponseBuilder    = render.json(jsonObj)
  def html(htmlCode: String): ResponseBuilder = render.html(htmlCode)
  def static(file: String): ResponseBuilder   = render.static(file)
  def plain(s: String): ResponseBuilder       = render.plain(s)
  def status(s: Int): ResponseBuilder         = render.status(s)
}
