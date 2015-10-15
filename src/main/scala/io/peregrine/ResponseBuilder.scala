package io.peregrine

import io.peregrine.jackson.DefaultJacksonJsonSerializer
import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import com.twitter.finagle.http.{Response => FinagleResponse, Cookie, Status}
import org.jboss.netty.util.CharsetUtil.UTF_8
import com.twitter.util.Future
import org.apache.commons.io.IOUtils
import java.io.File
import org.jboss.netty.handler.codec.http.DefaultCookie
import org.jboss.netty.handler.codec.http.{Cookie => NettyCookie, HttpResponseStatus}

object ResponseBuilder {
  def apply(status: Int) = new ResponseBuilder().status(status)
  def apply(body: Any)   = new ResponseBuilder().body(body)
}

class ResponseBuilder(serializer: JsonSerializer = DefaultJacksonJsonSerializer) extends CommonStatuses {
  private var status        : Option[Int]           = None
  private var headers       : Map[String, String]   = Map()
  private var strBody       : Option[String]        = None
  private var binBody       : Option[Array[Byte]]   = None
  private var json          : Option[Any]           = None
  private var view          : Option[View]          = None
  private var buffer        : Option[ChannelBuffer] = None
  private var cookies       : List[Cookie]          = List()
  private var csrfToken     : Option[String]        = None
  private var jsonSerializer: JsonSerializer        = serializer

  def contentType: Option[String] = this.headers.get("Content-Type")

  def withSerializer(serializer: JsonSerializer) = {
    jsonSerializer = serializer
    this
  }

  private def _setContentForJson(resp: HttpResponse, json: Any): Unit = {
    resp.headers.set("Content-Type", "application/json")
    val jsonBytes = jsonSerializer.serialize(json)
    _setContentForBinary(resp, jsonBytes)
  }

  private def _setContentView(resp: HttpResponse, view: View): Unit = {
    view._csrf = csrfToken
    val out = view.render
    val bytes = out.getBytes(UTF_8)
    resp.headers.set("Content-Length", bytes.length)
    if (view.contentType.isDefined && !resp.headers.contains("Content-Type")) {
      resp.headers.set("Content-Type", view.contentType.get)
    }
    resp.setContent(copiedBuffer(bytes))
  }

  private def _setContentForBinary(resp: HttpResponse, binary: Array[Byte]): Unit = {
    resp.headers.set("Content-Length", binary.length)
    resp.setContent(copiedBuffer(binary))
  }

  private def _setContentForString(resp: HttpResponse, string: String): Unit = {
    val bytes = string.getBytes(UTF_8)
    _setContentForBinary(resp, bytes)
  }

  private def _setContentForBuffer(resp: HttpResponse, buffer: ChannelBuffer): Unit = {
    resp.headers.set("Content-Length", buffer.capacity())
    resp.setContent(buffer)
  }

  private def setContent(resp: HttpResponse): HttpResponse = {
    json match {
      case Some(j) => _setContentForJson(resp, j)
      case None    => view match {
        case Some(v) => _setContentView(resp, v)
        case None    => strBody match {
          case Some(sb) => _setContentForString(resp, sb)
          case None     => binBody match {
            case Some(bb) => _setContentForBinary(resp, bb)
            case None     => buffer match {
              case Some(b) => _setContentForBuffer(resp, b)
              case None    => resp.headers.set("Content-Length", 0) //no content
            }
          }
        }
      }
    }
    resp
  }

  def cookie(k: String, v: String): ResponseBuilder = {
    this.cookies ::= new Cookie(new DefaultCookie(k, v))
    this
  }

  def cookie(c: Cookie): ResponseBuilder = {
    this.cookies ::= c
    this
  }

  def cookie(c: NettyCookie): ResponseBuilder = {
    this.cookies ::= new Cookie(c)
    this
  }



  def status(i: Int): ResponseBuilder = {
    this.status = Some(i)
    this
  }

  def nothing: ResponseBuilder = {
    this.header("Content-Type", "text/plain")
    this.body("")
    this
  }

  def plain(body:String): ResponseBuilder = {
    this.header("Content-Type", "text/plain")
    this.body(body)
    this
  }

  def html(body:String): ResponseBuilder = {
    this.header("Content-Type", "text/html")
    this.body(body)
    this
  }

  def body(s: String): ResponseBuilder = {
    this.strBody = Some(s)
    this
  }

  def body(b: Array[Byte]): ResponseBuilder = {
    this.binBody = Some(b)
    this
  }

