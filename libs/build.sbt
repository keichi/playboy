name := "playboy-libs"

version := "1.0-SNAPSHOT"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

scalacOptions ++= Seq("-feature", "-deprecation")

libraryDependencies ++= Seq(
  cache,
  "org.scala-lang" % "scala-reflect" % "2.10.3",
  "com.typesafe.play" %% "play" % "2.2.3",
  "com.typesafe.slick" %% "slick" % "2.0.3",
  "com.typesafe.play" %% "play-slick" % "0.6.1",
  "com.github.tototoshi" %% "scala-csv" % "1.0.0",
  "com.github.tototoshi" %% "slick-joda-mapper" % "1.1.0"
)     
