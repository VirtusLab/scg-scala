name := """sbt-plugin"""
organization := "org.virtuslab.semanticgraphs"
version := "0.2.15"

sbtPlugin := true

// choose a test framework

// utest
//libraryDependencies += "com.lihaoyi" %% "utest" % "0.4.8" % "test"
//testFrameworks += new TestFramework("utest.runner.Framework")

// ScalaTest
//libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1" % "test"
//libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

// Specs2
//libraryDependencies ++= Seq("org.specs2" %% "specs2-core" % "3.9.1" % "test")
//scalacOptions in Test ++= Seq("-Yrangepos")

initialCommands in console := """import com.virtuslab.semanticgraphs.sbt_plugin._"""

enablePlugins(ScriptedPlugin)
// set up 'scripted; sbt plugin for testing sbt plugins
scriptedLaunchOpts ++=
  Seq("-Xmx1024M", "-Dplugin.version=" + version.value)

crossSbtVersions := Vector("1.4.9", "0.13.18")

publishTo := sonatypePublishToBundle.value

// Your profile name of the sonatype account. The default is the same with the organization value
sonatypeProfileName := "org.virtuslab"

// To sync with Maven central, you need to supply the following information:
publishMavenStyle := true

// Open-source license of your choice
licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

// Where is the source code hosted: GitHub or GitLab?
import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("graphbuddy", "graphbuddy", "graphbuddy@virtuslab.com"))

// or if you want to set these fields manually
homepage := Some(url("https://graphbuddy.virtuslab.com/"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/virtuslab/graphbuddy"),
    "scm:git@github.com:virtuslab/graphbuddy.git"
  )
)
developers := List(
  Developer(
    id = "kborowski",
    name = "Krzysztof Borowski",
    email = "kborowski@virtuslab.com",
    url = url("https://github.com/liosedhel")
  )
)
