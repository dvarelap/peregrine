val twitterServer 	= "com.twitter" %% "twitter-server" % "1.10.0"
val scalaTest				= "org.scalatest" %% "scalatest" % "2.2.4"
val findbugs				= "com.google.code.findbugs" % "jsr305" % "2.0.1"
val jacksonScala		= "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.2.2"
val commonsIo				= "commons-io" % "commons-io" % "1.3.2"
val jacksonDatabind = "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.2"

resolvers += "Twitter" at "http://maven.twttr.com"

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")

// still in beta so it's only being published to local repo
publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

lazy val stilt = (project in file(".")).
  settings(
    organization := "com.github.dvarelap",
    name := "stilt",
    version := "0.1.0",
    scalaVersion := "2.11.6",
    libraryDependencies ++= Seq(
			twitterServer,
    	scalaTest,
    	findbugs,
    	jacksonScala,
    	commonsIo,
      jacksonDatabind
    )
  )










