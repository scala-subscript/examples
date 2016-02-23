val subscriptVersion = "3.0.3"

val commonSettings = Seq(
  scalaVersion := "2.11.7"
, libraryDependencies += "org.subscript-lang" %% "subscript-swing" % subscriptVersion
)

val actorSettings = Seq(
  libraryDependencies += "org.subscript-lang" %% "subscript-akka" % subscriptVersion
)

lazy val ab            = (project in file("ab-example")).settings(commonSettings: _*)
lazy val helloworld    = (project in file("helloworld-example")).settings(commonSettings: _*)
lazy val lookupframe   = (project in file("lookupframe-example")).settings(commonSettings: _*)
lazy val life          = (project in file("life-example")).settings(commonSettings: _*)
lazy val pingpong      = (project in file("pingpong-example")).settings(actorSettings: _*)
lazy val twitter       = (project in file("subscript-twitter-search")).settings(commonSettings: _*)
lazy val taskprocessor = (project in file("taskprocessor"))
  .settings(actorSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalatest"          %% "scalatest"    % "2.2.4"  % Test
    , "com.typesafe.akka"      %% "akka-testkit" % "2.3.11" % Test
    , "org.scala-lang.modules" %% "scala-xml"    % "1.0.4"  % Test
    )
  )

lazy val filedownloader = (project in file("filedownloader")).settings(commonSettings: _*)

lazy val root = (project in file("."))
  .aggregate(ab, helloworld, lookupframe, life, pingpong, twitter, taskprocessor)