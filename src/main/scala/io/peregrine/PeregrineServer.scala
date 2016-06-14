package io.peregrine

import java.io.{File, FileNotFoundException, FileOutputStream}
import java.lang.management.ManagementFactory
import java.net.SocketAddress

import com.twitter.finagle._
import com.twitter.finagle.http.codec.HttpServerDispatcher
import com.twitter.finagle.http.{HttpMuxer, Request => FinagleRequest, Response => FinagleResponse}
import com.twitter.finagle.netty3.{Netty3Listener, Netty3ListenerTLSConfig}
import com.twitter.finagle.server.DefaultServer
import com.twitter.finagle.ssl.Ssl
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import com.twitter.conversions.storage._

class PeregrineServer extends TwitterServer with PeregrineLogger {

  override lazy val log = logger()

  val controllers: ControllerCollection = new ControllerCollection
  var filters: Seq[Filter[FinagleRequest, FinagleResponse, FinagleRequest, FinagleResponse]] = Seq.empty
  val pid: String = ManagementFactory.getRuntimeMXBean.getName.split('@').head
  var secureServer: Option[ListeningServer] = None
  var server: Option[ListeningServer]       = None

  def allFilters(
      baseService: Service[FinagleRequest, FinagleResponse]): Service[FinagleRequest, FinagleResponse] = {
    filters.foldRight(baseService) { (b, a) =>
      b andThen a
    }
  }

  override def loggerFactories = loggingFactories

  import PeregrineServer._
  def register(controllers: ControllerEntry*) {
    controllers.foreach(ce => register(ce.controller, ce.prefix))
  }

  def register(controller: Controller, pathPrefix: String = "") {
    controller.withPrefix(pathPrefix)
    controllers.add(controller)
  }

  def addFilter(filter: Filter[FinagleRequest, FinagleResponse, FinagleRequest, FinagleResponse]) {
    filters = filters ++ Seq(filter)
  }

  def main() {
    start()
  }

  private[this] lazy val service = {
    val appService = new AppService(controllers)
    // val fileService   = new FileService
    val loggingFilter = new LoggingFilter
    val assetsFilter  = new AssetsFilter

    addFilter(assetsFilter)
    addFilter(loggingFilter)
    // addFilter(fileService)
    allFilters(appService)
  }

  private[this] lazy val codec = {
    http
      .Http()
      .maxRequestSize(config.maxRequestSize().megabyte)
      .enableTracing(enable = true)
      .server(ServerCodecConfig("httpserver", new SocketAddress {}))
      .pipelineFactory
  }

  def writePidFile() {
    val pidFile       = new File(config.pidPath())
    val pidFileStream = new FileOutputStream(pidFile)
    pidFileStream.write(pid.getBytes)
    pidFileStream.close()
  }

  def removePidFile() {
    val pidFile = new File(config.pidPath())
    pidFile.delete()
  }

  /**
    * Allow custom TLS configuration
    */
  def tlsConfig: Option[Netty3ListenerTLSConfig] = {
    if (!config.certificatePath().isEmpty && !config.keyPath().isEmpty) {
      if (!new File(config.certificatePath()).canRead) {
        val e = new FileNotFoundException("SSL Certificate not found: " + config.certificatePath())
        log.fatal(e, "SSL Certificate could not be read: " + config.certificatePath())
        throw e
      }
      if (!new File(config.keyPath()).canRead) {
        val e = new FileNotFoundException("SSL Key not found: " + config.keyPath())
        log.fatal(e, "SSL Key could not be read: " + config.keyPath())
        throw e
      }

      Some(Netty3ListenerTLSConfig(() =>
                Ssl.server(config.certificatePath(), config.keyPath(), null, null, null)))
    } else {
      None
    }
  }

  def startSecureServer() {
    tlsConfig.foreach { conf =>
      object HttpsListener extends Netty3Listener[Any, Any]("https", codec, tlsConfig = Some(conf))
      object HttpsServer
          extends DefaultServer[FinagleRequest, FinagleResponse, Any, Any](
              "https",
              HttpsListener,
              new HttpServerDispatcher(_, _)
          )
      printf(">> Listening [HTTPS] on 0.0.0.0%s%s%s%n", ANSI_YELLOW, config.sslPort(), ANSI_RESET)
      secureServer = Some(HttpsServer.serve(config.sslPort(), service))
    }
  }

  def startHttpServer() {
    object HttpListener extends Netty3Listener[Any, Any]("http", codec)
    object HttpServer
        extends DefaultServer[FinagleRequest, FinagleResponse, Any, Any](
            "http",
            HttpListener,
            new HttpServerDispatcher(_, _)
        )
    printf(">> Listening on 0.0.0.0%s%s%s%n", ANSI_YELLOW, config.port(), ANSI_RESET)
    server = Some(HttpServer.serve(config.port(), service))
  }

  def stop() {
    server map { _.close() }
    secureServer map { _.close() }
  }

  onExit {
    stop()
    removePidFile()
  }

  def start() {
    printf("%n== Peregringe has taken off for %s%s%s with pid %s%s%n",
           ANSI_BLUE,
           config.env(),
           ANSI_RESET,
           pid,
           ANSI_RESET)

    if (!config.pidPath().isEmpty) {
      writePidFile()
    }

    if (!config.port().isEmpty) {
      startHttpServer()
    }

    if (!config.sslPort().isEmpty) {
      startSecureServer()
    }

    server map { Await.ready(_) }
    secureServer map { Await.ready(_) }
  }
}

class DefaultPeregrineServer extends PeregrineServer with PeregrineServerPlugin {
  override def onServerInit(): Unit = {}

  override def main = {
    onServerInit()
    super.main()
  }
}

object PeregrineServer {
  case class ControllerEntry(controller: Controller, prefix: String = "")
  implicit def toControllerEntry(controller: (Controller, String)): ControllerEntry =
    ControllerEntry(controller._1, controller._2)
}
