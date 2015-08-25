package com.github.dvarelap.stilt.config

import com.twitter.app.GlobalFlag

object port            extends GlobalFlag[String](":7070", "Http Port")
object adminPort       extends GlobalFlag[String](":9999", "Admin/Stats Port")
object sslPort         extends GlobalFlag[String](":7443", "Https Port")
object env             extends GlobalFlag[String]("development", "Environment {development |test | production }")
object pidEnabled      extends GlobalFlag[Boolean](false, "whether to write pid file")
object pidPath         extends GlobalFlag[String]("", "path to pid file")
object logPath         extends GlobalFlag[String]("logs/stilt.log", "path to log")
object logLevel        extends GlobalFlag[String]("INFO", "log level")
object logNode         extends GlobalFlag[String]("stilt", "Logging node")
object templatePath    extends GlobalFlag[String]("/", "path to templates")
object assetPath       extends GlobalFlag[String]("/public", "path to assets")
object docRoot         extends GlobalFlag[String]("src/main/resources", "path to docroot")
object maxRequestSize  extends GlobalFlag[Int](5, "maximum request size (in megabytes)")
object certificatePath extends GlobalFlag[String]("", "path to SSL certificate")
object keyPath         extends GlobalFlag[String]("", "path to SSL key")
object showDirectories extends GlobalFlag[Boolean](false, "allow directory view in asset path")
