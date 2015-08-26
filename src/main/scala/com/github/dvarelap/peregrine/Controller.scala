package com.github.dvarelap.peregrine

import com.github.dvarelap.peregrine.jackson.DefaultJacksonJsonSerializer
import com.twitter.app.App
import com.twitter.server.Stats
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http._

class Controller extends App with Stats {

  val routes                     = new RouteVector
  val stats                      = statsReceiver.scope("Controller")
  var serializer: JsonSerializer = DefaultJacksonJsonSerializer

  var notFoundHandler: Option[(Request) => Future[ResponseBuilder]] = None
  var errorHandler   : Option[(Request) => Future[ResponseBuilder]] = None

  def get(paths: String*)   (callback: Request => Future[ResponseBuilder]) { paths.foreach(path => addRoute(HttpMethod.GET,    path)(callback)) }
  def delete(paths: String*)(callback: Request => Future[ResponseBuilder]) { paths.foreach(path => addRoute(HttpMethod.DELETE, path)(callback)) }
  def post(paths: String*)  (callback: Request => Future[ResponseBuilder]) { paths.foreach(path => addRoute(HttpMethod.POST,   path)(callback)) }
  def put(paths: String*)   (callback: Request => Future[ResponseBuilder]) { paths.foreach(path => addRoute(HttpMethod.PUT,    path)(callback)) }
  def head(paths: String*)  (callback: Request => Future[ResponseBuilder]) { paths.foreach(path => addRoute(HttpMethod.HEAD,   path)(callback)) }
  def patch(paths: String*) (callback: Request => Future[ResponseBuilder]) { paths.foreach(path => addRoute(HttpMethod.PATCH,  path)(callback)) }
  def options(paths: String*)(callback: Request => Future[ResponseBuilder]){ paths.foreach(path => addRoute(HttpMethod.OPTIONS, path)(callback)) }

  def notFound(callback: Request => Future[ResponseBuilder]) { notFoundHandler = Option(callback) }
  def error(callback: Request => Future[ResponseBuilder]) { errorHandler = Option(callback) }

  def render: ResponseBuilder = new ResponseBuilder(serializer)
  def route: Router = new Router(this)

  def redirect(location: String, message: String = "", permanent: Boolean = false): ResponseBuilder = {
    val msg = if (message == "") "Redirecting to <a href=\"%s\">%s</a>.".format(location, location)
    else message

    val code = if (permanent) 301 else 302

    render.plain(msg).status(code).header("Location", location)
  }

  def respondTo(r: Request)(callback: PartialFunction[ContentType, Future[ResponseBuilder]]): Future[ResponseBuilder] = {
    if (r.routeParams.get("format").isDefined) {
      val format      = r.routeParams("format")
      val mime        = FileService.getContentType("." + format)
      val contentType = ContentType(mime).getOrElse(new ContentType.All)

      if (callback.isDefinedAt(contentType)) callback(contentType)
      else throw new UnsupportedMediaType

    } else {
      val contentTypeMaybe = r.accepts.find(mimeType => callback.isDefinedAt(mimeType))
      contentTypeMaybe match {
        case Some(contentType) => callback(contentType)
        case None              => throw new UnsupportedMediaType
      }
    }
  }

  def addRoute(method: HttpMethod, path: String)(callback: Request => Future[ResponseBuilder]) {
    val regex = SinatraPathPatternParser(path)
    routes.add(Route(method, path, regex, (r) => {
      stats.timeFuture("%s/Root/%s".format(method.toString, path.stripPrefix("/"))) {
        callback(r)
      }
    }))
  }

  private[peregrine] def withPrefix(prefix: String) = routes.withPrefix(prefix)
}
