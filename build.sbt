name := """MachineLearning_UP"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.10.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.apache.hadoop"%"hadoop-client"%"2.7.1",
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
