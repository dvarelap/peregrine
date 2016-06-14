import Dependencies._

name := "peregrine"

organization := "prassee"

version := "0.0.5"

scalaVersion := "2.11.7"

publishMavenStyle := true

libraryDependencies ++= Seq(
  twitterServer,
  scalaTest,
  findbugs,
  jacksonScala,
  commonsIo,
  jacksonDatabind,
  mustache,
  scalaExtensions
)

resolvers += "Twitter" at "http://maven.twttr.com"

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")
