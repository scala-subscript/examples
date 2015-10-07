scalaVersion := "2.11.7"
SubscriptSbt.projectSettings

libraryDependencies ++= Seq(
  "org.subscript-lang" %% "subscript-akka" % "2.0.0"

, "org.scalatest"          %% "scalatest"    % "2.2.4"  % Test
, "com.typesafe.akka"      %% "akka-testkit" % "2.3.11" % Test
, "org.scala-lang.modules" %% "scala-xml"    % "1.0.4"  % Test
)
