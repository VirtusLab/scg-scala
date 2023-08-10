name := "scalac-plugin"

version := "0.2.20"

ThisBuild / crossScalaVersions := Seq(
  "2.13.11",
  "2.13.10",
  "2.13.9",
  "2.13.8",
  "2.12.18",
  "2.12.17",
  //"2.12.16",
  "2.12.15",
  "2.11.12"
)

ThisBuild / scalaVersion := "2.13.11"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")
scalacOptions += "-Ywarn-unused"

crossVersion := CrossVersion.full

organization := "org.virtuslab.semanticgraphs"

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value

libraryDependencies += "org.scalameta"              % "semanticdb-scalac-core" % "4.8.7" cross CrossVersion.full
libraryDependencies += "ch.qos.logback"             % "logback-classic"        % "1.2.10"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging"         % "3.9.5"

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
