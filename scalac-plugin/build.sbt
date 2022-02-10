name := "scalac-plugin"

version := "0.2.16"

ThisBuild / crossScalaVersions := Seq(
  "2.11.12",
  "2.12.8",
  "2.12.9",
  "2.12.10",
  "2.12.11",
  "2.12.12",
  "2.12.13",
  "2.12.14",
  "2.12.15",
  "2.13.0",
  "2.13.1",
  "2.13.2",
  "2.13.3",
  "2.13.4",
  "2.13.5",
  "2.13.6",
  "2.13.7",
  "2.13.8"
)

ThisBuild / scalaVersion := "2.13.8"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")
scalacOptions += "-Ywarn-unused"

crossVersion := CrossVersion.full

organization := "org.virtuslab.semanticgraphs"

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value

libraryDependencies += "org.scalameta"              % "semanticdb-scalac-core" % "4.4.34" cross CrossVersion.full
libraryDependencies += "ch.qos.logback"             % "logback-classic"        % "1.2.10"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging"         % "3.9.4"

// https://mvnrepository.com/artifact/com.google.protobuf/protobuf-java
libraryDependencies += "com.google.protobuf" % "protobuf-java" % "3.12.4"

publishMavenStyle := true

pomIncludeRepository := { _ =>
  false
}

autoScalaLibrary := false

makePom := makePom.dependsOn(assembly).value
packageBin in Compile := crossTarget.value / (assemblyJarName in assembly).value

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x                             => MergeStrategy.first // dedupliate, weird errors with the same scala-library .class files, like nowarn.class
}

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
