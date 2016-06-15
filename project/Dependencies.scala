import sbt.Keys._
import sbt._
object Dependencies {
  val twitterServer         = "com.twitter"                       %% "twitter-server"          % "1.13.0"
  val scalaTest             = "org.scalatest"                     %% "scalatest"               % "2.2.4"
  val findbugs              = "com.google.code.findbugs"          % "jsr305"                   % "2.0.1"
  val commonsIo             = "commons-io"                        % "commons-io"               % "1.3.2"
  val jacksonScala          = "com.fasterxml.jackson.module"      %% "jackson-module-scala"    % "2.4.4"
  val jacksonDatabind       = "com.fasterxml.jackson.core"        % "jackson-databind"         % "2.4.4"
  val mustache              = "com.github.spullara.mustache.java" % "compiler"                 % "0.9.1"
  val scalaExtensions       = "com.github.spullara.mustache.java" % "scala-extensions-2.11"    % "0.9.1"
  val peregrineServerPlugin = "prassee"                           %% "peregrine-server-plugin" % "0.0.1"
}
