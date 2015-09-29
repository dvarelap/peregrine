package io.peregrine.config

import com.twitter.app.GlobalFlag

object env {
  private[this] val environment = System.getenv("PEREGRINE_ENV") match {
    case e: String => e
    case _         => "development"
  }
  def apply(): String = environment
}

object port               extends GlobalFlag[String](":5000", "Http Port")
object sslPort            extends GlobalFlag[String](":5043", "Https Port")
object pidEnabled         extends GlobalFlag[Boolean](false, "whether to write pid file")
object pidPath            extends GlobalFlag[String]("", "path to pid file")
object logPath            extends GlobalFlag[String]("logs/peregrine.log", "path to log")
object logLevel           extends GlobalFlag[String]("INFO", "log level")
object logNode            extends GlobalFlag[String]("peregrine", "Logging node")
object templatePath       extends GlobalFlag[String]("/", "path to templates")
object assetPath          extends GlobalFlag[String]("/public", "path to assets")
object assetsPathPrefix   extends GlobalFlag[String]("/assets/", "the prefix used to prefix assets url")
object docRoot            extends GlobalFlag[String]("src/main/resources", "path to docroot")
object maxRequestSize     extends GlobalFlag[Int](5, "maximum request size (in megabytes)")
object certificatePath    extends GlobalFlag[String]("", "path to SSL certificate")
object keyPath            extends GlobalFlag[String]("", "path to SSL key")
object showDirectories    extends GlobalFlag[Boolean](false, "allow directory view in asset path")
object debugAssets        extends GlobalFlag[Boolean](false, "enable to show assets requests in logs")
