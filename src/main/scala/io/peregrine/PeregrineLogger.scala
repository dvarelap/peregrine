package io.peregrine

import com.twitter.logging._
import com.twitter.logging.Logging._
import java.util.{logging => javalog}

trait LoggerColors {
  val ANSI_RESET  = "\u001B[0m"
  val ANSI_BLACK  = "\u001B[30m"
  val ANSI_RED    = "\u001B[31m"
  val ANSI_GREEN  = "\u001B[32m"
  val ANSI_YELLOW = "\u001B[33m"
  val ANSI_BLUE   = "\u001B[34m"
  val ANSI_PURPLE = "\u001B[35m"
  val ANSI_CYAN   = "\u001B[36m"
  val ANSI_WHITE  = "\u001B[37m"
}

trait PeregrineLogger extends LoggerColors {

  val level       = LevelFlaggable.parse(config.logLevel())
  val environment = config.env()

  protected val policy = if (environment != "production") Policy.SigHup else Policy.Weekly(1)
  protected val consoleFormatter = new Formatter(
      prefix = s"$ANSI_CYAN%.3s$ANSI_RESET <yyyy/MM/dd HH:mm:ss.SSS> ")
  protected val fileFormatter = new Formatter(prefix = s"%.3s <yyyy/MM/dd HH:mm:ss.SSS> ") {
    override def formatText(record: javalog.LogRecord): String = {
      super.formatText(record).replaceAll("\u001B\\[[;\\d]*m", "")
    }
  }

  def loggingFactories: List[LoggerFactory] = logger :: Nil

  val logger = LoggerFactory(
      node = config.logNode(),
      level = Option(level),
      handlers = List(
          FileHandler(filename = s"log/$environment.log", rollPolicy = policy, formatter = fileFormatter),
          ConsoleHandler(consoleFormatter)
      )
  )
}

object PeregrineLogger extends PeregrineLogger
