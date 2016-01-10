import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._

object GithubgraphBuild extends Build {
  val Organization = "com.trizzle"
  val Name = "GithubGraph"
  val Version = "0.1.0"
  val ScalaVersion = "2.11.7"
  val ScalatraVersion = "2.4.0"

  lazy val project = Project (
    "githubgraph",
    file("."),
    settings = Seq(com.typesafe.sbt.SbtStartScript.startScriptForClassesSettings: _*) ++
      ScalatraPlugin.scalatraSettings ++ scalateSettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
        "ch.qos.logback" % "logback-classic" % "1.1.2" % "runtime",
        "org.eclipse.jetty" % "jetty-webapp" % "9.2.10.v20150310" % "compile;container",
        "javax.servlet" % "javax.servlet-api" % "3.1.0" % "compile;provided",
        "org.scalatra" %% "scalatra-json" % ScalatraVersion,
        "org.json4s"   %% "json4s-jackson" % "3.3.0",
        "com.typesafe.akka" %% "akka-actor" % "2.3.4",
        "net.databinder.dispatch" %% "dispatch-core" % "0.11.3",
        "net.databinder.dispatch" %% "dispatch-json4s-jackson" % "0.11.3",
        "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.6.3",
        "org.scala-lang.modules" % "scala-async_2.11" % "0.9.5",
        "org.reactivemongo" %% "reactivemongo" % "0.11.9",
        "com.typesafe.play" %% "play-iteratees" % "2.5.0-M1"
      ),

      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty,  /* default imports should be added here */
            Seq(
              Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
            ),  /* add extra bindings here */
            Some("templates")
          )
        )
      }
    )
  )
}