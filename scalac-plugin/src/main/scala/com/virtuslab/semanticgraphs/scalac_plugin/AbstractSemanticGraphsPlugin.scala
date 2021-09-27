package com.virtuslab.semanticgraphs.scalac_plugin

import java.nio.file.{Path, Paths}
import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.plugins.{Plugin, PluginComponent}
import scala.util.control.NonFatal

import com.typesafe.scalalogging.Logger

class AbstractSemanticGraphsPlugin(override val global: Global) extends Plugin {

  override val name: String = "scala-compiler-plugin"
  override val description: String = "scala compiler plugin simple example"

  private var root: Path = Paths.get(".").toAbsolutePath.getParent

  override val components: List[PluginComponent] = List(new SemanticGraphsPluginComponent(global, root))
}

class SemanticGraphsPluginComponent(
  override val global: Global, 
  projectRoot: Path
)extends PluginComponent {

  val logger = Logger(classOf[SemanticGraphsPluginComponent])

  override val phaseName: String = "semantic-code-graph-generator"
  override val runsAfter: List[String] = List("typer")

  override def newPhase(prev: Phase): Phase = new StdPhase(prev) {

    val generator = new SemanticGraphsGenerator(global, projectRoot)
    override def apply(unit: global.CompilationUnit): Unit = {
      try {
        generator.generateGraph(unit.asInstanceOf[generator.g.CompilationUnit])
      }
      catch {
        case NonFatal(e) => logger.warn(s"generating graph for ${unit.source.file.path} failed: $e")
      }
    }
  }
}
