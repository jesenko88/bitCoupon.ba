name := """bitCoupon"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "org.webjars" %% "webjars-play" % "2.3.0-2",
  "org.webjars" % "bootstrap" % "3.1.1-2",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.typesafe.play" %% "play-mailer" % "2.4.0",
  "log4j" % "log4j" % "1.2.17",
  "joda-time" % "joda-time" % "2.7",
  "edu.vt.middleware" % "vt-password" % "3.1.2",
  "org.imgscalr" % "imgscalr-lib" % "4.2",
  "com.paypal.sdk" % "rest-api-sdk" % "1.2.0",
  "org.jsoup" % "jsoup" % "1.8.1",
  "com.ckeditor" % "ckeditor-java-core" % "3.5.3",
  "org.apache.poi" % "poi" % "3.11",
  "nl.bitwalker" % "UserAgentUtils" % "1.2.4",
  "com.cloudinary" % "cloudinary" % "1.0.2"
)