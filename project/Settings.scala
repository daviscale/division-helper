import sbt._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

/**
 * Application settings. Configure the build for your application here.
 * You normally don't have to touch the actual build definition after this.
 */
object Settings {
  /** The name of your application */
  val name = "division-helper"

  /** The version of your application */
  val version = "0.0.1"

  /** Options for the scala compiler */
  val scalacOptions = Seq(
    "-Xlint",
    "-unchecked",
    "-deprecation",
    "-feature"
  )

  /** Declare global dependency versions here to avoid mismatches in multi part dependencies */
  object versions {
    val scala = "2.13.5"
    val scalaDom = "1.1.0"
    val scalajsReact = "1.7.7"
    val scalaCSS = "0.7.0"
    val log4js = "1.4.13"
    val autowire = "0.3.3"
    val booPickle = "1.3.2"
    val diode = "1.1.14"

    val react = "16.1.0"
    val jQuery = "3.6.0"
    val bootstrap = "4.6.0"
    val chartjs = "2.9.4"

    val scalajsScripts = "1.1.4"
  }


}
