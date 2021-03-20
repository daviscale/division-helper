import sbt.Keys._
import sbt.Project.projectToRef
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

// a special crossProject for configuring a JS/JVM/shared structure
lazy val shared = (crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure) in file("shared"))
  .settings(
    scalaVersion := Settings.versions.scala,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "autowire" % Settings.versions.autowire,
      "io.suzaku" %%% "boopickle" % Settings.versions.booPickle
    )
  )
  // set up settings specific to the JS project
  .jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJVM = shared.jvm.settings(name := "sharedJVM")

lazy val sharedJS = shared.js.settings(name := "sharedJS")

// use eliding to drop some debug code in the production build
lazy val elideOptions = settingKey[Seq[String]]("Set limit for elidable functions")

// instantiate the JS project for SBT with some additional settings
lazy val client: Project = (project in file("client"))
  .settings(
    name := "client",
    version := Settings.version,
    scalaVersion := Settings.versions.scala,
    scalacOptions ++= Settings.scalacOptions,
    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core" % Settings.versions.scalajsReact,
      "com.github.japgolly.scalajs-react" %%% "extra" % Settings.versions.scalajsReact,
      "com.github.japgolly.scalacss" %%% "ext-react" % Settings.versions.scalaCSS,
      "io.suzaku" %%% "diode" % Settings.versions.diode,
      "io.suzaku" %%% "diode-react" % Settings.versions.diode,
      "org.scala-js" %%% "scalajs-dom" % Settings.versions.scalaDom
    ),
    // by default we do development build, no eliding
    elideOptions := Seq(),
    scalacOptions ++= elideOptions.value,
    jsDependencies ++= Seq(
      "org.webjars.bower" % "react" % Settings.versions.react / "react-with-addons.js" minified "react-with-addons.min.js" commonJSName "React",
      "org.webjars.bower" % "react" % Settings.versions.react / "react-dom.js" minified "react-dom.min.js" dependsOn "react-with-addons.js" commonJSName "ReactDOM",
      "org.webjars" % "jquery" % Settings.versions.jQuery / "jquery.js" minified "jquery.min.js",
      "org.webjars" % "bootstrap" % Settings.versions.bootstrap / "bootstrap.js" minified "bootstrap.min.js" dependsOn "jquery.js",
      "org.webjars" % "chartjs" % Settings.versions.chartjs / "Chart.js" minified "Chart.min.js",
      "org.webjars" % "log4javascript" % Settings.versions.log4js / "js/log4javascript_uncompressed.js" minified "js/log4javascript.js"
    ),
    // yes, we want to package JS dependencies
    skip in packageJSDependencies := false,
    // use Scala.js provided launcher code to start the client app
    scalaJSUseMainModuleInitializer := true,
    scalaJSUseMainModuleInitializer in Test := false,
    // use uTest framework for tests
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .dependsOn(sharedJS)

// Client projects (just one in this case)
lazy val clients = Seq(client)

// instantiate the JVM project for SBT with some additional settings
lazy val server = (project in file("server"))
  .settings(
    name := "server",
    version := Settings.version,
    scalaVersion := Settings.versions.scala,
    scalacOptions ++= Settings.scalacOptions,
    libraryDependencies ++= Seq(
      "com.vmunier" %% "scalajs-scripts" % Settings.versions.scalajsScripts,
      "org.webjars" % "font-awesome" % "4.3.0-1" % Provided,
      "org.webjars" % "bootstrap" % Settings.versions.bootstrap % Provided
    ),
    commands += ReleaseCmd,
    // triggers scalaJSPipeline when using compile or continuous compilation
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    // connect to the client project
    scalaJSProjects := clients,
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    // compress CSS
    LessKeys.compress in Assets := true
  )
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin) // use the standard directory layout instead of Play's custom
  .aggregate(clients.map(projectToRef): _*)
  .dependsOn(sharedJVM)

// Command for building a release
lazy val ReleaseCmd = Command.command("release") {
  state => "set elideOptions in client := Seq(\"-Xelide-below\", \"WARNING\")" ::
    "client/clean" ::
    "client/test" ::
    "server/clean" ::
    "server/test" ::
    "server/dist" ::
    "set elideOptions in client := Seq()" ::
    state
}

lazy val root = (project in file(".")).aggregate(client, server).enablePlugins(ScalaJSPlugin)

// loads the Play server project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value
