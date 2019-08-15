val Http4sVersion = "0.20.0"
val LogbackVersion = "1.2.3"

import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

lazy val sharedSettings = Seq(
  organization := "oen",
  scalaVersion := "2.12.8",
  version := "0.1",
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "scalatags" % "0.6.8",
    "org.typelevel" %%% "cats-core" % "1.6.1",
    "io.circe" %%% "circe-generic" % "0.11.1",
    "io.circe" %%% "circe-literal" % "0.11.1",
    "io.circe" %%% "circe-generic-extras" % "0.11.1",
    "io.circe" %%% "circe-parser" % "0.11.1",
    "io.scalaland" %%% "chimney" % "0.3.2",
    "com.softwaremill.quicklens" %%% "quicklens" % "1.4.12"
  ),
  scalacOptions ++= Seq(
    "-Xlint",
    "-unchecked",
    "-deprecation",
    "-feature",
    "-Ypartial-unification",
    "-language:higherKinds"
  )
)

lazy val fastOptJSDev = TaskKey[Unit]("fastOptJSDev")

lazy val jsSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.7",
    "com.github.japgolly.scalajs-react" %%% "core" % "1.4.2",
    "com.github.japgolly.scalajs-react" %%% "extra" % "1.4.2",
    "com.payalabs" %%% "scalajs-react-bridge" % "0.8.1",
    "io.suzaku" %%% "diode" % "1.1.5",
    "io.suzaku" %%% "diode-react" % "1.1.5.142"
  ),
  npmDependencies in Compile ++= Seq(
    "react" -> "16.7.0",
    "react-dom" -> "16.7.0",
    "react-datepicker" -> "2.8.0",
    "bootstrap" -> "4.3.1",
    "jquery" -> "3.4.1"
  ),
  webpackBundlingMode := BundlingMode.LibraryAndApplication(), // LibraryOnly() for faster dev builds
  scalaJSUseMainModuleInitializer := true,
  fastOptJSDev := {
    // resources
    val targetRes = "../target/scala-2.12/classes/"
    IO.copyDirectory((resourceDirectory in Compile).value, new File(baseDirectory.value, targetRes))

    val targetBundler = targetRes + "scalajs-bundler/main/"

    // fastopt.js.map
    val fastOptData = (fastOptJS in Compile).value.data
    val mapFileName = fastOptData.name + ".map"
    val fastOptMapFrom = fastOptData.getParentFile / mapFileName
    val fastOptMapTo = new File(baseDirectory.value, targetBundler + mapFileName)
    IO.copyFile(fastOptMapFrom, fastOptMapTo)

    // webpack
    val webpackFiles = webpack.in(Compile, fastOptJS).value.map(_.data)
    webpackFiles.foreach { f =>
      val targetFile = new File(baseDirectory.value, targetBundler + f.name)
      IO.copyFile(f, targetFile)
    }
  }
)

lazy val jvmSettings = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "1.3.0",
    "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
    "org.http4s" %% "http4s-circe" % Http4sVersion,
    "org.http4s" %% "http4s-dsl" % Http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
    "ch.qos.logback" % "logback-classic" % LogbackVersion,
    "com.github.pureconfig" %% "pureconfig" % "0.11.0",
    "org.reactormonk" %% "cryptobits" % "1.3",
    "org.mindrot" % "jbcrypt" % "0.4",
    "org.reactivemongo" %% "reactivemongo" % "0.18.1"
  ),
  target := baseDirectory.value / ".." / "target"
)

lazy val billstracker =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Full).in(file("."))
    .settings(sharedSettings)
    .jsSettings(jsSettings)
    .jvmSettings(jvmSettings)

lazy val billstrackerJS = billstracker.js
  .enablePlugins(WorkbenchPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
  .disablePlugins(RevolverPlugin)

lazy val billstrackerJVM = billstracker.jvm
  .enablePlugins(DockerPlugin)
  .enablePlugins(JavaAppPackaging).settings(
  dockerExposedPorts := Seq(8080),
  dockerBaseImage := "oracle/graalvm-ce:19.1.1",
  (resources in Compile) ++= webpack.in(Compile, fullOptJS).in(billstrackerJS, Compile).value.map(_.data),
  (unmanagedResourceDirectories in Compile) += (resourceDirectory in(billstrackerJS, Compile)).value,
  resourceGenerators in Compile += Def.task {
    webpack.in(Compile, fullOptJS).in(billstrackerJS, Compile).value
    val fixedPath = "scalajs-bundler/main/node_modules"
    val jsPrefix = (target in(billstrackerJS, Compile)).value / ("scala-" + scalaBinaryVersion.value) / fixedPath
    val dstPrefix = (resourceManaged in Compile).value / fixedPath
    Seq(
      "react-datepicker/dist/react-datepicker.min.css"
    ).map { f =>
      val src = jsPrefix / f
      val dst = dstPrefix / f
      IO.copyFile(src, dst)
      dst
    }
  }.taskValue
)

disablePlugins(RevolverPlugin)
