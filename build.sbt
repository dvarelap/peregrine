import Dependencies._

name := "peregrine"

organization := "prassee"

version := "0.0.6.1"

scalaVersion := "2.11.8"

publishMavenStyle := true

libraryDependencies ++= Seq(
  twitterServer,
  scalaTest,
  findbugs,
  jacksonScala,
  commonsIo,
  jacksonDatabind,
  peregrineServerPlugin
)

resolvers ++= Seq("moma" at "https://github.com/prassee/moma/raw/master/snapshots",
  "Twitter" at "http://maven.twttr.com")

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")
