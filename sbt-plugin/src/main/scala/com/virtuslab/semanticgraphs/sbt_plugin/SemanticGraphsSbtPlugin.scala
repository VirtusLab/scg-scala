import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin

object SemanticGraphsSbtPlugin extends AutoPlugin {

  override def trigger = allRequirements
  override def requires = JvmPlugin

  object autoImport {
//    val exampleSetting = settingKey[String]("Version of scalac plugin")
//    val exampleTask = taskKey[String]("A task that is automatically imported to the build")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
//    exampleSetting := "0.2.10-SNAPSHOT",
//    exampleTask := "computed from example setting: " + exampleSetting.value,
    //resolver for scalac-plugin, has to be in maven style, as scalac-plugin needs to be used also from maven build tool
    //resolvers += Resolver.bintrayRepo("virtuslab", "graphbuddy"),
    resolvers += "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    addCompilerPlugin("org.virtuslab.semanticgraphs" % "scalac-plugin" % "0.2.19" cross CrossVersion.full),
    addCompilerPlugin("org.scalameta" % "semanticdb-scalac" % "4.8.4" cross CrossVersion.full),
    cleanFiles += baseDirectory.value / ".semanticgraphs",
    scalacOptions += "-Yrangepos"
  )

  override lazy val buildSettings = Seq()

  override lazy val globalSettings = Seq()
}
