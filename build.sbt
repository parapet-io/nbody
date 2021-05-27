
name := "nbody"

version := "0.1"

ThisBuild / scalaVersion := "2.13.6"

val dependencies = Seq(
  "io.parapet" %% "cluster-node" % "0.0.1-RC5",
  "io.parapet" %% "interop-cats" % "0.0.1-RC5",
  "com.github.scopt" %% "scopt" % "4.0.1",
  "org.slf4j" % "slf4j-log4j12" % "1.7.25",
  "org.slf4j" % "slf4j-log4j12" % "1.7.25"
)

lazy val core = project
  .in(file("core"))
  .settings(
    name := "core"
  )

lazy val api = project
  .in(file("api"))
  .enablePlugins(ProtobufPlugin)
  .settings(
    name := "api"
  )

lazy val node = project
  .in(file("node"))
  .enablePlugins(JavaAppPackaging, UniversalDeployPlugin)
  .settings(
    name := "node",
    libraryDependencies ++= dependencies)
  .dependsOn(api, core)

lazy val coordinator = project
  .enablePlugins(JavaAppPackaging, UniversalDeployPlugin)
  .in(file("coordinator"))
  .settings(
    name := "coordinator",
    libraryDependencies ++= dependencies)
  .dependsOn(api, core)

lazy val global = project
  .in(file("."))
  .aggregate(
    core,
    api,
    node,
    coordinator)
