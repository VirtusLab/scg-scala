package com.virtuslab.semanticgraphs.scalac_plugin.graphgenerator.analyzerv2

import com.virtuslab.semanticgraphs.proto.model.GraphNodeOuterClass.{Edge, GraphNode, Location, SemanticGraphFile}

object ProtoHelper {
  def createLocation(uri: String, startLine: Int, startCharacter: Int, endLine: Int, endCharacter: Int): Location = {
    Location
      .newBuilder()
      .setUri(uri)
      .setStartLine(startLine)
      .setStartCharacter(startCharacter)
      .setEndLine(endLine)
      .setEndCharacter(endCharacter)
      .build()
  }

  def createEdge(to: String, _type: String, location: Option[Location]): Edge = {
    val edgeBuilder = Edge
      .newBuilder()
      .setTo(to)
      .setType(_type)
    location.foreach(l => edgeBuilder.setLocation(l))
      edgeBuilder.build()
  }

  def createGraphNode(id: String, kind: String, displayName: String, edges: java.util.List[Edge], property: (String, String)): GraphNode =
    GraphNode.newBuilder().setId(id).setKind(kind).setDisplayName(displayName).addAllEdges(edges).putProperties(property._1, property._2).build()

  def createSemanticGraphFile(uri: String, nodes: java.util.List[GraphNode]): SemanticGraphFile = {
    SemanticGraphFile.newBuilder().setUri(uri).addAllNodes(nodes).build()
  }

}
