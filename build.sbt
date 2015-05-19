val twitterServer 	= "com.twitter" %% "twitter-server" % "1.10.0"
val scalaTest				= "org.scalatest" %% "scalatest" % "2.2.4"
val findbugs				= "com.google.code.findbugs" % "jsr305" % "2.0.1"
val jacksonScala		= "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.2.2"
val commonsIo				= "commons-io" % "commons-io" % "1.3.2"
val jacksonDatabind = "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.2"

resolvers += "Twitter" at "http://maven.twttr.com"

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")

lazy val stilt = (project in file(".")).
  settings(
    name := "stilt",
    version := "0.0.1",
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










