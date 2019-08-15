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
  .enablePlugins(JavaAppPackaging)
  .settings(
    dockerExposedPorts := Seq(8080),
    dockerBaseImage := "oracle/graalvm-ce:19.1.1",
    (unmanagedResourceDirectories in Compile) += (resourceDirectory in(billstrackerJS, Compile)).value,
    mappings.in(Universal) ++= webpack.in(Compile, fullOptJS).in(billstrackerJS, Compile).value.map { f =>
      f.data -> s"assets/${f.data.getName()}"
    },
    mappings.in(Universal) ++= Seq(
      (target in(billstrackerJS, Compile)).value / ("scala-" + scalaBinaryVersion.value) / "scalajs-bundler" / "main" / "node_modules" / "react-datepicker" / "dist" / "react-datepicker.min.css" -> "assets/react-datepicker.min.css",
      (target in(billstrackerJS, Compile)).value / ("scala-" + scalaBinaryVersion.value) / "scalajs-bundler" / "main" / "node_modules" / "bootstrap" / "dist" / "css" / "bootstrap.min.css" -> "assets/bootstrap.min.css"
    ),
    bashScriptExtraDefines += """addJava "-Dassets=${app_home}/../assets""""
  )

disablePlugins(RevolverPlugin)
