scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.subscript-lang" %% "subscript-swing" % "3.0.0"

, "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2"
, "commons-codec" % "commons-codec" % "1.10"

, "junit" % "junit" % "4.11" % Test
, "org.scalatest" %% "scalatest" % "2.2.2" % Test
  // "org.scala-lang.modules" % "scala-xml_2.11"%"1.0.2"
)

SubscriptSbt.projectSettings