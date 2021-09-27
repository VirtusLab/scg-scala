package com.virtuslab.semanticgraphs.scalac_plugin

import java.io.File
import java.nio.file.Path

import com.typesafe.scalalogging.Logger
import com.virtuslab.semanticgraphs.proto.model.GraphNodeOuterClass.SemanticGraphFile
import com.virtuslab.semanticgraphs.scalac_plugin.graphgenerator.analyzerv2._

import scala.meta.internal.semanticdb.scalac.SemanticdbOps
import scala.tools.nsc.Global

class SemanticGraphsGenerator(override val global: Global, projectRoot: Path) extends SemanticdbOps {

  val logger = Logger(this.getClass)

  def generateGraph(source: global.CompilationUnit): Unit = {
    val textDocument = source.toTextDocument
    val file         = projectRoot.resolve(textDocument.uri).toString

    val graphAccumulator = new GraphAccumulator(textDocument.uri)
    val helper           = new SemanticdbHelper(textDocument)
    new SemanticdbGraphExtractor(graphAccumulator, helper).createInitialGraphBasedOnSemanticDB()

    val tree              = TreeExtractor.extractTree(file)
    val semanticGraphFile = new AstGraphExtractor(graphAccumulator, helper).augmentTheInitialGraph(tree)
    dumpFile(projectRoot, semanticGraphFile)
  }

  def dumpFile(projectRoot: Path, semanticGraphFile: SemanticGraphFile) = {
    val fileUri = s"${projectRoot.toAbsolutePath}/.semanticgraphs/${semanticGraphFile.getUri}.semanticgraphdb"
    import java.io.FileOutputStream
    val file = new File(fileUri)
    file.getParentFile.mkdirs()
    file.createNewFile()
    val outputStream = new FileOutputStream(file, false)
    outputStream.write(semanticGraphFile.toByteArray)
    outputStream.close()
  }

}
