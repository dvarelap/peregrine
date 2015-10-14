package io.peregrine

import io.peregrine.jackson.DefaultJacksonJsonSerializer
import com.twitter.app.App
import com.twitter.server.Stats
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http._
import com.twitter.finagle.stats._

trait Controller extends App with Stats {

  val routeList                  = new RouteVector
  val stats                      = statsReceiver.scope("Controller")
  var serializer: JsonSerializer = DefaultJacksonJsonSerializer

  var notFoundHandler: Option[(Request) => Future[ResponseBuilder]] = handleNotFound
  var errorHandler   : Option[(Request) => Future[ResponseBuilder]] = None

  def get[R](paths: String*)    (callback: Request => R) { paths.foreach(path => addRoute(HttpMethod.GET,      path)(callback)) }
  def delete[R](paths: String*) (callback: Request => R) { paths.foreach(path => addRoute(HttpMethod.DELETE,   path)(callback)) }
  def post[R](paths: String*)   (callback: Request => R) { paths.foreach(path => addRoute(HttpMethod.POST,     path)(callback)) }
  def put[R](paths: String*)    (callback: Request => R) { paths.foreach(path => addRoute(HttpMethod.PUT,      path)(callback)) }
  def head[R](paths: String*)   (callback: Request => R) { paths.foreach(path => addRoute(HttpMethod.HEAD,     path)(callback)) }
  def patch[R](paths: String*)  (callback: Request => R) { paths.foreach(path => addRoute(HttpMethod.PATCH,    path)(callback)) }
  def options[R](paths: String*)(callback: Request => R) { paths.foreach(path => addRoute(HttpMethod.OPTIONS,  path)(callback)) }

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

  def addRoute[R](method: HttpMethod, path: String)(callback: Request => R) {
    val regex = SinatraPathPatternParser(path)
    routeList.add(Route(method, path, regex, (r) => {
      val reqStat = stats.stat("%s/Root/%s".format(method.toString, path.stripPrefix("/")))
      Stat.timeFuture(reqStat)(coerceFuture(callback(r)))
    }))
  }

  private[this] def coerceFuture[R](response: R): Future[ResponseBuilder] = response match {
    case f: Future[_]               => f.map(coerceResponseBuilder)
    case r: ResponseBuilder         => r.toFuture
    case other: Any                 => Future(coerceResponseBuilder(other))
  }

  private[this] def coerceResponseBuilder[R](response: R): ResponseBuilder = response match {
    case r: ResponseBuilder  => r
    case _                   => throw new RuntimeException("Result type not recognized")
  }

  private[peregrine] def withPrefix(prefix: String) = routeList.withPrefix(prefix)

  private[this] def handleNotFound: Option[(Request) => Future[ResponseBuilder]] = {
    if (config.env() != "development") None
    else Some(req => render.internal("__peregrine__/404.html", 404).toFuture)
  }

}