  def body(body: Any): ResponseBuilder = {
    body match {
      case null => nothing
      // case buf: Buf => body(buf)
      case bytes: Array[Byte] => this.body(bytes)
      // case cbos: ChannelBuffer => body(cbos)
      case "" => nothing
      case Unit => nothing
      case None => nothing
      case str: String => plain(str)
      case _file: File => static(_file.toString)

      case x: Int     => plain(x.toString)
      case x: Long    => plain(x.toString)
      case x: Short   => plain(x.toString)
      case x: Byte    => plain(x.toString)
      case x: Double  => plain(x.toString)
      case x: Float   => plain(x.toString)
      case x: Char    => plain(x.toString)
      case x: Boolean => plain(x.toString)
      case other => this.body(other.toString)

    }
    this
  }


  def header(k: String, v: String): ResponseBuilder = {
    this.headers += (k -> v)
    this
  }

  def headers(m: Map[String, String]): ResponseBuilder = {
    this.headers = this.headers ++ m
    this
  }

  def json(o: Any): ResponseBuilder = {
    this.header("Content-Type", "application/json")
    this.json = Some(o)
    this
  }

  def view(v: View): ResponseBuilder = {
    this.view = Some(v)
    this
  }

  def buffer(b: ChannelBuffer): ResponseBuilder = {
    this.buffer = Some(b)
    this
  }

  def contentType(ct: String): ResponseBuilder = {
    this.header("Content-Type", ct)
    this
  }

  def static(path: String): ResponseBuilder = {
    val fullAssetPath = new File(config.assetPath(), path).toString
    if (FileResolver.hasFile(fullAssetPath) && path != "/") {
      val stream  = FileResolver.getInputStream(fullAssetPath)
      val bytes   = IOUtils.toByteArray(stream)

      stream.read(bytes)

      val mtype = FileService.extMap.getContentType('.' + fullAssetPath.split('.').last)

      this.status(200)
      this.header("Content-Type", mtype)
      this.body(bytes)
    } else {
      throw new IllegalArgumentException(s"File does not exist ${fullAssetPath}")
    }

    this
  }

  private[peregrine] def internal(name: String, status: Int): ResponseBuilder = {

    val fullPath = getClass.getResource(s"/$name").toString
    val stream   = getClass.getResourceAsStream(s"/$name")

    val bytes   = IOUtils.toByteArray(stream)

    stream.read(bytes)

    val mtype = FileService.extMap.getContentType('.' + fullPath.split('.').last)

    this.status(status)
    this.header("Content-Type", mtype)
    this.body(bytes)
    this
  }

  def build: FinagleResponse  = {
    build(Request())
  }

  def build(request: Request): FinagleResponse = {
    val response = request.response

    // Only set the status code if set explicitly in the builder
    this.status map response.setStatusCode

    headers.foreach { xs =>
      response.headers.add(xs._1, xs._2)
    }

    cookies foreach response.cookies.add

    csrfToken = response.cookies.get("_authenticity_token").map(_.value)

    setContent(response)

    response
  }

  def toFuture:Future[ResponseBuilder] = Future.value(this)

  override def toString: String = {
    val buf = new StringBuilder

    buf.append(getClass.getSimpleName)
    buf.append('\n')
    buf.append(HTTP_1_1.toString)
    buf.append(' ')
    buf.append(this.status)
    buf.append('\n')
    buf.append(this.headers)

    buf.toString()
  }

}

trait CommonStatuses { self: ResponseBuilder =>
  def ok:                  ResponseBuilder = buildFromStatus(Status.Ok)
  def created:             ResponseBuilder = buildFromStatus(Status.Created)
  def accepted:            ResponseBuilder = buildFromStatus(Status.Accepted)
  def movedPermanently:    ResponseBuilder = buildFromStatus(Status.MovedPermanently)
  def found:               ResponseBuilder = buildFromStatus(Status.Found)
  def notModified:         ResponseBuilder = buildFromStatus(Status.NotModified)
  def temporaryRedirect:   ResponseBuilder = buildFromStatus(Status.TemporaryRedirect)
  def badRequest:          ResponseBuilder = buildFromStatus(Status.BadRequest)
  def unauthorized:        ResponseBuilder = buildFromStatus(Status.Unauthorized)
  def forbidden:           ResponseBuilder = buildFromStatus(Status.Forbidden)
  def notFound:            ResponseBuilder = buildFromStatus(Status.NotFound)
  def gone:                ResponseBuilder = buildFromStatus(Status.Gone)
  def internalServerError: ResponseBuilder = buildFromStatus(Status.InternalServerError)
  def notImplemented:      ResponseBuilder = buildFromStatus(Status.NotImplemented)
  def serviceUnavailable:  ResponseBuilder = buildFromStatus(Status.ServiceUnavailable)

  private def buildFromStatus(st: HttpResponseStatus): ResponseBuilder  = {
    status(st.getCode)
    this
  }

}
